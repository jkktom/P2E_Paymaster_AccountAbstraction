'use client'

import { useState, useEffect } from 'react'
import { authAPI } from '@/lib/api'
import type { User } from '@/types'

// Google OAuth Configuration
const GOOGLE_CLIENT_ID = '1041570512238-c1d24j95hjctqmd9k9i291o2evvemi5j.apps.googleusercontent.com'

interface GoogleAuthProps {
  onAuthSuccess: (user: User) => void
}

declare global {
  interface Window {
    google: any
  }
}

export default function GoogleAuth({ onAuthSuccess }: GoogleAuthProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string>('')

  // Handle Google OAuth response
  const handleGoogleResponse = async (response: any) => {
    if (!response.credential) {
      setError('구글 인증 정보를 받지 못했습니다')
      return
    }

    setIsLoading(true)
    setError('')

    try {
      const { user } = await authAPI.googleLogin(response.credential)
      onAuthSuccess(user)
    } catch (err: any) {
      console.error('Google 로그인 실패:', err)
      setError(err.response?.data?.message || '로그인에 실패했습니다')
    } finally {
      setIsLoading(false)
    }
  }

  // Initialize Google Sign-In
  useEffect(() => {
    if (typeof window !== 'undefined' && !window.google) {
      const script = document.createElement('script')
      script.src = 'https://accounts.google.com/gsi/client'
      script.async = true
      script.onload = initializeGoogleSignIn
      document.body.appendChild(script)
    } else if (window.google) {
      initializeGoogleSignIn()
    }
  }, [])

  const initializeGoogleSignIn = () => {
    if (typeof window !== 'undefined' && window.google) {
      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: handleGoogleResponse,
        auto_select: false,
        cancel_on_tap_outside: true,
      })

      window.google.accounts.id.renderButton(
        document.getElementById('google-signin-button'),
        {
          theme: 'outline',
          size: 'large',
          width: 280,
          text: 'signin_with',
          shape: 'rectangular',
        }
      )
    }
  }

  return (
    <div className="w-full">
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm text-center">
          {error}
        </div>
      )}

      {/* Google Sign-In Button */}
      <div className="flex justify-center">
        <div id="google-signin-button"></div>
      </div>

      {isLoading && (
        <div className="mt-4 text-center">
          <div className="inline-block animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-sm">로그인 중...</p>
        </div>
      )}

      {/* Terms and Privacy Notice */}
      <p className="text-xs mt-4 text-center leading-relaxed">
        로그인하면 <span className="text-blue-600 hover:underline cursor-pointer">서비스 이용약관</span>과{' '}
        <span className="text-blue-600 hover:underline cursor-pointer">개인정보처리방침</span>에 동의하는 것으로 간주됩니다.
      </p>
    </div>
  )
}