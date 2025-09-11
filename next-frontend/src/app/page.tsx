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
            Blooming Blockchain Service
          </h1>
          <p className="text-xl mb-12 max-w-3xl mx-auto leading-relaxed">
            Experience gasless transactions and governance voting powered by zkSync technology.
            <br />
            We provide simpler and safer blockchain services through account abstraction.
          </p>
          
          {!isAuthenticated ? (
            <div className="card-facebook p-8 max-w-md mx-auto">
              <h2 className="text-2xl font-semibold mb-6">
                Get Started
              </h2>
              <p className="mb-8 text-sm leading-relaxed">
                Easily log in with your Google account, earn points, 
                and exchange governance tokens.
              </p>
              
              {/* Google Auth Component */}
              <GoogleAuth onAuthSuccess={handleAuthSuccess} />
            </div>
          ) : (
            <div className="bg-blue-50 border border-blue-200 rounded-xl p-6 max-w-2xl mx-auto">
              <h3 className="text-lg font-semibold text-blue-800 mb-4">
                Welcome! 🎉
              </h3>
              
              {/* User Info Display */}
              <div className="mb-4 p-4 bg-white rounded-lg border">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    {user?.avatar ? (
                      <img 
                        src={user.avatar} 
                        alt="Profile" 
                        className="w-10 h-10 rounded-full"
                        onError={(e) => {
                          console.log('❌ Avatar failed to load on main page:', user.avatar);
                          e.currentTarget.style.display = 'none';
                          e.currentTarget.nextElementSibling?.classList.remove('hidden');
                        }}
                      />
                    ) : null}
                    <div className={`w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center ${user?.avatar ? 'hidden' : ''}`}>
                      <span className="text-lg font-bold text-white">
                        {user?.name?.charAt(0).toUpperCase()}
                      </span>
                    </div>
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
                    Logout
                  </button>
                </div>
              </div>
              
              <p className="text-blue-600 text-sm">
                You can now use all features including point earning, token exchange, and governance voting.
              </p>
            </div>
          )}
        </div>
      </section>

      {/* Features Overview - Always Visible */}
      <section className="mb-12" id="features">
        <h3 className="text-2xl font-bold mb-6 text-center">
          Platform Key Features
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">🪙</div>
            <h4 className="text-xl font-semibold mb-2">Point System</h4>
            <p>
              Earn sub points through activities, convert them to main points, 
              and exchange them for governance tokens at a 10:1 ratio.
            </p>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">⚡</div>
            <h4 className="text-xl font-semibold mb-2">Gasless Transactions</h4>
            <p>
              All blockchain transactions are sponsored through zkSync paymaster. 
              You can use all features without gas fees.
            </p>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-3xl mb-4">🗳️</div>
            <h4 className="text-xl font-semibold mb-2">Governance Voting</h4>
            <p>
              Participate in decentralized governance with BLOOM tokens. 
              Create proposals and vote on important decisions.
            </p>
          </div>
        </div>
      </section>

      {/* Main Application - Always Show Components */}
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
              <h3 className="text-lg font-semibold text-green-600 mb-4">💳 AA Wallet Address</h3>
              {isAuthenticated && user?.smartWalletAddress ? (
                <div className="bg-gray-50 p-4 rounded-lg">
                  <p className="text-sm mb-2">Smart Wallet Address:</p>
                  <p className="font-mono text-sm bg-white p-3 rounded border break-all">
                    {user.smartWalletAddress}
                  </p>
                  <p className="text-xs mt-2">
                    This wallet was created with zkSync Account Abstraction. All transactions are processed gaslessly.
                  </p>
                </div>
              ) : isAuthenticated ? (
                <p className="text-gray-500">Creating wallet...</p>
              ) : (
                <div className="bg-gray-50 p-4 rounded-lg text-center">
                  <p className="text-gray-500 mb-2">Log in to view your smart wallet address</p>
                  <button
                    onClick={() => document.getElementById('hero')?.scrollIntoView({ behavior: 'smooth' })}
                    className="text-blue-600 hover:text-blue-700 text-sm underline"
                  >
                    Go to Login
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Governance Proposals Section - Always Show */}
      <section className="mb-12">
        <div className="bg-white rounded-lg shadow-md p-6" id="governance">
          <h3 className="text-xl font-semibold text-purple-600 mb-6">🗳️ Governance Proposals and Voting</h3>
          
          {/* Create Proposal */}
          <div className="mb-6">
            <CreateProposal onProposalCreated={handleProposalCreated} />
          </div>
          
          {/* Proposals List */}
          <div>
            <h4 className="text-lg font-semibold text-gray-900 mb-4">Active Proposals List</h4>
            <ProposalList />
          </div>
        </div>
      </section>

      {/* Additional Information Section - Show only when not logged in */}
      {!isAuthenticated && (
        <section className="mb-12" id="demo">
          <div className="bg-white rounded-lg shadow-md p-8">
            <h3 className="text-2xl font-semibold text-center mb-6">
              System Architecture Demo
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div>
                <h4 className="text-lg font-semibold mb-3 text-blue-700">Backend (Spring Boot)</h4>
                <ul className="text-sm space-y-2">
                  <li>• Google OAuth 2.0 Authentication</li>
                  <li>• JWT Token Management</li>
                  <li>• H2/PostgreSQL Database and JPA</li>
                  <li>• Point System with Transaction Records</li>
                  <li>• Role-based Access Control (Admin/User)</li>
                  <li>• RESTful API for All Operations</li>
                </ul>
              </div>
              <div>
                <h4 className="text-lg font-semibold mb-3 text-green-700">Blockchain (zkSync)</h4>
                <ul className="text-sm space-y-2">
                  <li>• ERC20 Governance Token with Voting Features</li>
                  <li>• Account Abstraction using Paymaster</li>
                  <li>• Gasless Transactions for All Users</li>
                  <li>• zkSync Sepolia Testnet Deployment</li>
                  <li>• OpenZeppelin Security Standards</li>
                  <li>• Decentralized Governance Proposals</li>
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
              <p className="font-medium text-blue-700">Smart Contract</p>
              <p className="text-blue-600">✅ Deployed and Funded</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
