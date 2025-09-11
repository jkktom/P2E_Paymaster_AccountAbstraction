'use client'

import { useAuth } from '@/hooks/useAuth'
import { useRouter } from 'next/navigation'

interface SidebarProps {
  isMobileOpen: boolean
  setIsMobileOpen: (open: boolean) => void
}

export default function Sidebar({ isMobileOpen, setIsMobileOpen }: SidebarProps) {
  const { user, isAuthenticated } = useAuth()
  const router = useRouter()

  const navigateToHome = () => {
    router.push('/')
    setIsMobileOpen(false)
  }

  return (
    <>
      {/* Mobile overlay */}
      {isMobileOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={() => setIsMobileOpen(false)}
        />
      )}
      
      {/* Sidebar */}
      <div className={`
        fixed lg:static top-0 left-0 h-screen lg:h-auto z-30 lg:z-auto
        bg-white shadow-lg border-r transition-transform duration-300 w-64
        ${isMobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        flex flex-col
      `}>
        {/* Sidebar Header */}
        <div className="flex items-center justify-between p-4">
        {/* Mobile close button */}
        <button
            onClick={() => setIsMobileOpen(false)}
            className="lg:hidden p-1.5 rounded-lg hover:bg-gray-200 transition-colors"
            aria-label="Close sidebar"
        >
            <svg
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            >
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
            />
            </svg>
        </button>
        </div>
        

        {/* Navigation Menu */}
        <nav className="flex-1 p-4 space-y-3 overflow-y-auto">
          {/* 1. POINTS Section */}
          <div className="space-y-1">
            <h3 className="font-medium text-orange-600 text-sm px-3 py-2 border-b border-orange-100">
              🪙 Points
            </h3>
            
            {/* Point Earning */}
            <div className="ml-2 space-y-1">
              <p className="text-xs px-3 py-1">Earn Points</p>
              <a 
                href="#earn-sub-10" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">🎁</span>
                <span>Earn Sub Points +10</span>
              </a>
              <a 
                href="#earn-sub-50" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">🎁</span>
                <span>Earn Sub Points +50</span>
              </a>
              <a 
                href="#earn-main-5" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">⭐</span>
                <span>Earn Main Points +5</span>
              </a>
            </div>

            {/* Point Conversion */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs px-3 py-1">Point Conversion</p>
              <a 
                href="#convert-sub-main" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">🔄</span>
                <span>Sub → Main Conversion</span>
              </a>
            </div>

            {/* Balance Display */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs px-3 py-1">My Balance</p>
              <a 
                href="#point-balance" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">💰</span>
                <span>Check Point Balance</span>
              </a>
            </div>
          </div>

          {/* 2. TOKEN Section */}
          <div className="space-y-1">
            <h3 className="font-medium text-green-600 text-sm px-3 py-2 border-b border-green-100">
              🪙 Token
            </h3>
            
            {/* Token Exchange */}
            <div className="ml-2 space-y-1">
              <p className="text-xs px-3 py-1">Token Exchange (Gasless)</p>
              <a 
                href="#exchange-main-token" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">⚡</span>
                <span>Points → Token Exchange</span>
              </a>
            </div>

            {/* Token Balance */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs px-3 py-1">Token Balance</p>
              <a 
                href="#token-balance" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">💎</span>
                <span>Token Holdings</span>
              </a>
            </div>

            {/* Wallet Info */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs px-3 py-1">Wallet Info</p>
              <a 
                href="#wallet-info" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">💳</span>
                <span>Check Wallet Address</span>
              </a>
            </div>
            {/* Vote */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs px-3 py-1">Vote Participation</p>
              <a 
                href="#vote" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">📊</span>
                <span>Participate in Voting</span>
              </a>
            </div>
          </div>

          {/* 3. VOTE Section */}
          {isAuthenticated && user && (
            <div className="space-y-1">
              <h3 className="font-medium text-purple-600 text-sm px-3 py-2 border-b border-purple-100">
                🗳️ Vote
              </h3>
              
              {/* Voting Power */}
              <div className="ml-2 space-y-1">
                <p className="text-xs px-3 py-1">Voting Power</p>
                <a 
                  href="#voting-power" 
                  onClick={() => setIsMobileOpen(false)}
                  className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
                >
                  <span className="text-base mr-3">⚡</span>
                  <span>Check Voting Power</span>
                </a>
              </div>

              {/* Proposals */}
              <div className="ml-2 space-y-1 pt-2">
                <p className="text-xs px-3 py-1">Governance Proposals</p>
                <a 
                  href="#proposals" 
                  onClick={() => setIsMobileOpen(false)}
                  className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
                >
                  <span className="text-base mr-3">📋</span>
                  <span>Proposal List</span>
                </a>
              </div>

              {/* Voting Buttons */}
              <div className="ml-2 space-y-1 pt-2">
                <p className="text-xs px-3 py-1">Vote Participation (Gasless)</p>
                <a 
                  href="#vote-buttons" 
                  onClick={() => setIsMobileOpen(false)}
                  className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
                >
                  <span className="text-base mr-3">✅</span>
                  <span>For/Against Voting</span>
                </a>
              </div>

              {/* Create Proposal (Admin only) */}
              {user.roleId === 1 && (
                <div className="ml-2 space-y-1 pt-2">
                  <p className="text-xs px-3 py-1">Admin Functions</p>
                  <a 
                    href="#create-proposal" 
                    onClick={() => setIsMobileOpen(false)}
                    className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
                  >
                    <span className="text-base mr-3">➕</span>
                    <span>Create Proposal</span>
                  </a>
                </div>
              )}
            </div>
          )}
        </nav>

        {/* User Info at Bottom */}
        {isAuthenticated && user && (
          <div className="mt-auto p-4 border-t bg-gray-50">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center flex-shrink-0">
                <span className="text-sm font-bold text-white">
                  {user.name.charAt(0).toUpperCase()}
                </span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">
                  {user.name}
                </p>
                <div className="flex items-center mt-1">
                  <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${
                    user.roleId === 1 ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'
                  }`}>
                    {user.roleId === 1 ? 'Admin' : 'User'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {!isAuthenticated && (
          <div className="mt-auto p-4 border-t bg-gray-50 text-center">
            <p className="text-sm text-gray-600 mb-3">Login required</p>
            <button
              onClick={navigateToHome}
              className="text-sm text-blue-600 hover:text-blue-800 underline"
            >
              Go to Login Page
            </button>
          </div>
        )}
      </div>
    </>
  )
}
