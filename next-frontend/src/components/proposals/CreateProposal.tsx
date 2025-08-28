'use client';

import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '@/hooks/useAuth';

interface CreateProposalProps {
  onProposalCreated?: () => void;
}

const CreateProposal: React.FC<CreateProposalProps> = ({ onProposalCreated }) => {
  const [description, setDescription] = useState('');
  const [deadline, setDeadline] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const { user, accessToken } = useAuth();

  const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

  // 최소 마감일 계산 (현재 시간 + 1시간)
  const getMinDateTime = () => {
    const now = new Date();
    now.setHours(now.getHours() + 1);
    return now.toISOString().slice(0, 16);
  };

  // 최대 마감일 계산 (현재 시간 + 30일)
  const getMaxDateTime = () => {
    const now = new Date();
    now.setDate(now.getDate() + 30);
    return now.toISOString().slice(0, 16);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!user?.googleId) {
      setError('제안을 생성하려면 로그인이 필요합니다.');
      return;
    }

    if (description.trim().length === 0) {
      setError('제안 설명을 입력해주세요.');
      return;
    }

    if (!deadline) {
      setError('마감일을 선택해주세요.');
      return;
    }

    const deadlineDate = new Date(deadline);
    const now = new Date();
    
    if (deadlineDate <= now) {
      setError('마감일은 현재 시간보다 미래여야 합니다.');
      return;
    }

    try {
      setIsSubmitting(true);
      setError(null);
      setSuccess(null);

      // Format deadline for LocalDateTime (yyyy-MM-ddTHH:mm:ss)
      const formattedDeadline = deadlineDate.toISOString().slice(0, 19);
      
      const proposalRequest = {
        description: description.trim(),
        proposerGoogleId: user.googleId,
        deadline: formattedDeadline,
      };

      console.log('🚀 Creating proposal with data:', {
        description: proposalRequest.description.substring(0, 50) + '...',
        proposerGoogleId: proposalRequest.proposerGoogleId,
        deadline: proposalRequest.deadline
      });

      const response = await axios.post(`${API_BASE_URL}/api/proposals`, proposalRequest, {
        headers: {
          'Content-Type': 'application/json',
          ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        },
      });

      const createdProposal = response.data;
      setSuccess(`제안이 성공적으로 생성되었습니다! (제안 #${createdProposal.blockchainProposalId})`);
      
      // 폼 초기화
      setDescription('');
      setDeadline('');
      setShowForm(false);

      // 상위 컴포넌트에 알림
      if (onProposalCreated) {
        onProposalCreated();
      }

    } catch (error: any) {
      console.error('❌ 제안 생성 중 오류 발생:', error);
      console.error('❌ Error response data:', error.response?.data);
      console.error('❌ Error status:', error.response?.status);
      
      let errorMessage = '제안 생성 중 오류가 발생했습니다.';
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.response?.data) {
        errorMessage = `${error.response.status}: ${JSON.stringify(error.response.data)}`;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      setError(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReset = () => {
    setDescription('');
    setDeadline('');
    setError(null);
    setSuccess(null);
  };

  const setQuickDeadline = (days: number) => {
    const now = new Date();
    now.setDate(now.getDate() + days);
    setDeadline(now.toISOString().slice(0, 16));
  };

  if (!user) {
    return (
      <div className="text-center py-6">
        <p className="text-gray-500 text-sm">제안을 생성하려면 로그인이 필요합니다.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {!showForm ? (
        <div className="text-center">
          <button
            onClick={() => setShowForm(true)}
            className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors text-sm"
          >
            새 제안 생성
          </button>
        </div>
      ) : (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
          <div className="flex justify-between items-center mb-4">
            <h4 className="text-md font-semibold text-gray-900">새 제안 생성</h4>
            <button
              onClick={() => {
                setShowForm(false);
                handleReset();
              }}
              className="text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div>
          
          {error && (
            <div className="mb-3 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-600 text-xs">{error}</p>
            </div>
          )}

          {success && (
            <div className="mb-3 p-3 bg-green-50 border border-green-200 rounded-lg">
              <p className="text-green-600 text-xs">{success}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-3">
            <div>
              <label htmlFor="description" className="block text-xs font-medium text-gray-700 mb-1">
                제안 설명 *
              </label>
              <textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={4}
                placeholder="거버넌스 제안에 대한 상세한 설명을 입력하세요..."
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none text-sm"
                required
              />
              <div className="flex justify-between text-xs text-gray-500 mt-1">
                <span>자유롭게 작성하세요</span>
                <span>{description.length}자</span>
              </div>
            </div>

            <div>
              <label htmlFor="deadline" className="block text-xs font-medium text-gray-700 mb-1">
                투표 마감일 *
              </label>
              
              {/* Quick deadline buttons */}
              <div className="flex gap-2 mb-2">
                <button
                  type="button"
                  onClick={() => setQuickDeadline(1)}
                  className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs hover:bg-gray-200 transition-colors"
                >
                  1일 후
                </button>
                <button
                  type="button"
                  onClick={() => setQuickDeadline(7)}
                  className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs hover:bg-gray-200 transition-colors"
                >
                  1주일 후
                </button>
              </div>
              
              <input
                type="datetime-local"
                id="deadline"
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
                min={getMinDateTime()}
                max={getMaxDateTime()}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                required
              />
              <p className="text-xs text-gray-500 mt-1">
                빠른 설정 버튼을 사용하거나 직접 날짜를 선택하세요
              </p>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
              <h5 className="text-xs font-medium text-blue-900 mb-1">⚠️ 중요 안내</h5>
              <ul className="text-xs text-blue-800 space-y-1">
                <li>• 제안이 zkSync Era 블록체인에 기록됩니다</li>
                <li>• 생성 후 제안 내용을 수정할 수 없습니다</li>
                <li>• 가스비는 페이마스터를 통해 후원됩니다</li>
              </ul>
            </div>

            <div className="flex gap-2">
              <button
                type="submit"
                disabled={isSubmitting || !description.trim() || !deadline}
                className="flex-1 px-3 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm"
              >
                {isSubmitting ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    생성 중...
                  </span>
                ) : (
                  '제안 생성'
                )}
              </button>
              
              <button
                type="button"
                onClick={handleReset}
                disabled={isSubmitting}
                className="px-3 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm"
              >
                초기화
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default CreateProposal;