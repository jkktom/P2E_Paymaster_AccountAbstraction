import { HardhatUserConfig } from "hardhat/config";
import "@nomicfoundation/hardhat-toolbox";
import "@openzeppelin/hardhat-upgrades";
import "@matterlabs/hardhat-zksync-solc";
import "@matterlabs/hardhat-zksync-deploy";
import "@matterlabs/hardhat-zksync-verify";
import "dotenv/config";

const config: HardhatUserConfig = {
  solidity: {
    version: "0.8.20",
    settings: {
      optimizer: {
        enabled: true,
        runs: 200,
      },
    },
  },
  // zkSync 컴파일러 설정
  zksolc: {
    version: "latest",
    settings: {
      optimizer: {
        enabled: true,
        runs: 200,
      },
    },
  },
  networks: {
    hardhat: {
      chainId: 1337,
      accounts: {
        count: 10,
        accountsBalance: "10000000000000000000000", // 10,000 ETH
      },
    },
    localhost: {
      url: "http://127.0.0.1:8545",
      chainId: 1337,
    },
    // sepolia: {
    //   url: `https://eth-sepolia.g.alchemy.com/v2/${process.env.ALCHEMY_API_KEY}`,
    //   accounts: process.env.PRIVATE_KEY ? [process.env.PRIVATE_KEY] : [],
    //   gasPrice: "auto",
    // },
    // zkSync Sepolia 네트워크 설정 개선
    zkSyncSepolia: {
      url: "https://sepolia.era.zksync.dev",
      ethNetwork: "sepolia", // Ethereum L1 네트워크 지정
      accounts: process.env.PRIVATE_KEY ? [process.env.PRIVATE_KEY] : [],
      chainId: 300,
      zksync: true, // zkSync 네트워크임을 명시
      verifyURL: "https://explorer.sepolia.era.zksync.dev/contract_verification", // 검증용 URL
    },
    // zkSync Era Mainnet (나중에 사용)
    // zkSync: {
    //   url: "https://mainnet.era.zksync.io",
    //   ethNetwork: "mainnet",
    //   accounts: process.env.PRIVATE_KEY ? [process.env.PRIVATE_KEY] : [],
    //   chainId: 324,
    //   zksync: true,
    //   verifyURL: "https://zksync2-mainnet-explorer.zksync.io/contract_verification",
    // },
    // mainnet: {
    //   url: `https://eth-mainnet.g.alchemy.com/v2/${process.env.ALCHEMY_API_KEY}`,
    //   accounts: process.env.PRIVATE_KEY ? [process.env.PRIVATE_KEY] : [],
    //   gasPrice: "auto",
    // },
  },
  gasReporter: {
    enabled: true,
    currency: "USD",
    gasPrice: 20,
  },
  // etherscan: {
  //   apiKey: {
  //     sepolia: process.env.ETHERSCAN_API_KEY || "",
  //     mainnet: process.env.ETHERSCAN_API_KEY || "",
  //   },
  // },
  // zkSync 기본 네트워크 설정
  defaultNetwork: "hardhat",
};

export default config;