'use client'

import { useState, useEffect, createContext, useContext, ReactNode } from 'react'
import { authAPI, getStoredToken } from '@/lib/api'
import type { User } from '@/types'

interface AuthContextType {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  signIn: (googleToken: string) => Promise<void>
  demoSignIn: (userType: 'admin' | 'user') => Promise<void>
  signOut: () => void
  error: string | null
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // Check for existing token on mount
    const initAuth = async () => {
      const token = getStoredToken()
      if (token) {
        try {
          // Validate token by getting user profile
          const userData = await authAPI.getProfile()
          setUser(userData)
        } catch (err) {
          // Token invalid, clear it
          authAPI.signOut()
        }
      }
      setIsLoading(false)
    }

    initAuth()
  }, [])

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
    setError(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        signIn,
        demoSignIn,
        signOut,
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