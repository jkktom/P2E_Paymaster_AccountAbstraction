'use client'

import { useAuth } from '@/hooks/useAuth'
import { formatTokenBalance } from '@/utils/tokenUtils'

export default function Header() {
  const { user, balance, isAuthenticated, signOut, isLoading } = useAuth()

  return (
    <header className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 py-3 flex justify-between items-center">
        {/* Left side with title */}
        <div className="flex items-center space-x-3">
          {/* Title - Responsive */}
          <div className="min-w-0 flex-1">
            <h1 className="text-xl lg:text-2xl font-semibold">
              블루밍 블록체인 서비스
            </h1>
            <p className="text-xs lg:text-sm text-gray-600 hidden sm:block">
              zkSync Account Abstraction 기반 포인트 및 토큰 관리 서비스
            </p>
          </div>
        </div>

        {/* Right side - Points Display (always visible) and Auth */}
        <div className="flex items-center space-x-4">
          {/* Points Display - Always visible */}
          <div className="hidden md:flex items-center space-x-3 text-sm bg-gray-50 px-4 py-2 rounded-lg">
            <div className="flex items-center space-x-2">
              <span className="text-blue-600 font-medium">메인:</span>
              <span className="font-bold text-blue-800">{isAuthenticated ? (balance?.mainPoint || 0) : '-'}</span>
            </div>
            <div className="text-gray-300">|</div>
            <div className="flex items-center space-x-2">
              <span className="text-green-600 font-medium">서브:</span>
              <span className="font-bold text-green-800">{isAuthenticated ? (balance?.subPoint || 0) : '-'}</span>
            </div>
            <div className="text-gray-300">|</div>
            <div className="flex items-center space-x-2">
              <span className="text-purple-600 font-medium">토큰:</span>
              <span className="font-bold text-purple-800">{isAuthenticated ? formatTokenBalance(balance?.tokenBalance || 0) : '-'}</span>
            </div>
          </div>
          
          {isLoading ? (
            <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
          ) : isAuthenticated && user ? (
            <div className="flex items-center space-x-3">
              {/* User Profile */}
              {user.avatar ? (
                <img 
                  src={user.avatar} 
                  alt="Profile" 
                  className="w-8 h-8 rounded-full"
                />
              ) : (
                <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center">
                  <span className="text-sm font-bold text-white">
                    {user.name.charAt(0).toUpperCase()}
                  </span>
                </div>
              )}
              <div className="text-right">
                <p className="font-medium text-sm">{user.name}</p>
                <div className="flex items-center space-x-2">
                  <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${
                    user.roleId === 1 ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'
                  }`}>
                    {user.roleId === 1 ? '관리자' : '사용자'}
                  </span>
                  {user.smartWalletAddress && (
                    <span className="text-xs text-gray-500 font-mono">
                      {user.smartWalletAddress.slice(0, 6)}...{user.smartWalletAddress.slice(-4)}
                    </span>
                  )}
                </div>
              </div>
              <button
                onClick={signOut}
                className="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 text-sm font-medium transition-colors"
              >
                로그아웃
              </button>
            </div>
          ) : (
            <button
              onClick={() => {
                // Scroll to hero section for sign-in
                document.getElementById('hero')?.scrollIntoView({ behavior: 'smooth' })
              }}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors"
            >
              로그인
            </button>
          )}
        </div>
      </div>
    </header>
  )
}