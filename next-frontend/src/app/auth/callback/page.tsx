'use client'

import { useEffect, useState, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'

function AuthCallbackContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [status, setStatus] = useState<'processing' | 'success' | 'error'>('processing')
  const [message, setMessage] = useState('인증 처리 중...')

  useEffect(() => {
    console.log('🚀 AuthCallback component mounted')
    console.log('🔍 Current URL:', window.location.href)
    console.log('🔍 Search params:', searchParams?.toString())
    
    // Check if there's token data in URL params or handle OAuth success response
    const token = searchParams?.get('token')
    const error = searchParams?.get('error')
    
    console.log('🔑 Token from URL:', token ? 'Found: ' + token.substring(0, 20) + '...' : 'Not found')
    console.log('❌ Error from URL:', error || 'None')

    if (error) {
      setStatus('error')
      setMessage(`인증 실패: ${error}`)
      return
    }

    if (token) {
      console.log('🔑 Token received in callback:', token.substring(0, 50) + '...')
      
      // Store token and redirect to home
      localStorage.setItem('jwtToken', token)
      console.log('💾 Token stored in localStorage')
      
      // Also set the Authorization header immediately
      if (typeof window !== 'undefined') {
        // Import and use the setAuthToken function
        import('@/lib/api').then(({ setAuthToken }) => {
          setAuthToken(token)
          console.log('🔐 Authorization header set')
        })
      }
      
      setStatus('success')
      setMessage('로그인 성공! 홈페이지로 이동합니다...')
      
      // Force a page reload to trigger useAuth to pick up the new token
      setTimeout(() => {
        console.log('🔄 Redirecting to home page...')
        window.location.href = '/'
      }, 1500)
    } else {
      // If no token in URL, this might be the OAuth redirect from backend
      // The backend should have handled the token already
      setStatus('success')
      setMessage('로그인 성공! 홈페이지로 이동합니다...')
      
      setTimeout(() => {
        window.location.href = '/'
      }, 1500)
    }
  }, [router, searchParams])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white rounded-lg shadow-md p-8 text-center">
        <div className="mb-6">
          {status === 'processing' && (
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          )}
          {status === 'success' && (
            <div className="text-green-500 text-6xl mb-4">✅</div>
          )}
          {status === 'error' && (
            <div className="text-red-500 text-6xl mb-4">❌</div>
          )}
        </div>

        <h1 className="text-xl font-semibold mb-4 text-gray-900">
          Google 로그인
        </h1>

        <p className="text-gray-600 mb-6">
          {message}
        </p>

        {status === 'error' && (
          <button
            onClick={() => router.push('/')}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            홈으로 돌아가기
          </button>
        )}
      </div>
    </div>
  )
}

export default function AuthCallback() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    }>
      <AuthCallbackContent />
    </Suspense>
  )
}