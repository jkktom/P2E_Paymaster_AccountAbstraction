'use client'

import { useState } from 'react'
import GoogleAuth from '@/components/GoogleAuth'
import PointsManager from '@/components/PointsManager'
import WalletConnect from '@/components/WalletConnect'
import GovernanceVoting from '@/components/GovernanceVoting'
import { authAPI } from '@/lib/api'
import type { User, WalletState } from '@/types'

export default function Home() {
  const [user, setUser] = useState<User | null>(null)
  const [wallet, setWallet] = useState<WalletState>({ isConnected: false })

  const handleAuthSuccess = (authenticatedUser: User) => {
    setUser(authenticatedUser)
  }

  const handleSignOut = () => {
    authAPI.signOut()
    setUser(null)
    setWallet({ isConnected: false })
  }

  const handleWalletChange = (walletState: WalletState) => {
    setWallet(walletState)
  }

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
        <div className="max-w-md w-full">
          <GoogleAuth onAuthSuccess={handleAuthSuccess} />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">
              Blooming Blockchain Service
            </h1>
            <p className="text-sm text-gray-600">
              zkSync Account Abstraction POC
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <div className="text-right">
              <p className="font-medium text-gray-800">{user.name}</p>
              <p className="text-sm text-gray-600">{user.email}</p>
              <span className={`inline-block px-2 py-1 rounded-full text-xs ${
                user.id.includes('admin') ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'
              }`}>
                {user.id.includes('admin') ? 'Admin' : 'User'}
              </span>
            </div>
            <button
              onClick={handleSignOut}
              className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
            >
              Sign Out
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left Column */}
          <div className="space-y-6">
            <PointsManager user={user} />
            <WalletConnect onWalletChange={handleWalletChange} />
          </div>

          {/* Right Column */}
          <div className="space-y-6">
            <GovernanceVoting user={user} wallet={wallet} />
          </div>
        </div>

        {/* Smart Wallet Info */}
        {user.smartWalletAddress && (
          <div className="mt-8 bg-white rounded-lg shadow-md p-6">
            <h3 className="text-lg font-semibold mb-3">Account Abstraction Wallet</h3>
            <div className="bg-gray-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600 mb-2">Your Smart Wallet Address:</p>
              <p className="font-mono text-sm bg-white p-2 rounded border">
                {user.smartWalletAddress}
              </p>
              <p className="text-xs text-gray-500 mt-2">
                This wallet was automatically created by the Spring Boot backend using Account Abstraction.
                All transactions are gasless via zkSync paymaster.
              </p>
            </div>
          </div>
        )}

        {/* System Status */}
        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h3 className="text-lg font-semibold text-blue-800 mb-2">System Status</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div>
              <p className="font-medium text-blue-700">Backend API</p>
              <p className="text-blue-600">✅ Spring Boot Connected</p>
            </div>
            <div>
              <p className="font-medium text-blue-700">Blockchain</p>
              <p className="text-blue-600">✅ zkSync Sepolia</p>
            </div>
            <div>
              <p className="font-medium text-blue-700">Contracts</p>
              <p className="text-blue-600">✅ Deployed & Funded</p>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t mt-12">
        <div className="max-w-6xl mx-auto px-4 py-6 text-center text-gray-600">
          <p className="text-sm">
            Technical Interview Submission - Blockchain Goods Exchange Service
          </p>
          <p className="text-xs mt-1">
            Spring Boot + zkSync + Account Abstraction + Next.js
          </p>
        </div>
      </footer>
    </div>
  )
}
