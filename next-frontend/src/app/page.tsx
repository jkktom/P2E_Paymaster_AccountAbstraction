'use client'

import { useState } from 'react'
import { useAuth } from '@/hooks/useAuth'
import GoogleAuth from '@/components/GoogleAuthBackend'
import PointsManager from '@/components/PointsManager'
import WalletConnect from '@/components/WalletConnect'
import GovernanceVoting from '@/components/GovernanceVoting'
import type { WalletState } from '@/types'

export default function Home() {
  const { user, isAuthenticated, signOut } = useAuth()
  const [wallet, setWallet] = useState<WalletState>({ isConnected: false })

  const handleWalletChange = (walletState: WalletState) => {
    setWallet(walletState)
  }

  const handleAuthSuccess = (user: any) => {
    // This will be handled by the useAuth hook automatically
    console.log('Authentication successful:', user)
  }

  return (
    <div className="px-6 py-8">
      {/* Hero Section with Facebook-style Auth */}
      <section className="text-center mb-16" id="hero">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-5xl font-semibold text-gray-900 mb-6 leading-tight">
            블루밍 블록체인 서비스
          </h1>
          <p className="text-xl text-facebook-primary mb-12 max-w-3xl mx-auto leading-relaxed">
            zkSync 기술로 구현된 가스리스 거래와 거버넌스 투표를 경험해보세요.
            <br />
            계정 추상화로 더 간단하고 안전한 블록체인 서비스를 제공합니다.
          </p>
          
          {!isAuthenticated ? (
            <div className="card-facebook p-8 max-w-md mx-auto">
              <h2 className="text-2xl font-semibold text-gray-900 mb-6">
                서비스 시작하기
              </h2>
              <p className="text-facebook-primary mb-8 text-sm leading-relaxed">
                구글 계정으로 간편하게 로그인하고 포인트를 적립하고, 
                거버넌스 토큰을 교환해보세요.
              </p>
              
              {/* Google Auth Component */}
              <GoogleAuth onAuthSuccess={handleAuthSuccess} />
            </div>
          ) : (
            <div className="bg-blue-50 border border-blue-200 rounded-xl p-6 max-w-2xl mx-auto">
              <h3 className="text-lg font-semibold text-blue-800 mb-4">
                환영합니다! 🎉
              </h3>
              
              {/* User Info Display */}
              <div className="mb-4 p-4 bg-white rounded-lg border">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    {user?.avatar && (
                      <img 
                        src={user.avatar} 
                        alt="Profile" 
                        className="w-10 h-10 rounded-full"
                      />
                    )}
                    <div>
                      <p className="text-gray-800 font-medium">{user?.name}</p>
                      <p className="text-gray-600 text-sm">{user?.email}</p>
                      <p className="text-gray-500 text-xs">ID: {user?.googleId}</p>
                    </div>
                  </div>
                  <button
                    onClick={signOut}
                    className="px-3 py-1 text-sm bg-red-100 text-red-600 rounded hover:bg-red-200 transition-colors"
                  >
                    로그아웃
                  </button>
                </div>
              </div>
              
              <p className="text-blue-600 text-sm">
                이제 포인트 적립, 토큰 교환, 거버넌스 투표 등 모든 기능을 이용할 수 있습니다.
              </p>
            </div>
          )}
        </div>
      </section>

      {/* Features Overview - Always Visible */}
      <section className="mb-12" id="features">
        <h3 className="text-2xl font-bold text-gray-800 mb-6 text-center">
          Platform Features
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">🪙</div>
            <h4 className="text-xl font-semibold mb-2">Points System</h4>
            <p className="text-gray-600">
              Earn sub points through activities, convert to main points, 
              and exchange for governance tokens at a 10:1 ratio.
            </p>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">⚡</div>
            <h4 className="text-xl font-semibold mb-2">Gasless Transactions</h4>
            <p className="text-gray-600">
              All blockchain transactions are sponsored via zkSync paymaster. 
              No ETH needed for gas fees.
            </p>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">🗳️</div>
            <h4 className="text-xl font-semibold mb-2">Governance Voting</h4>
            <p className="text-gray-600">
              Participate in decentralized governance with your BLOOM tokens. 
              Create proposals and vote on important decisions.
            </p>
          </div>
        </div>
      </section>

      {/* Main Application - Conditional Content */}
      {isAuthenticated && user ? (
        <>
          {/* POINTS Section */}
          <section className="mb-12">
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
              {/* Point Balance & Points Manager */}
              <div className="space-y-6">
                <div className="bg-white rounded-lg shadow-md p-6" id="point-balance">
                  <h3 className="text-lg font-semibold text-orange-600 mb-4">💰 내 포인트 잔액</h3>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="bg-blue-50 p-4 rounded-lg text-center">
                      <p className="text-sm text-blue-600 mb-1">서브포인트</p>
                      <p className="text-2xl font-bold text-blue-700">0</p>
                    </div>
                    <div className="bg-green-50 p-4 rounded-lg text-center">
                      <p className="text-sm text-green-600 mb-1">메인포인트</p>
                      <p className="text-2xl font-bold text-green-700">0</p>
                    </div>
                  </div>
                </div>

                <div id="points">
                  <PointsManager user={user} />
                </div>
              </div>

              {/* Token Exchange */}
              <div className="space-y-6">
                <div className="bg-white rounded-lg shadow-md p-6" id="exchange-main-token">
                  <h3 className="text-lg font-semibold text-green-600 mb-4">⚡ 토큰 교환 (가스리스)</h3>
                  <div className="p-4 bg-green-50 rounded-lg">
                    <h4 className="font-medium mb-2">메인포인트 → 거버넌스토큰</h4>
                    <p className="text-sm text-gray-600 mb-3">10 메인포인트 = 1 거버넌스토큰 (가스비 무료!)</p>
                    <p className="text-xs text-gray-500 mb-3">포인트 매니저에서 토큰 교환을 진행하세요</p>
                  </div>
                </div>

                <div className="bg-white rounded-lg shadow-md p-6" id="wallet-info">
                  <h3 className="text-lg font-semibold text-green-600 mb-4">💳 AA 지갑 주소</h3>
                  {user.smartWalletAddress ? (
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <p className="text-sm text-gray-600 mb-2">Smart Wallet Address:</p>
                      <p className="font-mono text-sm bg-white p-3 rounded border break-all">
                        {user.smartWalletAddress}
                      </p>
                      <p className="text-xs text-gray-500 mt-2">
                        zkSync Account Abstraction으로 생성된 지갑입니다. 모든 거래가 가스리스로 처리됩니다.
                      </p>
                    </div>
                  ) : (
                    <p className="text-gray-500">지갑 생성 중...</p>
                  )}
                </div>
              </div>
            </div>
          </section>

          {/* TOKEN Section */}
          <section className="mb-12">
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
              {/* Token Balance */}
              <div className="space-y-6">
                <div className="bg-white rounded-lg shadow-md p-6" id="token-balance">
                  <h3 className="text-lg font-semibold text-green-600 mb-4">💎 거버넌스토큰 잔액</h3>
                  <div className="bg-gradient-to-r from-green-50 to-blue-50 p-6 rounded-lg text-center">
                    <p className="text-sm text-gray-600 mb-2">보유 중인 거버넌스토큰</p>
                    <p className="text-3xl font-bold text-green-700">0 BLOOM</p>
                    <p className="text-xs text-gray-500 mt-2">토큰을 보유하면 가스리스 투표가 가능합니다</p>
                  </div>
                </div>

                <WalletConnect onWalletChange={handleWalletChange} />
              </div>

              {/* Voting Power & Proposals */}
              <div className="space-y-6">
                <div className="bg-white rounded-lg shadow-md p-6" id="voting-power">
                  <h3 className="text-lg font-semibold text-purple-600 mb-4">⚡ 투표 권한 확인</h3>
                  <div className="p-4 bg-purple-50 rounded-lg">
                    <p className="text-sm text-purple-700 mb-2">현재 투표 권한</p>
                    <p className="text-2xl font-bold text-purple-800">0 BLOOM</p>
                    <p className="text-xs text-gray-600 mt-2">거버넌스토큰을 보유하면 가스리스 투표가 가능합니다</p>
                  </div>
                </div>

                <div className="bg-white rounded-lg shadow-md p-6" id="proposals">
                  <h3 className="text-lg font-semibold text-purple-600 mb-4">📋 제안 목록</h3>
                  <div className="space-y-3">
                    <div className="p-4 bg-gray-50 rounded-lg">
                      <p className="text-sm text-gray-600">현재 활성화된 제안이 없습니다.</p>
                      <p className="text-xs text-gray-500 mt-1">관리자가 제안을 생성하면 여기에 표시됩니다.</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Voting Interface */}
              <div className="space-y-6">
                <div className="bg-white rounded-lg shadow-md p-6" id="vote-buttons">
                  <h3 className="text-lg font-semibold text-purple-600 mb-4">✅ 찬성/반대 투표 (가스리스)</h3>
                  <div className="p-4 bg-purple-50 rounded-lg text-center">
                    <p className="text-sm text-gray-600 mb-4">투표하려면 거버넌스토큰이 필요합니다</p>
                    <div className="flex gap-3">
                      <button 
                        disabled 
                        className="flex-1 px-4 py-2 bg-green-300 text-white rounded-lg opacity-50 cursor-not-allowed"
                      >
                        찬성 (무료) ⚡
                      </button>
                      <button 
                        disabled 
                        className="flex-1 px-4 py-2 bg-red-300 text-white rounded-lg opacity-50 cursor-not-allowed"
                      >
                        반대 (무료) ⚡
                      </button>
                    </div>
                  </div>
                </div>

                {user.roleId === 1 && (
                  <div className="bg-white rounded-lg shadow-md p-6" id="create-proposal">
                    <h3 className="text-lg font-semibold text-purple-600 mb-4">➕ 제안 생성 (관리자)</h3>
                    <div className="space-y-3">
                      <input
                        type="text"
                        placeholder="제안 내용을 입력하세요..."
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                      />
                      <button className="w-full px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors">
                        거버넌스 제안 생성
                      </button>
                    </div>
                  </div>
                )}

                <div id="governance">
                  <GovernanceVoting user={user} wallet={wallet} />
                </div>
              </div>
            </div>
          </section>
        </>
      ) : (
        /* Public Demo Content */
        <section className="mb-12" id="demo">
          <div className="bg-white rounded-lg shadow-md p-8">
            <h3 className="text-2xl font-semibold text-center mb-6">
              System Architecture Demo
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div>
                <h4 className="text-lg font-semibold mb-3 text-blue-700">Backend (Spring Boot)</h4>
                <ul className="text-sm text-gray-600 space-y-2">
                  <li>• Google OAuth 2.0 authentication</li>
                  <li>• JWT token management</li>
                  <li>• H2/PostgreSQL database with JPA</li>
                  <li>• Point system with transaction logging</li>
                  <li>• Role-based access control (Admin/User)</li>
                  <li>• RESTful APIs for all operations</li>
                </ul>
              </div>
              <div>
                <h4 className="text-lg font-semibold mb-3 text-green-700">Blockchain (zkSync)</h4>
                <ul className="text-sm text-gray-600 space-y-2">
                  <li>• ERC20 governance token with voting</li>
                  <li>• Account Abstraction with paymaster</li>
                  <li>• Gasless transactions for all users</li>
                  <li>• zkSync Sepolia testnet deployment</li>
                  <li>• OpenZeppelin security standards</li>
                  <li>• Decentralized governance proposals</li>
                </ul>
              </div>
            </div>
          </div>
        </section>
      )}

      {/* System Status - Always Visible */}
      <section className="mb-8" id="status">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-800 mb-4 text-center">System Status</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div className="text-center">
              <p className="font-medium text-blue-700">Backend API</p>
              <p className="text-blue-600">✅ Spring Boot Ready</p>
            </div>
            <div className="text-center">
              <p className="font-medium text-blue-700">Blockchain</p>
              <p className="text-blue-600">✅ zkSync Sepolia</p>
            </div>
            <div className="text-center">
              <p className="font-medium text-blue-700">Smart Contracts</p>
              <p className="text-blue-600">✅ Deployed & Funded</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
