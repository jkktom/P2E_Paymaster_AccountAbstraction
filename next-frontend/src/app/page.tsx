'use client'

import { useAuth } from '@/hooks/useAuth'
import GoogleAuth from '@/components/GoogleAuthBackend'
import PointsManager from '@/components/PointsManager'
import CreateProposal from '@/components/proposals/CreateProposal'
import ProposalList from '@/components/proposals/ProposalList'

export default function Home() {
  const { user, isAuthenticated, signOut } = useAuth()

  const handleAuthSuccess = (user: any) => {
    // This will be handled by the useAuth hook automatically
    console.log('Authentication successful:', user)
  }

  const handleProposalCreated = () => {
    // This will trigger a refresh of the proposal list
    window.location.reload()
  }

  return (
    <div className="px-6 py-8">
      {/* Hero Section with Facebook-style Auth */}
      <section className="text-center mb-16" id="hero">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-5xl font-semibold mb-6 leading-tight">
            블루밍 블록체인 서비스
          </h1>
          <p className="text-xl mb-12 max-w-3xl mx-auto leading-relaxed">
            zkSync 기술로 구현된 가스리스 거래와 거버넌스 투표를 경험해보세요.
            <br />
            계정 추상화로 더 간단하고 안전한 블록체인 서비스를 제공합니다.
          </p>
          
          {!isAuthenticated ? (
            <div className="card-facebook p-8 max-w-md mx-auto">
              <h2 className="text-2xl font-semibold mb-6">
                서비스 시작하기
              </h2>
              <p className="mb-8 text-sm leading-relaxed">
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
                      <p className="font-medium">{user?.name}</p>
                      <p className="text-sm">{user?.email}</p>
                      <p className="text-xs">ID: {user?.googleId}</p>
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
        <h3 className="text-2xl font-bold mb-6 text-center">
          플랫폼 주요 기능
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">🪙</div>
            <h4 className="text-xl font-semibold mb-2">포인트 시스템</h4>
            <p>
              활동을 통해 서브포인트를 획득하고, 메인포인트로 전환한 후 
              거버넌스 토큰과 10:1 비율로 교환할 수 있습니다.
            </p>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">⚡</div>
            <h4 className="text-xl font-semibold mb-2">가스리스 거래</h4>
            <p>
              모든 블록체인 거래는 zkSync 페이마스터를 통해 후원됩니다. 
              가스비 없이 모든 기능을 이용할 수 있습니다.
            </p>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">🗳️</div>
            <h4 className="text-xl font-semibold mb-2">거버넌스 투표</h4>
            <p>
              BLOOM 토큰으로 탈중앙화 거버넌스에 참여하세요. 
              제안서를 생성하고 중요한 결정에 투표할 수 있습니다.
            </p>
          </div>
        </div>
      </section>

      {/* Main Application - Conditional Content */}
      {isAuthenticated && user ? (
        <>
          {/* Main Content Section */}
          <section className="mb-12">
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
              {/* Points Management */}
              <div className="space-y-6">
                <div id="points">
                  <PointsManager user={user} />
                </div>
              </div>

              {/* AA Wallet Address */}
              <div className="space-y-6">
                <div className="bg-white rounded-lg shadow-md p-6" id="wallet-info">
                  <h3 className="text-lg font-semibold text-green-600 mb-4">💳 AA 지갑 주소</h3>
                  {user.smartWalletAddress ? (
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <p className="text-sm mb-2">스마트 지갑 주소:</p>
                      <p className="font-mono text-sm bg-white p-3 rounded border break-all">
                        {user.smartWalletAddress}
                      </p>
                      <p className="text-xs mt-2">
                        zkSync Account Abstraction으로 생성된 지갑입니다. 모든 거래가 가스리스로 처리됩니다.
                      </p>
                    </div>
                  ) : (
                    <p>지갑 생성 중...</p>
                  )}
                </div>
              </div>
            </div>
          </section>

          {/* Governance Proposals Section */}
          <section className="mb-12">
            <div className="bg-white rounded-lg shadow-md p-6" id="governance">
              <h3 className="text-xl font-semibold text-purple-600 mb-6">🗳️ 거버넌스 제안 및 투표</h3>
              
              {/* Create Proposal */}
              <div className="mb-6">
                <CreateProposal onProposalCreated={handleProposalCreated} />
              </div>
              
              {/* Proposals List */}
              <div>
                <h4 className="text-lg font-semibold text-gray-900 mb-4">활성 제안 목록</h4>
                <ProposalList />
              </div>
            </div>
          </section>

        </>
      ) : (
        /* Public Demo Content */
        <section className="mb-12" id="demo">
          <div className="bg-white rounded-lg shadow-md p-8">
            <h3 className="text-2xl font-semibold text-center mb-6">
              시스템 아키텍처 데모
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div>
                <h4 className="text-lg font-semibold mb-3 text-blue-700">백엔드 (Spring Boot)</h4>
                <ul className="text-sm space-y-2">
                  <li>• Google OAuth 2.0 인증</li>
                  <li>• JWT 토큰 관리</li>
                  <li>• H2/PostgreSQL 데이터베이스 및 JPA</li>
                  <li>• 거래 기록이 포함된 포인트 시스템</li>
                  <li>• 역할 기반 접근 제어 (관리자/사용자)</li>
                  <li>• 모든 작업을 위한 RESTful API</li>
                </ul>
              </div>
              <div>
                <h4 className="text-lg font-semibold mb-3 text-green-700">블록체인 (zkSync)</h4>
                <ul className="text-sm space-y-2">
                  <li>• 투표 기능이 있는 ERC20 거버넌스 토큰</li>
                  <li>• 페이마스터를 사용한 계정 추상화</li>
                  <li>• 모든 사용자를 위한 가스리스 거래</li>
                  <li>• zkSync 세폴리아 테스트넷 배포</li>
                  <li>• OpenZeppelin 보안 표준</li>
                  <li>• 탈중앙화 거버넌스 제안</li>
                </ul>
              </div>
            </div>
          </div>
        </section>
      )}

      {/* System Status - Always Visible */}
      <section className="mb-8" id="status">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-800 mb-4 text-center">시스템 상태</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div className="text-center">
              <p className="font-medium text-blue-700">백엔드 API</p>
              <p className="text-blue-600">✅ Spring Boot 준비완료</p>
            </div>
            <div className="text-center">
              <p className="font-medium text-blue-700">블록체인</p>
              <p className="text-blue-600">✅ zkSync 세폴리아</p>
            </div>
            <div className="text-center">
              <p className="font-medium text-blue-700">스마트 컨트랙트</p>
              <p className="text-blue-600">✅ 배포 및 자금조달 완료</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
