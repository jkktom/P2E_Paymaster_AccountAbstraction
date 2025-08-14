'use client'

import { useState, useEffect } from 'react'
import { pointsAPI, exchangeAPI } from '@/lib/api'
import { useAuth } from '@/hooks/useAuth'
import type { MainPointAccount, SubPointAccount, TransactionStatus, User } from '@/types'

interface PointsManagerProps {
  user: User
}

export default function PointsManager({ user }: PointsManagerProps) {
  const { refreshBalance } = useAuth()
  const [mainPoints, setMainPoints] = useState<MainPointAccount | null>(null)
  const [subPoints, setSubPoints] = useState<SubPointAccount | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [txStatus, setTxStatus] = useState<TransactionStatus>({ status: 'success' })

  useEffect(() => {
    fetchPointsData()
  }, [])

  const fetchPointsData = async () => {
    try {
      const [mainPointsData, subPointsData] = await Promise.all([
        pointsAPI.getMainPoints(),
        pointsAPI.getSubPoints()
      ])
      
      setMainPoints(mainPointsData)
      setSubPoints(subPointsData)
    } catch (error) {
      console.error('Failed to fetch points:', error)
    }
  }

  // Simulate point earning for demo - calls backend admin API
  const earnMainPoints = async (amount: number = 10) => {
    setIsLoading(true)
    setTxStatus({ status: 'pending' })

    try {
      // Call backend API to grant points (for demo purposes using admin endpoint)
      await pointsAPI.earnDemoPoints('MAIN', amount)
      setTxStatus({ status: 'success' })
      await fetchPointsData()
      await refreshBalance() // Update header balance
    } catch (error: any) {
      console.error('Failed to earn main points:', error)
      setTxStatus({ 
        status: 'error', 
        error: error.response?.data?.message || 'Failed to earn main points' 
      })
    } finally {
      setIsLoading(false)
    }
  }

  const earnSubPoints = async (amount: number = 10) => {
    setIsLoading(true)
    setTxStatus({ status: 'pending' })

    try {
      // Call backend API to grant points (for demo purposes using admin endpoint)
      await pointsAPI.earnDemoPoints('SUB', amount)
      setTxStatus({ status: 'success' })
      await fetchPointsData()
      await refreshBalance() // Update header balance
    } catch (error: any) {
      console.error('Failed to earn sub points:', error)
      setTxStatus({ 
        status: 'error', 
        error: error.response?.data?.message || 'Failed to earn sub points' 
      })
    } finally {
      setIsLoading(false)
    }
  }

  // Convert sub points to main points via backend service
  const convertSubToMain = async () => {
    if (!subPoints || subPoints.balance < 10) return

    setIsLoading(true)
    setTxStatus({ status: 'pending' })

    try {
      // Call backend conversion API
      await pointsAPI.convertSubToMain(10)
      setTxStatus({ status: 'success' })
      await fetchPointsData()
      await refreshBalance() // Update header balance
    } catch (error: any) {
      console.error('Failed to convert points:', error)
      setTxStatus({ 
        status: 'error', 
        error: error.response?.data?.message || 'Failed to convert points' 
      })
    } finally {
      setIsLoading(false)
    }
  }

  // Exchange main points for governance tokens via backend + blockchain
  const exchangeToTokens = async () => {
    if (!mainPoints || mainPoints.balance < 10) return

    setIsLoading(true)
    setTxStatus({ status: 'pending' })

    try {
      // Call backend token exchange API (handles both DB update and blockchain tx)
      const result = await exchangeAPI.pointsToTokens(10)
      setTxStatus({ 
        status: 'success',
        hash: result.txHash 
      })
      await fetchPointsData()
    } catch (error: any) {
      console.error('Failed to exchange tokens:', error)
      setTxStatus({ 
        status: 'error', 
        error: error.response?.data?.message || 'Failed to exchange tokens' 
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-semibold mb-4">Points Management</h2>
      
      {/* Point Balances */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="bg-blue-50 p-4 rounded-lg">
          <h3 className="font-medium text-blue-800">Sub Points</h3>
          <p className="text-2xl font-bold text-blue-600">
            {subPoints?.balance || 0}
          </p>
          <p className="text-sm text-gray-600">
            Total Earned: {subPoints?.totalEarned || 0}
          </p>
          <p className="text-sm text-gray-500">
            Converted: {subPoints?.subToMain || 0}
          </p>
        </div>
        
        <div className="bg-green-50 p-4 rounded-lg">
          <h3 className="font-medium text-green-800">Main Points</h3>
          <p className="text-2xl font-bold text-green-600">
            {mainPoints?.balance || 0}
          </p>
          <p className="text-sm text-gray-600">
            Total Earned: {mainPoints?.totalEarned || 0}
          </p>
          <p className="text-sm text-gray-500">
            To Tokens: {mainPoints?.pointsToToken || 0}
          </p>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="space-y-3">
        <button
          onClick={() => earnMainPoints(10)}
          disabled={isLoading}
          className="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Earn 10 Main Points (Demo)
        </button>

        <button
          onClick={() => earnSubPoints(10)}
          disabled={isLoading}
          className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Earn 10 Sub Points (Demo)
        </button>

        <button
          onClick={convertSubToMain}
          disabled={isLoading || !subPoints || subPoints.balance < 10}
          className="w-full px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Convert 10 Sub → 1 Main Point
        </button>

        <button
          onClick={exchangeToTokens}
          disabled={isLoading || !mainPoints || mainPoints.balance < 10}
          className="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Exchange 10 Main → 1 BLOOM Token (Gasless!)
        </button>
      </div>

      {/* Transaction Status */}
      {txStatus.status !== 'success' && (
        <div className={`mt-4 p-3 rounded-lg ${
          txStatus.status === 'pending' ? 'bg-yellow-50 border border-yellow-200' :
          txStatus.status === 'error' ? 'bg-red-50 border border-red-200' : ''
        }`}>
          <div className={`text-sm font-medium ${
            txStatus.status === 'pending' ? 'text-yellow-800' :
            txStatus.status === 'error' ? 'text-red-800' : ''
          }`}>
            {txStatus.status === 'pending' && '⏳ Processing transaction...'}
            {txStatus.status === 'error' && `❌ ${txStatus.error}`}
          </div>
          {txStatus.hash && (
            <p className="text-xs text-gray-600 mt-1 font-mono">
              Tx: {txStatus.hash.slice(0, 10)}...{txStatus.hash.slice(-8)}
            </p>
          )}
        </div>
      )}

      {/* Info */}
      <div className="mt-6 text-xs text-gray-500 bg-gray-50 p-3 rounded">
        <p><strong>Note:</strong> All operations are processed through the Spring Boot backend.</p>
        <p>• Point earning uses backend service with transaction logging</p>
        <p>• Conversion follows 10:1 ratio enforced by JPA validation</p>
        <p>• Token exchange uses zkSync paymaster for gasless transactions</p>
      </div>
    </div>
  )
}