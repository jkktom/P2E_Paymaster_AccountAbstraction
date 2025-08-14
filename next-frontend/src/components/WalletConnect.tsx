'use client'

import { useState, useEffect } from 'react'
import { Wallet } from 'zksync-ethers'
import { getZkSyncProvider, getGovernanceTokenContract } from '@/lib/contracts'
import type { WalletState } from '@/types'

interface WalletConnectProps {
  onWalletChange: (wallet: WalletState) => void
}

export default function WalletConnect({ onWalletChange }: WalletConnectProps) {
  const [wallet, setWallet] = useState<WalletState>({ isConnected: false })
  const [isConnecting, setIsConnecting] = useState(false)

  const connectWallet = async () => {
    if (!window.ethereum) {
      alert('Please install MetaMask or use a Web3-enabled browser')
      return
    }

    setIsConnecting(true)
    try {
      // Request account access
      await window.ethereum.request({ method: 'eth_requestAccounts' })
      
      const provider = getZkSyncProvider()
      const accounts = await window.ethereum.request({ method: 'eth_accounts' })
      
      if (accounts.length > 0) {
        const address = accounts[0]
        const zkWallet = new Wallet(address, provider)
        
        // Get balance and voting power
        const balance = await provider.getBalance(address)
        const governanceContract = getGovernanceTokenContract(provider)
        const votingPower = await governanceContract.getVotingPower(address)

        const walletState: WalletState = {
          address,
          isConnected: true,
          balance: balance.toString(),
          votingPower: votingPower.toString(),
        }

        setWallet(walletState)
        onWalletChange(walletState)
      }
    } catch (error) {
      console.error('Failed to connect wallet:', error)
      alert('Failed to connect wallet')
    } finally {
      setIsConnecting(false)
    }
  }

  const disconnectWallet = () => {
    const walletState: WalletState = { isConnected: false }
    setWallet(walletState)
    onWalletChange(walletState)
  }

  // Auto-connect if previously connected
  useEffect(() => {
    if (window.ethereum && window.ethereum.selectedAddress) {
      connectWallet()
    }
  }, [])

  return (
    <div className="flex flex-col items-center gap-4 p-6 bg-white rounded-lg shadow-md">
      <h2 className="text-xl font-semibold">zkSync Wallet</h2>
      
      {!wallet.isConnected ? (
        <button
          onClick={connectWallet}
          disabled={isConnecting}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          {isConnecting ? 'Connecting...' : 'Connect Wallet'}
        </button>
      ) : (
        <div className="text-center">
          <p className="text-sm mb-2">Connected</p>
          <p className="font-mono text-sm bg-gray-100 p-2 rounded">
            {wallet.address?.slice(0, 6)}...{wallet.address?.slice(-4)}
          </p>
          <div className="mt-2 text-sm">
            <p>ETH Balance: {wallet.balance ? (parseFloat(wallet.balance) / 1e18).toFixed(4) : '0'}</p>
            <p>Voting Power: {wallet.votingPower ? (parseFloat(wallet.votingPower) / 1e18).toFixed(2) : '0'} BLOOM</p>
          </div>
          <button
            onClick={disconnectWallet}
            className="mt-4 px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
          >
            Disconnect
          </button>
        </div>
      )}
    </div>
  )
}