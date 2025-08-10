'use client'

import { useState } from 'react'
import { useAuth } from '@/hooks/useAuth'
import PointsManager from '@/components/PointsManager'
import WalletConnect from '@/components/WalletConnect'
import GovernanceVoting from '@/components/GovernanceVoting'
import type { WalletState } from '@/types'

export default function Home() {
  const { user, isAuthenticated } = useAuth()
  const [wallet, setWallet] = useState<WalletState>({ isConnected: false })

  const handleWalletChange = (walletState: WalletState) => {
    setWallet(walletState)
  }

  return (
    <div className="px-6 py-8">
      {/* Hero Section */}
      <section className="text-center mb-12" id="hero">
        <h2 className="text-4xl font-bold text-gray-800 mb-4">
          Welcome to Blooming Blockchain Service
        </h2>
        <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
          Experience the future of blockchain with Account Abstraction, gasless transactions, 
          and governance voting powered by zkSync technology.
        </p>
        
        {!isAuthenticated && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 max-w-2xl mx-auto">
            <h3 className="text-lg font-semibold text-blue-800 mb-2">
              Sign in to access all features
            </h3>
            <p className="text-blue-600 text-sm">
              Click "Sign In" in the upper right corner to start earning points, 
              exchanging tokens, and participating in governance voting.
            </p>
          </div>
        )}
      </section>

      {/* Features Overview - Always Visible */}
      <section className="mb-12" id="features">
        <h3 className="text-2xl font-bold text-gray-800 mb-6 text-center">
          Platform Features
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">🪙</div>
            <h4 className="text-xl font-semibold mb-2">Points System</h4>
            <p className="text-gray-600">
              Earn sub points through activities, convert to main points, 
              and exchange for governance tokens at a 10:1 ratio.
            </p>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">⚡</div>
            <h4 className="text-xl font-semibold mb-2">Gasless Transactions</h4>
            <p className="text-gray-600">
              All blockchain transactions are sponsored via zkSync paymaster. 
              No ETH needed for gas fees.
            </p>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">🗳️</div>
            <h4 className="text-xl font-semibold mb-2">Governance Voting</h4>
            <p className="text-gray-600">
              Participate in decentralized governance with your BLOOM tokens. 
              Create proposals and vote on important decisions.
            </p>
          </div>
        </div>
      </section>

      {/* Main Application - Conditional Content */}
      {isAuthenticated && user ? (
        <>
          {/* User Dashboard */}
          <section className="mb-12">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Left Column */}
              <div className="space-y-6" id="points">
                <PointsManager user={user} />
                <WalletConnect onWalletChange={handleWalletChange} />
              </div>

              {/* Right Column */}
              <div className="space-y-6" id="governance">
                <GovernanceVoting user={user} wallet={wallet} />
              </div>
            </div>
          </section>

          {/* Smart Wallet Info */}
          {user.smartWalletAddress && (
            <section className="mb-12" id="wallet">
              <div className="bg-white rounded-lg shadow-md p-6">
                <h3 className="text-lg font-semibold mb-3">Account Abstraction Wallet</h3>
                <div className="bg-gray-50 p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-2">Your Smart Wallet Address:</p>
                  <p className="font-mono text-sm bg-white p-2 rounded border break-all">
                    {user.smartWalletAddress}
                  </p>
                  <p className="text-xs text-gray-500 mt-2">
                    This wallet was automatically created by the Spring Boot backend using Account Abstraction.
                    All transactions are gasless via zkSync paymaster.
                  </p>
                </div>
              </div>
            </section>
          )}
        </>
      ) : (
        /* Public Demo Content */
        <section className="mb-12" id="demo">
          <div className="bg-white rounded-lg shadow-md p-8">
            <h3 className="text-2xl font-semibold text-center mb-6">
              System Architecture Demo
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div>
                <h4 className="text-lg font-semibold mb-3 text-blue-700">Backend (Spring Boot)</h4>
                <ul className="text-sm text-gray-600 space-y-2">
                  <li>• Google OAuth 2.0 authentication</li>
                  <li>• JWT token management</li>
                  <li>• H2/PostgreSQL database with JPA</li>
                  <li>• Point system with transaction logging</li>
                  <li>• Role-based access control (Admin/User)</li>
                  <li>• RESTful APIs for all operations</li>
                </ul>
              </div>
              <div>
                <h4 className="text-lg font-semibold mb-3 text-green-700">Blockchain (zkSync)</h4>
                <ul className="text-sm text-gray-600 space-y-2">
                  <li>• ERC20 governance token with voting</li>
                  <li>• Account Abstraction with paymaster</li>
                  <li>• Gasless transactions for all users</li>
                  <li>• zkSync Sepolia testnet deployment</li>
                  <li>• OpenZeppelin security standards</li>
                  <li>• Decentralized governance proposals</li>
                </ul>
              </div>
            </div>
          </div>
        </section>
      )}

      {/* System Status - Always Visible */}
      <section className="mb-8" id="status">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-800 mb-4 text-center">System Status</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div className="text-center">
              <p className="font-medium text-blue-700">Backend API</p>
              <p className="text-blue-600">✅ Spring Boot Ready</p>
            </div>
            <div className="text-center">
              <p className="font-medium text-blue-700">Blockchain</p>
              <p className="text-blue-600">✅ zkSync Sepolia</p>
            </div>
            <div className="text-center">
              <p className="font-medium text-blue-700">Smart Contracts</p>
              <p className="text-blue-600">✅ Deployed & Funded</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
