export interface User {
  id?: string
  googleId: string
  email: string
  name: string
  avatar?: string
  roleId: number
  smartWalletAddress?: string
  createdAt: string
}

export interface UserBalance {
  mainPoint: number
  subPoint: number
  tokenBalance: number
  updatedAt?: string
}

export interface PointAccount {
  id: string
  userId: string
  balance: number
  totalEarned: number
  createdAt: string
  updatedAt: string
}

export interface MainPointAccount extends PointAccount {
  pointsToToken: number
}

export interface SubPointAccount extends PointAccount {
  subToMain: number
}

export interface Proposal {
  id: number
  description: string
  proposer: string
  forVotes: string
  againstVotes: string
  deadline: number
  executed: boolean
  canceled: boolean
}

export interface WalletState {
  address?: string
  isConnected: boolean
  balance?: string
  votingPower?: string
}

export interface TransactionStatus {
  hash?: string
  status: 'pending' | 'success' | 'error'
  error?: string
}

export interface SmartWalletBalance {
  address: string
  ethBalance: string
  ethBalanceFormatted: string
  governanceTokenBalance: string
  governanceTokenBalanceFormatted: string
}

export interface PaymasterInfo {
  address: string
  balance: string
  balanceInEth: string
  isActive: boolean
  governanceTokenAddress: string
}

export interface WalletSummary {
  wallet: {
    address: string
    ethBalance: string
    governanceTokenBalance: string
    isPaymasterEligible: boolean
  }
  paymaster: {
    address: string
    balance: string
    isActive: boolean
  }
  canPerformGaslessTransactions: boolean
}