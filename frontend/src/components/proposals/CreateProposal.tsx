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

    if (description.trim().length < 10) {
      setError('제안 설명은 최소 10자 이상이어야 합니다.');
      return;
    }

    if (description.trim().length > 1000) {
      setError('제안 설명은 최대 1000자까지 입니다.');
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

      const proposalRequest = {
        description: description.trim(),
        proposerGoogleId: user.googleId,
        deadline: deadlineDate.toISOString(),
      };

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

      // 상위 컴포넌트에 알림
      if (onProposalCreated) {
        onProposalCreated();
      }

    } catch (error: any) {
      console.error('제안 생성 중 오류 발생:', error);
      const errorMessage = error.response?.data?.message || '제안 생성 중 오류가 발생했습니다.';
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

  if (!user) {
    return (
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">새 제안 생성</h3>
        <div className="text-center py-8">
          <p className="text-gray-500">제안을 생성하려면 로그인이 필요합니다.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">새 제안 생성</h3>
      
      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600 text-sm">{error}</p>
        </div>
      )}

      {success && (
        <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
          <p className="text-green-600 text-sm">{success}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
            제안 설명 *
          </label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={6}
            maxLength={1000}
            placeholder="거버넌스 제안에 대한 상세한 설명을 입력하세요..."
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            required
          />
          <div className="flex justify-between text-xs text-gray-500 mt-1">
            <span>최소 10자, 최대 1000자</span>
            <span>{description.length}/1000</span>
          </div>
        </div>

        <div>
          <label htmlFor="deadline" className="block text-sm font-medium text-gray-700 mb-2">
            투표 마감일 *
          </label>
          <input
            type="datetime-local"
            id="deadline"
            value={deadline}
            onChange={(e) => setDeadline(e.target.value)}
            min={getMinDateTime()}
            max={getMaxDateTime()}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required
          />
          <p className="text-xs text-gray-500 mt-1">
            최소 1시간 후부터 최대 30일 후까지 설정 가능합니다
          </p>
        </div>

        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 className="text-sm font-medium text-blue-900 mb-2">⚠️ 중요 안내</h4>
          <ul className="text-xs text-blue-800 space-y-1">
            <li>• 제안이 생성되면 zkSync Era 블록체인에 기록됩니다</li>
            <li>• 생성 후 제안 내용을 수정할 수 없습니다</li>
            <li>• 가스비는 페이마스터를 통해 후원됩니다</li>
            <li>• 제안 생성에는 몇 초에서 몇 분 소요될 수 있습니다</li>
          </ul>
        </div>

        <div className="flex gap-3">
          <button
            type="submit"
            disabled={isSubmitting || !description.trim() || !deadline}
            className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
          >
            {isSubmitting ? (
              <span className="flex items-center justify-center">
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                제안 생성 중...
              </span>
            ) : (
              '제안 생성'
            )}
          </button>
          
          <button
            type="button"
            onClick={handleReset}
            disabled={isSubmitting}
            className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            초기화
          </button>
        </div>
      </form>

      <div className="mt-4 text-xs text-gray-500">
        <p>제안자: {user.name} ({user.email})</p>
        <p>지갑 주소: {user.smartWalletAddress}</p>
      </div>
    </div>
  );
};

export default CreateProposal;