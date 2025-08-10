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
        <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
          {/* Always visible features */}
          <div className="space-y-1">
            <h3 className="font-medium text-gray-500 text-xs uppercase tracking-wider px-3 py-2">
              Pages
            </h3>
            
            <a 
              href="#hero" 
              onClick={() => setIsMobileOpen(false)}
              className="flex items-center px-3 py-2 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-colors text-sm"
            >
              <span className="text-lg mr-3">🏠</span>
              <span>Home</span>
            </a>

            <a 
              href="#features" 
              onClick={() => setIsMobileOpen(false)}
              className="flex items-center px-3 py-2 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-colors text-sm"
            >
              <span className="text-lg mr-3">⚡</span>
              <span>Features</span>
            </a>

            <a 
              href="#demo" 
              onClick={() => setIsMobileOpen(false)}
              className="flex items-center px-3 py-2 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-colors text-sm"
            >
              <span className="text-lg mr-3">📋</span>
              <span>Demo</span>
            </a>
          </div>

          {/* Authenticated user features */}
          {isAuthenticated && user && (
            <div className="space-y-1 pt-4 border-t">
              <h3 className="font-medium text-gray-500 text-xs uppercase tracking-wider px-3 py-2">
                My Account
              </h3>
              
              <a 
                href="#points" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-lg mr-3">🪙</span>
                <span>Points Manager</span>
              </a>

              <a 
                href="#wallet" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-lg mr-3">💳</span>
                <span>Wallet Connect</span>
              </a>

              <a 
                href="#governance" 
                onClick={() => setIsMobileOpen(false)}
                className="flex items-center px-3 py-2 rounded-lg hover:bg-green-50 hover:text-green-700 transition-colors text-sm"
              >
                <span className="text-lg mr-3">🗳️</span>
                <span>Governance</span>
              </a>
            </div>
          )}

          {/* System Status */}
          <div className="space-y-1 pt-4 border-t">
            <h3 className="font-medium text-gray-500 text-xs uppercase tracking-wider px-3 py-2">
              System
            </h3>
            
            <a 
              href="#status" 
              onClick={() => setIsMobileOpen(false)}
              className="flex items-center px-3 py-2 rounded-lg hover:bg-purple-50 hover:text-purple-700 transition-colors text-sm"
            >
              <span className="text-lg mr-3">📊</span>
              <span>Status</span>
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
