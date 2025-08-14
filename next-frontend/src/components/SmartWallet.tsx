'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '@/hooks/useAuth'
import { SmartWalletAPI } from '@/lib/api/smartWallet'
import { getStoredToken } from '@/lib/api'
import type { WalletSummary } from '@/types'

export default function SmartWallet() {
  const { user, isAuthenticated } = useAuth()
  const [walletSummary, setWalletSummary] = useState<WalletSummary | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadWalletSummary = async () => {
    if (!isAuthenticated || !user?.smartWalletAddress) {
      return
    }

    setIsLoading(true)
    setError(null)
    
    try {
      const token = getStoredToken()
      if (!token) {
        throw new Error('No authentication token found')
      }

      const summary = await SmartWalletAPI.getWalletSummary(token)
      setWalletSummary(summary)
    } catch (err: any) {
      console.error('Failed to load wallet summary:', err)
      setError(err.message || 'Failed to load wallet information')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadWalletSummary()
  }, [isAuthenticated, user?.smartWalletAddress])

  const handleRefresh = () => {
    loadWalletSummary()
  }

  const handleGaslessExchange = async (amount: number) => {
    if (!walletSummary) return

    try {
      setIsLoading(true)
      const token = getStoredToken()
      if (!token) {
        throw new Error('No authentication token found')
      }

      const result = await SmartWalletAPI.performGaslessExchange(token, { amount })
      console.log('Gasless exchange result:', result)
      
      // Refresh wallet summary after successful exchange
      await loadWalletSummary()
      
      alert(`Gasless exchange successful!\nTx Hash: ${result.txHash}`)
    } catch (err: any) {
      console.error('Gasless exchange failed:', err)
      alert(`Gasless exchange failed: ${err.message}`)
    } finally {
      setIsLoading(false)
    }
  }

  if (!isAuthenticated) {
    return (
      <div className="flex flex-col items-center gap-4 p-6 bg-white rounded-lg shadow-md">
        <h2 className="text-xl font-semibold">Smart Wallet</h2>
        <p className="text-gray-600">Please sign in to view your smart wallet</p>
      </div>
    )
  }

  if (!user?.smartWalletAddress) {
    return (
      <div className="flex flex-col items-center gap-4 p-6 bg-white rounded-lg shadow-md">
        <h2 className="text-xl font-semibold">Smart Wallet</h2>
        <p className="text-yellow-600">Smart wallet is being created... Please refresh the page.</p>
      </div>
    )
  }

  if (isLoading && !walletSummary) {
    return (
      <div className="flex flex-col items-center gap-4 p-6 bg-white rounded-lg shadow-md">
        <h2 className="text-xl font-semibold">Smart Wallet</h2>
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
          <p className="text-gray-600">Loading wallet information...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex flex-col items-center gap-4 p-6 bg-white rounded-lg shadow-md">
        <h2 className="text-xl font-semibold">Smart Wallet</h2>
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <button
            onClick={handleRefresh}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    )
  }

  if (!walletSummary) {
    return (
      <div className="flex flex-col items-center gap-4 p-6 bg-white rounded-lg shadow-md">
        <h2 className="text-xl font-semibold">Smart Wallet</h2>
        <p className="text-gray-600">No wallet information available</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6 p-6 bg-white rounded-lg shadow-md">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Smart Wallet</h2>
        <button
          onClick={handleRefresh}
          disabled={isLoading}
          className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 disabled:opacity-50"
        >
          {isLoading ? 'Refreshing...' : 'Refresh'}
        </button>
      </div>

      {/* Wallet Information */}
      <div className="bg-gray-50 rounded-lg p-4">
        <h3 className="font-medium text-gray-900 mb-3">Wallet Details</h3>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-600">Address:</span>
            <span className="font-mono text-gray-900">
              {walletSummary.wallet.address.slice(0, 6)}...{walletSummary.wallet.address.slice(-4)}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">ETH Balance:</span>
            <span className="font-medium text-gray-900">{walletSummary.wallet.ethBalance}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">BLOOM Tokens:</span>
            <span className="font-medium text-gray-900">{walletSummary.wallet.governanceTokenBalance}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">Gasless Eligible:</span>
            <span className={`font-medium ${walletSummary.wallet.isPaymasterEligible ? 'text-green-600' : 'text-red-600'}`}>
              {walletSummary.wallet.isPaymasterEligible ? 'Yes' : 'No'}
            </span>
          </div>
        </div>
      </div>

      {/* Paymaster Information */}
      <div className="bg-blue-50 rounded-lg p-4">
        <h3 className="font-medium text-gray-900 mb-3">Paymaster Status</h3>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-600">Status:</span>
            <span className={`font-medium ${walletSummary.paymaster.isActive ? 'text-green-600' : 'text-red-600'}`}>
              {walletSummary.paymaster.isActive ? 'Active' : 'Inactive'}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">Balance:</span>
            <span className="font-medium text-gray-900">{walletSummary.paymaster.balance}</span>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="flex flex-col gap-3">
        <div className={`p-3 rounded-lg text-center text-sm ${
          walletSummary.canPerformGaslessTransactions 
            ? 'bg-green-100 text-green-800' 
            : 'bg-yellow-100 text-yellow-800'
        }`}>
          {walletSummary.canPerformGaslessTransactions
            ? '✅ Ready for gasless transactions'
            : '⚠️ Gasless transactions not available'
          }
        </div>

        {walletSummary.canPerformGaslessTransactions && (
          <div className="flex gap-2">
            <button
              onClick={() => handleGaslessExchange(1)}
              disabled={isLoading}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
            >
              {isLoading ? 'Processing...' : 'Exchange 1 BLOOM'}
            </button>
            <button
              onClick={() => handleGaslessExchange(5)}
              disabled={isLoading}
              className="flex-1 px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:opacity-50"
            >
              {isLoading ? 'Processing...' : 'Exchange 5 BLOOM'}
            </button>
          </div>
        )}

        {!walletSummary.wallet.isPaymasterEligible && (
          <div className="text-xs text-gray-600 text-center">
            You need at least 1 BLOOM token to perform gasless transactions
          </div>
        )}
      </div>
    </div>
  )
}