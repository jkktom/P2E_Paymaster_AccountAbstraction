'use client'

import { useAuth } from '@/hooks/useAuth'
import { formatTokenBalance } from '@/utils/tokenUtils'

interface HeaderProps {
  isMobileOpen: boolean
  setIsMobileOpen: (open: boolean) => void
}

export default function Header({ isMobileOpen, setIsMobileOpen }: HeaderProps) {
  const { user, balance, isAuthenticated, signOut, isLoading } = useAuth()

  return (
    <header className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-40">
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
              className="w-5 h-5"
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
            <h1 className="text-xl lg:text-2xl font-semibold truncate">
              <span className="hidden sm:inline">블루밍 블록체인 서비스</span>
              <span className="sm:hidden">블루밍</span>
            </h1>
            <p className="text-xs lg:text-sm text-facebook-primary hidden sm:block">
              zkSync Account Abstraction POC
            </p>
          </div>
        </div>

        {/* Right side - Points Display or Auth */}
        <div className="flex items-center space-x-4">
          {isLoading ? (
            <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
          ) : isAuthenticated && user ? (
            <div className="flex items-center space-x-4">
              {/* Points Display */}
              <div className="hidden md:flex items-center space-x-3 text-sm">
                <div className="flex items-center space-x-2">
                  <span className="text-facebook-primary">메인:</span>
                  <span className="font-medium">{balance?.mainPoint || 0}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-facebook-primary">서브:</span>
                  <span className="font-medium">{balance?.subPoint || 0}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-facebook-primary">토큰:</span>
                  <span className="font-medium">{formatTokenBalance(balance?.tokenBalance || 0)}</span>
                </div>
              </div>
              
              {/* User Profile */}
              <div className="flex items-center space-x-3">
                <div className="text-right">
                  <p className="font-medium text-sm">{user.name}</p>
                  <div className="flex flex-col space-y-1">
                    <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${
                      user.roleId === 1 ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'
                    }`}>
                      {user.roleId === 1 ? '관리자' : '사용자'}
                    </span>
                    {user.smartWalletAddress && (
                      <span className="text-xs text-gray-600 font-mono">
                        {user.smartWalletAddress.slice(0, 6)}...{user.smartWalletAddress.slice(-4)}
                      </span>
                    )}
                  </div>
                </div>
                <button
                  onClick={signOut}
                  className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 text-sm font-medium transition-colors"
                >
                  로그아웃
                </button>
              </div>
            </div>
          ) : (
            <button
              onClick={() => {
                // Scroll to hero section for sign-in
                document.getElementById('hero')?.scrollIntoView({ behavior: 'smooth' })
              }}
              className="btn-facebook"
            >
              로그인
            </button>
          )}
        </div>
      </div>
    </header>
  )
}