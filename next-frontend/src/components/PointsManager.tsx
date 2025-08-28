'use client'

import { useState, useEffect } from 'react'
import { pointsAPI, exchangeAPI } from '@/lib/api'
import { useAuth } from '@/hooks/useAuth'
import { formatTokenBalance } from '@/utils/tokenUtils'
import type { MainPointAccount, SubPointAccount, TransactionStatus, User } from '@/types'

interface PointsManagerProps {
  user?: User
}

export default function PointsManager({ user }: PointsManagerProps) {
  const { refreshBalance, balance, isAuthenticated } = useAuth()
  const [mainPoints, setMainPoints] = useState<MainPointAccount | null>(null)
  const [subPoints, setSubPoints] = useState<SubPointAccount | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [txStatus, setTxStatus] = useState<TransactionStatus>({ status: 'success' })

  useEffect(() => {
    if (isAuthenticated) {
      fetchPointsData()
    }
  }, [isAuthenticated])

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

  // Check if user is logged in before performing actions
  const requireLogin = () => {
    if (!isAuthenticated) {
      alert('로그인이 필요합니다.');
      document.getElementById('hero')?.scrollIntoView({ behavior: 'smooth' });
      return false;
    }
    return true;
  };

  // Simulate point earning for demo - calls backend admin API
  const earnMainPoints = async (amount: number = 10) => {
    if (!requireLogin()) return;
    
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
    if (!requireLogin()) return;
    
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
    if (!requireLogin()) return;
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
    if (!requireLogin()) return;
    if (!mainPoints || mainPoints.balance < 10) return

    setIsLoading(true)
    setTxStatus({ status: 'pending' })

    try {
      // Call backend token exchange API (handles both DB update and blockchain tx)
      const result = await exchangeAPI.pointsToTokens(10)
      
      // Show success immediately
      setTxStatus({ 
        status: 'success',
        hash: result.transactionHash || result.txHash 
      })
      
      // Wait for blockchain confirmation before refreshing
      console.log('🎉 Token exchange successful!', result)
      
      // Wait 3 seconds for blockchain processing, then refresh
      setTimeout(async () => {
        await fetchPointsData()
        await refreshBalance() // Update header balance including token balance
      }, 3000)
      
    } catch (error: any) {
      console.error('Failed to exchange tokens:', error)
      console.error('Error details:', error.response?.data)
      
      setTxStatus({ 
        status: 'error', 
        error: error.response?.data?.error || error.response?.data?.message || 'Failed to exchange tokens' 
      })
    } finally {
      // Keep loading state for the full 3 seconds to show processing
      setTimeout(() => {
        setIsLoading(false)
      }, 3000)
    }
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-semibold mb-4">포인트 및 토큰 관리</h2>
      
      {/* Point and Token Balances */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-blue-50 p-4 rounded-lg">
          <h3 className="font-medium text-blue-800">서브포인트</h3>
          <p className="text-2xl font-bold text-blue-600">
            {subPoints?.balance || 0}
          </p>
          <p className="text-sm">
            총 획득: {subPoints?.totalEarned || 0}
          </p>
          <p className="text-sm">
            전환완료: {subPoints?.subToMain || 0}
          </p>
        </div>
        
        <div className="bg-green-50 p-4 rounded-lg">
          <h3 className="font-medium text-green-800">메인포인트</h3>
          <p className="text-2xl font-bold text-green-600">
            {mainPoints?.balance || 0}
          </p>
          <p className="text-sm">
            총 획득: {mainPoints?.totalEarned || 0}
          </p>
          <p className="text-sm">
            토큰교환: {mainPoints?.pointsToToken || 0}
          </p>
        </div>

        <div className="bg-purple-50 p-4 rounded-lg">
          <h3 className="font-medium text-purple-800">BLOOM 토큰</h3>
          <p className="text-2xl font-bold text-purple-600">
            {formatTokenBalance(balance?.tokenBalance || 0)}
          </p>
          <p className="text-sm">
            거버넌스 투표 가능
          </p>
          <p className="text-sm">
            가스리스 거래
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
          메인포인트 10개 획득 (데모)
        </button>

        <button
          onClick={() => earnSubPoints(10)}
          disabled={isLoading}
          className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          서브포인트 10개 획득 (데모)
        </button>

        <button
          onClick={convertSubToMain}
          disabled={isLoading || !subPoints || subPoints.balance < 10}
          className="w-full px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          서브포인트 10개 → 메인포인트 1개 전환
        </button>

        <button
          onClick={exchangeToTokens}
          disabled={isLoading || !mainPoints || mainPoints.balance < 10}
          className="w-full px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          메인포인트 10개 → BLOOM 토큰 1개 교환 (가스리스!)
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
            {txStatus.status === 'pending' && '⏳ 거래 처리 중...'}
            {txStatus.status === 'error' && `❌ ${txStatus.error}`}
          </div>
          {txStatus.hash && (
            <p className="text-xs mt-1 font-mono">
              Tx: {txStatus.hash.slice(0, 10)}...{txStatus.hash.slice(-8)}
            </p>
          )}
        </div>
      )}

      {/* Info */}
      <div className="mt-6 text-xs bg-gray-50 p-3 rounded">
        <p><strong>참고:</strong> 모든 작업은 Spring Boot 백엔드를 통해 처리됩니다.</p>
        <p>• 포인트 획득은 거래 기록과 함께 백엔드 서비스를 사용합니다</p>
        <p>• 전환은 JPA 검증으로 강제된 10:1 비율을 따릅니다</p>
        <p>• 토큰 교환은 가스리스 거래를 위해 zkSync 페이마스터를 사용합니다</p>
      </div>
    </div>
  )
}