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
      setError('Failed to receive Google authentication information')
      return
    }

    setIsLoading(true)
    setError('')

    try {
      const { user } = await authAPI.googleLogin(response.credential)
      onAuthSuccess(user)
    } catch (err: any) {
      console.error('Google login failed:', err)
      setError(err.response?.data?.message || 'Login failed')
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
          <p className="mt-2 text-sm">Logging in...</p>
        </div>
      )}

      {/* Terms and Privacy Notice */}
      <p className="text-xs mt-4 text-center leading-relaxed">
        By logging in, you agree to the <span className="text-blue-600 hover:underline cursor-pointer">Terms of Service</span> and{' '}
        <span className="text-blue-600 hover:underline cursor-pointer">Privacy Policy</span>.
      </p>
    </div>
  )
}