'use client'

import { useAuth } from '@/hooks/useAuth'

interface SidebarProps {
  isMobileOpen: boolean
  setIsMobileOpen: (open: boolean) => void
}

export default function Sidebar({ isMobileOpen, setIsMobileOpen }: SidebarProps) {
  const { user, isAuthenticated } = useAuth()

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
        <div className="p-4 border-b bg-gray-50">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-gray-800">
                Navigation
              </h2>
              <p className="text-xs text-gray-600">Menu</p>
            </div>
            {/* Mobile close button */}
            <button
              onClick={() => setIsMobileOpen(false)}
              className="lg:hidden p-1.5 rounded-lg hover:bg-gray-200 transition-colors"
              aria-label="Close sidebar"
            >
              <svg
                className="w-4 h-4 text-gray-600"
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
        </div>

        {/* Navigation Menu */}
        <nav className="flex-1 p-4 space-y-3 overflow-y-auto">
          {/* 1. POINTS Section */}
          <div className="space-y-1">
            <h3 className="font-medium text-orange-600 text-sm px-3 py-2 border-b border-orange-100">
              🪙 포인트 (Points)
            </h3>
            
            {/* Point Earning */}
            <div className="ml-2 space-y-1">
              <p className="text-xs text-gray-500 px-3 py-1">포인트 받기</p>
              <a 
                href="#earn-sub-10" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">🎁</span>
                <span>서브포인트 +10 받기</span>
              </a>
              <a 
                href="#earn-sub-50" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">🎁</span>
                <span>서브포인트 +50 받기</span>
              </a>
              <a 
                href="#earn-main-5" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">⭐</span>
                <span>메인포인트 +5 받기</span>
              </a>
            </div>

            {/* Point Conversion */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs text-gray-500 px-3 py-1">포인트 변환</p>
              <a 
                href="#convert-sub-main" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">🔄</span>
                <span>서브 → 메인 변환</span>
              </a>
            </div>

            {/* Balance Display */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs text-gray-500 px-3 py-1">내 잔액</p>
              <a 
                href="#point-balance" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-orange-50 hover:text-orange-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">💰</span>
                <span>포인트 잔액 확인</span>
              </a>
            </div>
          </div>

          {/* 2. TOKEN Section */}
          <div className="space-y-1">
            <h3 className="font-medium text-green-600 text-sm px-3 py-2 border-b border-green-100">
              🪙 토큰 (Token)
            </h3>
            
            {/* Token Exchange */}
            <div className="ml-2 space-y-1">
              <p className="text-xs text-gray-500 px-3 py-1">토큰 교환 (가스리스)</p>
              <a 
                href="#exchange-main-token" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">⚡</span>
                <span>메인포인트 → 거버넌스토큰</span>
              </a>
            </div>

            {/* Token Balance */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs text-gray-500 px-3 py-1">토큰 잔액</p>
              <a 
                href="#token-balance" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">💎</span>
                <span>거버넌스토큰 잔액</span>
              </a>
            </div>

            {/* Wallet Info */}
            <div className="ml-2 space-y-1 pt-2">
              <p className="text-xs text-gray-500 px-3 py-1">지갑 정보</p>
              <a 
                href="#wallet-info" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-base mr-3">💳</span>
                <span>AA 지갑 주소</span>
              </a>
            </div>
          </div>

          {/* 3. VOTE Section */}
          {isAuthenticated && user && (
            <div className="space-y-1">
              <h3 className="font-medium text-purple-600 text-sm px-3 py-2 border-b border-purple-100">
                🗳️ 투표 (Vote)
              </h3>
              
              {/* Voting Power */}
              <div className="ml-2 space-y-1">
                <p className="text-xs text-gray-500 px-3 py-1">투표 권한</p>
                <a 
                  href="#voting-power" 
                  onClick={() => setIsMobileOpen(false)}
                  className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
                >
                  <span className="text-base mr-3">⚡</span>
                  <span>투표 권한 확인</span>
                </a>
              </div>

              {/* Proposals */}
              <div className="ml-2 space-y-1 pt-2">
                <p className="text-xs text-gray-500 px-3 py-1">거버넌스 제안</p>
                <a 
                  href="#proposals" 
                  onClick={() => setIsMobileOpen(false)}
                  className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
                >
                  <span className="text-base mr-3">📋</span>
                  <span>제안 목록</span>
                </a>
              </div>

              {/* Voting Buttons */}
              <div className="ml-2 space-y-1 pt-2">
                <p className="text-xs text-gray-500 px-3 py-1">투표 참여 (가스리스)</p>
                <a 
                  href="#vote-buttons" 
                  onClick={() => setIsMobileOpen(false)}
                  className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
                >
                  <span className="text-base mr-3">✅</span>
                  <span>찬성/반대 투표</span>
                </a>
              </div>

              {/* Create Proposal (Admin only) */}
              {user.id.includes('admin') && (
                <div className="ml-2 space-y-1 pt-2">
                  <p className="text-xs text-gray-500 px-3 py-1">관리자 기능</p>
                  <a 
                    href="#create-proposal" 
                    onClick={() => setIsMobileOpen(false)}
                    className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
                  >
                    <span className="text-base mr-3">➕</span>
                    <span>제안 생성</span>
                  </a>
                </div>
              )}
            </div>
          )}

          {/* System Status */}
          <div className="space-y-1 pt-4 border-t">
            <h3 className="font-medium text-gray-500 text-xs uppercase tracking-wider px-3 py-2">
              시스템 상태
            </h3>
            
            <a 
              href="#status" 
              onClick={() => setIsMobileOpen(false)}
              className="flex items-center px-3 py-2 rounded-lg hover:bg-gray-50 hover:text-gray-700 transition-colors text-sm"
            >
              <span className="text-base mr-3">📊</span>
              <span>시스템 상태</span>
            </a>
          </div>
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
                <p className="text-sm font-medium text-gray-800 truncate">
                  {user.name}
                </p>
                <div className="flex items-center mt-1">
                  <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${
                    user.id.includes('admin') ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'
                  }`}>
                    {user.id.includes('admin') ? '관리자' : '사용자'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  )
}
