'use client';

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '@/hooks/useAuth';

interface Proposal {
  id: number;
  blockchainProposalId: number;
  description: string;
  proposerAddress: string;
  proposerGoogleId: string;
  deadline: string;
  executed: boolean;
  canceled: boolean;
  createdAt: string;
  txHash: string;
  isActive: boolean;
  canVote: boolean;
  isExpired: boolean;
}

interface VoteStats {
  totalVoters: number;
  forVoters: number;
  againstVoters: number;
}

const ProposalList: React.FC = () => {
  const [proposals, setProposals] = useState<Proposal[]>([]);
  const [voteStats, setVoteStats] = useState<Record<number, VoteStats>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [votingProposal, setVotingProposal] = useState<number | null>(null);
  const { user, accessToken } = useAuth();

  const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

  useEffect(() => {
    fetchProposals();
  }, []);

  const fetchProposals = async () => {
    try {
      setLoading(true);
      setError(null);

      // 활성 제안들 가져오기
      const proposalsResponse = await axios.get(`${API_BASE_URL}/api/proposals/active`, {
        headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined,
      });

      const proposalsData = proposalsResponse.data;
      setProposals(proposalsData);

      // 각 제안의 투표 통계 가져오기
      const statsPromises = proposalsData.map(async (proposal: Proposal) => {
        try {
          const statsResponse = await axios.get(
            `${API_BASE_URL}/api/votes/stats/proposal/${proposal.id}`,
            {
              headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined,
            }
          );
          return { proposalId: proposal.id, stats: statsResponse.data };
        } catch (error) {
          console.warn(`투표 통계를 가져올 수 없습니다. 제안 ID: ${proposal.id}`, error);
          return { proposalId: proposal.id, stats: { totalVoters: 0, forVoters: 0, againstVoters: 0 } };
        }
      });

      const statsResults = await Promise.all(statsPromises);
      const statsMap = statsResults.reduce((acc, { proposalId, stats }) => {
        acc[proposalId] = stats;
        return acc;
      }, {} as Record<number, VoteStats>);

      setVoteStats(statsMap);
    } catch (error) {
      console.error('제안 목록을 가져오는 중 오류 발생:', error);
      setError('제안 목록을 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleVote = async (proposalId: number, support: boolean) => {
    if (!user?.googleId) {
      alert('투표하려면 로그인이 필요합니다.');
      return;
    }

    try {
      setVotingProposal(proposalId);
      setError(null);

      const voteRequest = {
        proposalId,
        userGoogleId: user.googleId,
        support,
      };

      await axios.post(`${API_BASE_URL}/api/votes`, voteRequest, {
        headers: {
          'Content-Type': 'application/json',
          ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        },
      });

      alert(`${support ? '찬성' : '반대'} 투표가 완료되었습니다!`);
      
      // 투표 후 제안 목록과 통계 새로고침
      await fetchProposals();
    } catch (error: any) {
      console.error('투표 중 오류 발생:', error);
      const errorMessage = error.response?.data?.message || '투표 처리 중 오류가 발생했습니다.';
      setError(errorMessage);
      alert(errorMessage);
    } finally {
      setVotingProposal(null);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadge = (proposal: Proposal) => {
    if (proposal.executed) {
      return <span className="px-2 py-1 bg-green-100 text-green-800 text-sm rounded-full">실행됨</span>;
    }
    if (proposal.canceled) {
      return <span className="px-2 py-1 bg-red-100 text-red-800 text-sm rounded-full">취소됨</span>;
    }
    if (proposal.isExpired) {
      return <span className="px-2 py-1 bg-gray-100 text-gray-800 text-sm rounded-full">만료됨</span>;
    }
    if (proposal.isActive) {
      return <span className="px-2 py-1 bg-blue-100 text-blue-800 text-sm rounded-full">활성</span>;
    }
    return <span className="px-2 py-1 bg-gray-100 text-gray-800 text-sm rounded-full">비활성</span>;
  };

  const getVotePercentage = (forVotes: number, totalVotes: number) => {
    if (totalVotes === 0) return 0;
    return Math.round((forVotes / totalVotes) * 100);
  };

  if (loading) {
    return (
      <div className="space-y-4">
        <h2 className="text-2xl font-bold text-gray-900">거버넌스 제안</h2>
        <div className="animate-pulse">
          <div className="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-32 bg-gray-200 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">거버넌스 제안</h2>
        <button
          onClick={fetchProposals}
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
        >
          새로고침
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600">{error}</p>
        </div>
      )}

      {proposals.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-gray-500 text-lg">현재 활성 제안이 없습니다.</div>
          <p className="text-gray-400 mt-2">새로운 제안을 생성해보세요!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {proposals.map((proposal) => {
            const stats = voteStats[proposal.id] || { totalVoters: 0, forVoters: 0, againstVoters: 0 };
            const forPercentage = getVotePercentage(stats.forVoters, stats.totalVoters);
            
            return (
              <div key={proposal.id} className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-lg font-semibold text-gray-900">
                        제안 #{proposal.blockchainProposalId}
                      </h3>
                      {getStatusBadge(proposal)}
                    </div>
                    <p className="text-gray-700 leading-relaxed mb-3">{proposal.description}</p>
                    
                    <div className="text-sm text-gray-500 space-y-1">
                      <p>제안자: {proposal.proposerAddress.slice(0, 6)}...{proposal.proposerAddress.slice(-4)}</p>
                      <p>생성일: {formatDate(proposal.createdAt)}</p>
                      <p>마감일: {formatDate(proposal.deadline)}</p>
                      {proposal.txHash && (
                        <p>트랜잭션: {proposal.txHash.slice(0, 10)}...{proposal.txHash.slice(-6)}</p>
                      )}
                    </div>
                  </div>
                </div>

                {/* 투표 통계 */}
                <div className="mb-4 p-4 bg-gray-50 rounded-lg">
                  <h4 className="text-sm font-medium text-gray-900 mb-3">투표 현황</h4>
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>총 투표자: {stats.totalVoters}명</span>
                      <span>찬성률: {forPercentage}%</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-green-500 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${forPercentage}%` }}
                      ></div>
                    </div>
                    <div className="flex justify-between text-xs text-gray-600">
                      <span>찬성: {stats.forVoters}명</span>
                      <span>반대: {stats.againstVoters}명</span>
                    </div>
                  </div>
                </div>

                {/* 투표 버튼 */}
                {proposal.canVote && user && (
                  <div className="flex gap-3">
                    <button
                      onClick={() => handleVote(proposal.id, true)}
                      disabled={votingProposal === proposal.id}
                      className="flex-1 px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      {votingProposal === proposal.id ? '처리 중...' : '찬성'}
                    </button>
                    <button
                      onClick={() => handleVote(proposal.id, false)}
                      disabled={votingProposal === proposal.id}
                      className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      {votingProposal === proposal.id ? '처리 중...' : '반대'}
                    </button>
                  </div>
                )}

                {!proposal.canVote && (
                  <div className="text-center py-2 text-gray-500 text-sm">
                    {proposal.isExpired ? '마감된 제안입니다' : '투표할 수 없는 제안입니다'}
                  </div>
                )}

                {!user && (
                  <div className="text-center py-2 text-gray-500 text-sm">
                    투표하려면 로그인이 필요합니다
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default ProposalList;