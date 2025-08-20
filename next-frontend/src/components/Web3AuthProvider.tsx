'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { Web3Auth } from '@web3auth/modal';
import { CHAIN_NAMESPACES, WEB3AUTH_NETWORK } from '@web3auth/base';
import { EthereumPrivateKeyProvider } from '@web3auth/ethereum-provider';
import { ethers } from 'ethers';
import { useAuth } from '@/hooks/useAuth';

// zkSync Era Sepolia configuration
const chainConfig = {
  chainNamespace: CHAIN_NAMESPACES.EIP155,
  chainId: "0x12C", // 300 in hex (zkSync Era Sepolia)
  rpcTarget: process.env.NEXT_PUBLIC_ZKSYNC_RPC_URL || "https://sepolia.era.zksync.dev",
  displayName: "zkSync Era Sepolia",
  blockExplorerUrl: "https://sepolia.explorer.zksync.io/",
  ticker: "ETH",
  tickerName: "Ethereum",
};

interface Web3AuthContextType {
  web3auth: Web3Auth | null;
  provider: any;
  signer: ethers.Signer | null;
  walletAddress: string | null;
  isLoading: boolean;
  isConnected: boolean;
  connectWallet: () => Promise<void>;
  disconnectWallet: () => Promise<void>;
  signTransaction: (transactionData: any) => Promise<string>;
}

const Web3AuthContext = createContext<Web3AuthContextType | null>(null);

export const useWeb3Auth = () => {
  const context = useContext(Web3AuthContext);
  if (!context) {
    throw new Error('useWeb3Auth must be used within Web3AuthProvider');
  }
  return context;
};

interface Web3AuthProviderProps {
  children: ReactNode;
}

export const Web3AuthProvider = ({ children }: Web3AuthProviderProps) => {
  const [web3auth, setWeb3auth] = useState<Web3Auth | null>(null);
  const [provider, setProvider] = useState<any>(null);
  const [signer, setSigner] = useState<ethers.Signer | null>(null);
  const [walletAddress, setWalletAddress] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isConnected, setIsConnected] = useState(false);
  const { user, isAuthenticated } = useAuth();

  useEffect(() => {
    const init = async () => {
      try {
        const clientId = process.env.NEXT_PUBLIC_WEB3AUTH_CLIENT_ID;
        const network = process.env.NEXT_PUBLIC_WEB3AUTH_NETWORK;
        
        if (!clientId) {
          console.error('Web3Auth Client ID not found in environment variables');
          setIsLoading(false);
          return;
        }

        console.log('Initializing Web3Auth with:', { clientId, network });

        // Create private key provider
        const privateKeyProvider = new EthereumPrivateKeyProvider({
          config: { chainConfig },
        });

        // Create Web3Auth instance
        const web3authInstance = new Web3Auth({
          clientId,
          web3AuthNetwork: network === 'sapphire_devnet' ? WEB3AUTH_NETWORK.SAPPHIRE_DEVNET : WEB3AUTH_NETWORK.SAPPHIRE_MAINNET,
          chainConfig,
          privateKeyProvider,
          uiConfig: {
            mode: "dark", // or "light"
            useLogoLoader: true,
            logoLight: "https://web3auth.io/images/web3auth-logo.svg",
            logoDark: "https://web3auth.io/images/web3auth-logo---Dark.svg",
            defaultLanguage: "en",
            theme: {
              primary: "#768729"
            }
          }
        });

        // Initialize
        await web3authInstance.init();
        setWeb3auth(web3authInstance);

        console.log('Web3Auth initialized successfully');

        // Check if already connected
        if (web3authInstance.connected) {
          const web3authProvider = web3authInstance.provider;
          if (web3authProvider) {
            await setupProvider(web3authProvider);
          }
        }
      } catch (error) {
        console.error('Web3Auth initialization failed:', error);
      } finally {
        setIsLoading(false);
      }
    };

    init();
  }, []);

  // Auto-connect Web3Auth when user is logged in
  useEffect(() => {
    const autoConnect = async () => {
      if (isAuthenticated && user && web3auth && !isConnected && !isLoading) {
        try {
          console.log('Auto-connecting Web3Auth for logged-in user:', user.email);
          await connectWallet();
        } catch (error) {
          console.warn('Auto-connect failed, user will need to connect manually when voting:', error);
        }
      }
    };

    autoConnect();
  }, [isAuthenticated, user, web3auth, isConnected, isLoading]);

  const setupProvider = async (web3authProvider: any) => {
    try {
      const ethersProvider = new ethers.BrowserProvider(web3authProvider);
      const ethersSigner = await ethersProvider.getSigner();
      const address = await ethersSigner.getAddress();

      setProvider(web3authProvider);
      setSigner(ethersSigner);
      setWalletAddress(address);
      setIsConnected(true);

      console.log('Web3Auth wallet connected:', address);

      // Update backend with new wallet address
      try {
        await fetch('/api/users/update-wallet', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ walletAddress: address }),
        });
      } catch (error) {
        console.warn('Failed to update backend with wallet address:', error);
      }
    } catch (error) {
      console.error('Failed to setup Web3Auth provider:', error);
    }
  };

  const connectWallet = async () => {
    if (!web3auth) {
      throw new Error('Web3Auth not initialized');
    }

    try {
      setIsLoading(true);
      
      console.log('Connecting to Web3Auth...');
      
      // Connect specifically with Google
      const web3authProvider = await web3auth.connectTo("google");
      
      if (web3authProvider) {
        console.log('Web3Auth connected, setting up provider...');
        await setupProvider(web3authProvider);
      } else {
        throw new Error('Failed to get Web3Auth provider');
      }
    } catch (error) {
      console.error('Web3Auth connection failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const disconnectWallet = async () => {
    if (!web3auth) return;

    try {
      await web3auth.logout();
      setProvider(null);
      setSigner(null);
      setWalletAddress(null);
      setIsConnected(false);
      console.log('Web3Auth disconnected');
    } catch (error) {
      console.error('Web3Auth disconnect failed:', error);
    }
  };

  const signTransaction = async (transactionData: any): Promise<string> => {
    if (!signer) {
      throw new Error('Wallet not connected');
    }

    try {
      console.log('Signing transaction:', transactionData);
      
      // Send transaction
      const tx = await signer.sendTransaction(transactionData);
      console.log('Transaction sent:', tx.hash);
      
      // Wait for transaction confirmation
      console.log('Waiting for transaction confirmation...');
      const receipt = await tx.wait();
      console.log('Transaction confirmed:', receipt?.hash);
      
      return tx.hash;
    } catch (error) {
      console.error('Transaction failed:', error);
      throw error;
    }
  };

  const value: Web3AuthContextType = {
    web3auth,
    provider,
    signer,
    walletAddress,
    isLoading,
    isConnected,
    connectWallet,
    disconnectWallet,
    signTransaction,
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg">Initializing Web3Auth...</div>
      </div>
    );
  }

  return (
    <Web3AuthContext.Provider value={value}>
      {children}
    </Web3AuthContext.Provider>
  );
};