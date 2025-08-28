'use client'

import { useState, useEffect } from 'react'
import { getGovernanceTokenContract, getZkSyncProvider, createGaslessTransaction } from '@/lib/contracts'
import { Wallet } from 'zksync-ethers'
import type { Proposal, User, WalletState, TransactionStatus } from '@/types'

// zkSync Era Sepolia Contract Addresses (deployed and public)
const GOVERNANCE_TOKEN_ADDRESS = '0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e'

interface GovernanceVotingProps {
  user: User
  wallet: WalletState
}

export default function GovernanceVoting({ user, wallet }: GovernanceVotingProps) {
  const [proposals, setProposals] = useState<Proposal[]>([])
  const [votingPower, setVotingPower] = useState<string>('0')
  const [isLoading, setIsLoading] = useState(false)
  const [txStatus, setTxStatus] = useState<TransactionStatus>({ status: 'success' })
  const [newProposalDesc, setNewProposalDesc] = useState('')

  useEffect(() => {
    if (wallet.isConnected && wallet.address) {
      fetchGovernanceData()
    }
  }, [wallet.isConnected, wallet.address])

  const fetchGovernanceData = async () => {
    if (!wallet.address) return

    try {
      const provider = getZkSyncProvider()
      const contract = getGovernanceTokenContract(provider)

      // Get voting power
      const power = await contract.getVotingPower(wallet.address)
      setVotingPower(power.toString())

      // Fetch recent proposals (assuming we have a few test proposals)
      const proposalPromises = []
      for (let i = 1; i <= 3; i++) {
        try {
          const proposal = await contract.getProposal(i)
          if (proposal.proposer !== '0x0000000000000000000000000000000000000000') {
            proposalPromises.push({
              id: i,
              description: proposal.description,
              proposer: proposal.proposer,
              forVotes: proposal.forVotes.toString(),
              againstVotes: proposal.againstVotes.toString(),
              deadline: Number(proposal.deadline),
              executed: proposal.executed,
              canceled: proposal.canceled
            })
          }
        } catch (err) {
          // Proposal doesn't exist, skip
          break
        }
      }
      
      setProposals(proposalPromises)
    } catch (error) {
      console.error('Failed to fetch governance data:', error)
    }
  }

  const delegateVoting = async () => {
    if (!wallet.address) return

    setIsLoading(true)
    setTxStatus({ status: 'pending' })

    try {
      // Use user's private key to create wallet (in production, this would be handled securely)
      const provider = getZkSyncProvider()
      
      // For demo, we'll use a simple delegation
      const zkWallet = new Wallet('0x' + 'f'.repeat(64), provider) // Demo private key
      
      // Create gasless delegation transaction
      const tx = await createGaslessTransaction(
        zkWallet,
        GOVERNANCE_TOKEN_ADDRESS,
        'delegateVoting',
        [wallet.address]
      )

      setTxStatus({
        status: 'success',
        hash: tx.hash
      })

      await fetchGovernanceData()
    } catch (error: any) {
      console.error('Failed to delegate voting:', error)
      setTxStatus({
        status: 'error',
        error: error.message || 'Failed to delegate voting power'
      })
    } finally {
      setIsLoading(false)
    }
  }

  const createProposal = async () => {
    if (!newProposalDesc.trim() || !wallet.address) return

    setIsLoading(true)
    setTxStatus({ status: 'pending' })

    try {
      const provider = getZkSyncProvider()
      const zkWallet = new Wallet('0x' + 'f'.repeat(64), provider) // Demo private key
      
      // Set deadline to 7 days from now
      const deadline = Math.floor(Date.now() / 1000) + (7 * 24 * 60 * 60)
      
      // Create gasless proposal transaction
      const tx = await createGaslessTransaction(
        zkWallet,
        GOVERNANCE_TOKEN_ADDRESS,
        'createProposal',
        [newProposalDesc, deadline]
      )

      setTxStatus({
        status: 'success',
        hash: tx.hash
      })

      setNewProposalDesc('')
      await fetchGovernanceData()
    } catch (error: any) {
      console.error('Failed to create proposal:', error)
      setTxStatus({
        status: 'error',
        error: error.message || 'Failed to create proposal'
      })
    } finally {
      setIsLoading(false)
    }
  }

  const vote = async (proposalId: number, support: boolean) => {
    if (!wallet.address) return

    setIsLoading(true)
    setTxStatus({ status: 'pending' })

    try {
      const provider = getZkSyncProvider()
      const zkWallet = new Wallet('0x' + 'f'.repeat(64), provider) // Demo private key
      
      // Create gasless vote transaction
      const tx = await createGaslessTransaction(
        zkWallet,
        GOVERNANCE_TOKEN_ADDRESS,
        'vote',
        [proposalId, support]
      )

      setTxStatus({
        status: 'success',
        hash: tx.hash
      })

      await fetchGovernanceData()
    } catch (error: any) {
      console.error('Failed to vote:', error)
      setTxStatus({
        status: 'error',
        error: error.message || 'Failed to cast vote'
      })
    } finally {
      setIsLoading(false)
    }
  }

  if (!wallet.isConnected) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4">Governance Voting</h2>
        <p>Please connect your wallet to participate in governance.</p>
      </div>
    )
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-semibold mb-4">Governance Voting</h2>
      
      {/* Voting Power Status */}
      <div className="bg-purple-50 p-4 rounded-lg mb-6">
        <h3 className="font-medium text-purple-800">Your Voting Power</h3>
        <p className="text-2xl font-bold text-purple-600">
          {(parseFloat(votingPower) / 1e18).toFixed(2)} BLOOM
        </p>
        <p className="text-sm">
          Wallet: {wallet.address?.slice(0, 6)}...{wallet.address?.slice(-4)}
        </p>
        {parseFloat(votingPower) === 0 && (
          <button
            onClick={delegateVoting}
            disabled={isLoading}
            className="mt-2 px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:opacity-50"
          >
            Delegate to Self (Activate Voting)
          </button>
        )}
      </div>

      {/* Create Proposal */}
      <div className="mb-6 p-4 bg-gray-50 rounded-lg">
        <h3 className="font-medium mb-3">Create New Proposal</h3>
        <div className="flex gap-3">
          <input
            type="text"
            value={newProposalDesc}
            onChange={(e) => setNewProposalDesc(e.target.value)}
            placeholder="Enter proposal description..."
            className="flex-1 p-2 border border-gray-300 rounded"
            disabled={isLoading || parseFloat(votingPower) === 0}
          />
          <button
            onClick={createProposal}
            disabled={isLoading || !newProposalDesc.trim() || parseFloat(votingPower) === 0}
            className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50"
          >
            Create (Gasless)
          </button>
        </div>
        {parseFloat(votingPower) === 0 && (
          <p className="text-xs mt-2">You need voting power to create proposals</p>
        )}
      </div>

      {/* Proposals List */}
      <div>
        <h3 className="font-medium mb-3">Active Proposals</h3>
        {proposals.length === 0 ? (
          <p className="text-center py-4">No proposals found. Create the first one!</p>
        ) : (
          <div className="space-y-4">
            {proposals.map((proposal) => {
              const now = Math.floor(Date.now() / 1000)
              const isExpired = now > proposal.deadline
              const forVotes = parseFloat(proposal.forVotes) / 1e18
              const againstVotes = parseFloat(proposal.againstVotes) / 1e18
              const totalVotes = forVotes + againstVotes

              return (
                <div key={proposal.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex justify-between items-start mb-2">
                    <h4 className="font-medium">#{proposal.id}: {proposal.description}</h4>
                    <span className={`px-2 py-1 rounded text-xs ${
                      proposal.canceled ? 'bg-red-100 text-red-800' :
                      proposal.executed ? 'bg-green-100 text-green-800' :
                      isExpired ? 'bg-gray-100 text-gray-800' :
                      'bg-blue-100 text-blue-800'
                    }`}>
                      {proposal.canceled ? 'Canceled' :
                       proposal.executed ? 'Executed' :
                       isExpired ? 'Expired' : 'Active'}
                    </span>
                  </div>

                  <div className="text-sm mb-3">
                    <p>Proposer: {proposal.proposer.slice(0, 10)}...{proposal.proposer.slice(-8)}</p>
                    <p>Deadline: {new Date(proposal.deadline * 1000).toLocaleDateString()}</p>
                  </div>

                  <div className="mb-3">
                    <div className="flex justify-between text-sm mb-1">
                      <span>For: {forVotes.toFixed(2)} BLOOM</span>
                      <span>Against: {againstVotes.toFixed(2)} BLOOM</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-green-600 h-2 rounded-l-full"
                        style={{ 
                          width: totalVotes > 0 ? `${(forVotes / totalVotes) * 100}%` : '0%' 
                        }}
                      ></div>
                    </div>
                  </div>

                  {!proposal.executed && !proposal.canceled && !isExpired && parseFloat(votingPower) > 0 && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => vote(proposal.id, true)}
                        disabled={isLoading}
                        className="px-3 py-1 bg-green-600 text-white rounded text-sm hover:bg-green-700 disabled:opacity-50"
                      >
                        Vote For (Gasless)
                      </button>
                      <button
                        onClick={() => vote(proposal.id, false)}
                        disabled={isLoading}
                        className="px-3 py-1 bg-red-600 text-white rounded text-sm hover:bg-red-700 disabled:opacity-50"
                      >
                        Vote Against (Gasless)
                      </button>
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* Transaction Status */}
      {txStatus.status !== 'success' && (
        <div className={`mt-4 p-3 rounded-lg ${
          txStatus.status === 'pending' ? 'bg-yellow-50 border border-yellow-200' :
          txStatus.status === 'error' ? 'bg-red-50 border border-red-200' : ''
        }`}>
          <div className={`text-sm font-medium ${
            txStatus.status === 'pending' ? 'text-yellow-800' :
            txStatus.status === 'error' ? 'text-red-800' : ''
          }`}>
            {txStatus.status === 'pending' && '⏳ Processing blockchain transaction...'}
            {txStatus.status === 'error' && `❌ ${txStatus.error}`}
          </div>
          {txStatus.hash && (
            <p className="text-xs mt-1 font-mono">
              Tx: {txStatus.hash.slice(0, 10)}...{txStatus.hash.slice(-8)}
            </p>
          )}
        </div>
      )}

      {/* Info */}
      <div className="mt-6 text-xs bg-gray-50 p-3 rounded">
        <p><strong>Note:</strong> All governance transactions are gasless via zkSync paymaster.</p>
        <p>• Voting power requires delegation (click "Delegate to Self" if you have tokens)</p>
        <p>• Proposals require minimum voting power to create</p>
        <p>• All votes and proposals are recorded on zkSync Sepolia testnet</p>
      </div>
    </div>
  )
}