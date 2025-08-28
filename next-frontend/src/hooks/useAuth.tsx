'use client'

import { useState, useEffect, createContext, useContext, ReactNode } from 'react'
import { authAPI, getStoredToken } from '@/lib/api'
import type { User, UserBalance } from '@/types'

interface AuthContextType {
  user: User | null
  balance: UserBalance | null
  isLoading: boolean
  isAuthenticated: boolean
  signIn: (googleToken: string) => Promise<void>
  demoSignIn: (userType: 'admin' | 'user') => Promise<void>
  signOut: () => void
  refreshBalance: () => Promise<void>
  error: string | null
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [balance, setBalance] = useState<UserBalance | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tokenCheckTrigger, setTokenCheckTrigger] = useState(0)

  useEffect(() => {
    // Check for existing token on mount
    const initAuth = async () => {
      console.log('🔄 Initializing auth...')
      const token = getStoredToken()
      console.log('🔍 Found stored token:', token ? 'Yes' : 'No')
      
      if (token) {
        try {
          console.log('📡 Making API call to get user info...')
          // Validate token by getting current user info (this endpoint exists)
          const response = await authAPI.getCurrentUser()
          console.log('📥 Auth user response:', response)
          // The response has this structure: { success: true, user: {...}, balance: {...} }
          if (response.success && response.user) {
            console.log('✅ User authenticated:', response.user.name)
            setUser(response.user)
            if (response.balance) {
              setBalance(response.balance)
            }
          } else {
            throw new Error('Invalid response format')
          }
        } catch (err) {
          console.error('❌ Token validation failed:', err)
          // Token invalid, clear it
          authAPI.signOut()
        }
      } else {
        console.log('❌ No token found, user not authenticated')
      }
      setIsLoading(false)
    }

    initAuth()
  }, [tokenCheckTrigger]) // Re-run when tokenCheckTrigger changes

  // Listen for localStorage changes (when token is added/removed)
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'jwtToken') {
        console.log('🔄 Token change detected in localStorage')
        setTokenCheckTrigger(prev => prev + 1) // Trigger re-check
      }
    }

    // Listen for storage events from other tabs/windows
    window.addEventListener('storage', handleStorageChange)

    // Also check periodically for same-tab changes
    const interval = setInterval(() => {
      const currentToken = getStoredToken()
      const hasTokenNow = !!currentToken
      const hadTokenBefore = !!user
      
      if (hasTokenNow !== hadTokenBefore) {
        console.log('🔄 Token state change detected')
        setTokenCheckTrigger(prev => prev + 1)
      }
    }, 1000)

    return () => {
      window.removeEventListener('storage', handleStorageChange)
      clearInterval(interval)
    }
  }, [user])

  const signIn = async (googleToken: string) => {
    setIsLoading(true)
    setError(null)
    try {
      const { user: userData } = await authAPI.googleLogin(googleToken)
      setUser(userData)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const demoSignIn = async (userType: 'admin' | 'user') => {
    setIsLoading(true)
    setError(null)
    try {
      const { user: userData } = await authAPI.demoLogin(userType)
      setUser(userData)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Demo login failed')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const signOut = () => {
    authAPI.signOut()
    setUser(null)
    setBalance(null)
    setError(null)
  }

  const refreshBalance = async () => {
    if (user) {
      try {
        const response = await authAPI.getCurrentUser()
        if (response.success && response.balance) {
          setBalance(response.balance)
        }
      } catch (err) {
        console.error('Failed to refresh balance:', err)
      }
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        balance,
        isLoading,
        isAuthenticated: !!user,
        signIn,
        demoSignIn,
        signOut,
        refreshBalance,
        error,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}