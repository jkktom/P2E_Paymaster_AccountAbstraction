'use client'

import { useState } from 'react'
import { useAuth } from '@/hooks/useAuth'

declare global {
  interface Window {
    google: any
  }
}

interface HeaderProps {
  isMobileOpen: boolean
  setIsMobileOpen: (open: boolean) => void
}

export default function Header({ isMobileOpen, setIsMobileOpen }: HeaderProps) {
  const { user, isAuthenticated, signIn, demoSignIn, signOut, isLoading, error } = useAuth()
  const [showAuthModal, setShowAuthModal] = useState(false)
  const [authError, setAuthError] = useState<string>('')

  // Handle Google OAuth response
  const handleGoogleResponse = async (response: any) => {
    if (!response.credential) {
      setAuthError('No credential received from Google')
      return
    }

    setAuthError('')
    try {
      await signIn(response.credential)
      setShowAuthModal(false)
    } catch (err: any) {
      setAuthError(err.response?.data?.message || 'Login failed')
    }
  }

  // Demo login
  const handleDemoLogin = async (userType: 'admin' | 'user') => {
    setAuthError('')
    try {
      await demoSignIn(userType)
      setShowAuthModal(false)
    } catch (err: any) {
      setAuthError(err.response?.data?.message || 'Demo login failed')
    }
  }

  // Initialize Google Sign-In when modal opens
  const initializeGoogleSignIn = () => {
    if (typeof window !== 'undefined' && window.google && showAuthModal) {
      setTimeout(() => {
        window.google.accounts.id.initialize({
          client_id: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
          callback: handleGoogleResponse,
        })

        const buttonElement = document.getElementById('google-signin-button')
        if (buttonElement) {
          window.google.accounts.id.renderButton(buttonElement, {
            theme: 'outline',
            size: 'large',
            width: 250,
          })
        }
      }, 100)
    }
  }

  // Load Google Sign-In script
  useState(() => {
    if (typeof window !== 'undefined' && !window.google) {
      const script = document.createElement('script')
      script.src = 'https://accounts.google.com/gsi/client'
      script.async = true
      script.onload = initializeGoogleSignIn
      document.body.appendChild(script)
    }
  })

  // Re-initialize when modal opens
  useState(() => {
    if (showAuthModal) {
      initializeGoogleSignIn()
    }
  })

  return (
    <>
      <header className="bg-white shadow-sm border-b sticky top-0 z-40">
        <div className="px-4 py-3 flex justify-between items-center">
          {/* Left side with hamburger menu and title */}
          <div className="flex items-center space-x-3">
            {/* Hamburger Menu Button */}
            <button
              onClick={() => setIsMobileOpen(!isMobileOpen)}
              className="lg:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors"
              aria-label="Toggle mobile menu"
            >
              <svg
                className="w-5 h-5 text-gray-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                {isMobileOpen ? (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                ) : (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                )}
              </svg>
            </button>

            {/* Title - Responsive */}
            <div className="min-w-0 flex-1">
              <h1 className="text-xl lg:text-2xl font-bold text-gray-800 truncate">
                <span className="hidden sm:inline">블루밍 블록체인 서비스</span>
                <span className="sm:hidden">블루밍</span>
              </h1>
              <p className="text-xs lg:text-sm text-gray-600 hidden sm:block">
                zkSync Account Abstraction POC
              </p>
            </div>
          </div>
          {/* Right side content */}
          <div className="flex items-center space-x-6">
            {/* Status Bar */}
            <div className="hidden lg:flex items-center space-x-6 text-sm">
              {/* Main Points */}
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-gray-400 rounded-full flex items-center justify-center">
                  <span className="text-white text-xs font-bold">M</span>
                </div>
                <span className="text-gray-600">Main:</span>
                <span className="font-semibold text-gray-800">[]</span>
              </div>

              {/* Sub Points */}
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-gray-400 rounded-full flex items-center justify-center">
                  <span className="text-white text-xs font-bold">S</span>
                </div>
                <span className="text-gray-600">Sub:</span>
                <span className="font-semibold text-gray-800">[]</span>
              </div>

              {/* Token */}
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-gray-400 rounded-full flex items-center justify-center">
                  <span className="text-white text-xs font-bold">T</span>
                </div>
                <span className="text-gray-600">Token:</span>
                <span className="font-semibold text-gray-800">[]</span>
              </div>
            </div>

            {/* Auth Section */}
            <div className="flex items-center space-x-4">
              {isLoading ? (
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
              ) : isAuthenticated && user ? (
                <div className="flex items-center space-x-3">
                  <div className="text-right">
                    <p className="font-medium text-gray-800 text-sm">{user.name}</p>
                    <span
                      className={`inline-block px-2 py-1 rounded-full text-xs ${
                        user.id.includes('admin')
                          ? 'bg-purple-100 text-purple-800'
                          : 'bg-blue-100 text-blue-800'
                      }`}
                    >
                      {user.id.includes('admin') ? 'Admin' : 'User'}
                    </span>
                  </div>
                  <button
                    onClick={signOut}
                    className="px-3 py-2 bg-gray-600 text-white rounded hover:bg-gray-700 text-sm"
                  >
                    Sign Out
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setShowAuthModal(true)}
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                  Sign In
                </button>
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Auth Modal */}
      {showAuthModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">Sign In</h2>
              <button
                onClick={() => {
                  setShowAuthModal(false)
                  setAuthError('')
                }}
                className="text-gray-500 hover:text-gray-700"
              >
                ✕
              </button>
            </div>

            {authError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
                {authError}
              </div>
            )}

            {/* Google Sign-In Button */}
            <div className="flex flex-col items-center space-y-4">
              <div id="google-signin-button"></div>
              
              {/* Demo Login Buttons */}
              <div className="mt-6 pt-4 border-t border-gray-200 w-full">
                <p className="text-sm text-gray-500 mb-3 text-center">
                  Demo Login (for interview):
                </p>
                <div className="flex space-x-3">
                  <button
                    onClick={() => handleDemoLogin('user')}
                    disabled={isLoading}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                  >
                    Demo User
                  </button>
                  <button
                    onClick={() => handleDemoLogin('admin')}
                    disabled={isLoading}
                    className="flex-1 px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:opacity-50"
                  >
                    Demo Admin
                  </button>
                </div>
              </div>
            </div>

            {isLoading && (
              <div className="text-center mt-4">
                <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
                <p className="mt-2 text-sm text-gray-600">Signing in...</p>
              </div>
            )}
          </div>
        </div>
      )}
    </>
  )
}