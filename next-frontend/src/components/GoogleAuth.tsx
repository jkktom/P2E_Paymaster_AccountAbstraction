'use client'

import { useState } from 'react'
import { authAPI } from '@/lib/api'
import type { User } from '@/types'

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
      setError('No credential received from Google')
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

  // Demo login for interview presentation
  const handleDemoLogin = async (userType: 'admin' | 'user') => {
    setIsLoading(true)
    setError('')

    try {
      const { user } = await authAPI.demoLogin(userType)
      onAuthSuccess(user)
    } catch (err: any) {
      console.error('Demo login failed:', err)
      setError(err.response?.data?.message || 'Demo login failed')
    } finally {
      setIsLoading(false)
    }
  }

  // Initialize Google Sign-In
  const initializeGoogleSignIn = () => {
    if (typeof window !== 'undefined' && window.google) {
      window.google.accounts.id.initialize({
        client_id: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
        callback: handleGoogleResponse,
      })

      window.google.accounts.id.renderButton(
        document.getElementById('google-signin-button'),
        {
          theme: 'outline',
          size: 'large',
          width: 300,
        }
      )
    }
  }

  // Load Google Sign-In script
  useState(() => {
    if (typeof window !== 'undefined') {
      const script = document.createElement('script')
      script.src = 'https://accounts.google.com/gsi/client'
      script.async = true
      script.onload = initializeGoogleSignIn
      document.body.appendChild(script)
    }
  })

  return (
    <div className="flex flex-col items-center space-y-4 p-8 bg-white rounded-lg shadow-md">
      <h2 className="text-2xl font-bold text-gray-800">
        Blooming Blockchain Service
      </h2>
      <p className="text-gray-600 text-center">
        Sign in to manage your points and governance tokens
      </p>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
          {error}
        </div>
      )}

      {/* Google Sign-In Button */}
      <div className="flex flex-col items-center space-y-4">
        <div id="google-signin-button"></div>
        
        {/* Demo Login Buttons for Interview */}
        <div className="mt-6 pt-4 border-t border-gray-200">
          <p className="text-sm text-gray-500 mb-3 text-center">
            Demo Login (for interview presentation):
          </p>
          <div className="flex space-x-3">
            <button
              onClick={() => handleDemoLogin('user')}
              disabled={isLoading}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              Demo User
            </button>
            <button
              onClick={() => handleDemoLogin('admin')}
              disabled={isLoading}
              className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50"
            >
              Demo Admin
            </button>
          </div>
        </div>
      </div>

      {isLoading && (
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-sm text-gray-600">Signing in...</p>
        </div>
      )}
    </div>
  )
}