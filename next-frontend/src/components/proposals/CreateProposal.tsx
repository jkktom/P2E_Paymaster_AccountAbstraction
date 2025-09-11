'use client';

import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '@/hooks/useAuth';
import { formatDateForBackend, addDaysToKoreaDate, getCurrentKoreaDate, formatDateForInput } from '@/utils/dateUtils';

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

  // Calculate minimum deadline (current time + 1 hour)
  const getMinDateTime = () => {
    const koreaDate = getCurrentKoreaDate();
    koreaDate.setHours(koreaDate.getHours() + 1);
    return formatDateForInput(koreaDate);
  };

  // Calculate maximum deadline (current time + 30 days)
  const getMaxDateTime = () => {
    const koreaDate = getCurrentKoreaDate();
    const futureDate = addDaysToKoreaDate(koreaDate, 30);
    return formatDateForInput(futureDate);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!user?.googleId) {
      setError('Login required to create a proposal.');
      return;
    }

    if (description.trim().length === 0) {
      setError('Please enter a proposal description.');
      return;
    }

    if (!deadline) {
      setError('Please select a deadline.');
      return;
    }

    const deadlineDate = new Date(deadline);
    const now = new Date();
    
    if (deadlineDate <= now) {
      setError('Deadline must be in the future.');
      return;
    }

    try {
      setIsSubmitting(true);
      setError(null);
      setSuccess(null);

      // Convert Korean time to UTC for backend
      const formattedDeadline = formatDateForBackend(deadlineDate);
      
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
      setSuccess(`Proposal created successfully! (Proposal #${createdProposal.blockchainProposalId})`);
      
      // Reset form
      setDescription('');
      setDeadline('');
      setShowForm(false);

      // Notify parent component
      if (onProposalCreated) {
        onProposalCreated();
      }

    } catch (error: any) {
      console.error('❌ Error creating proposal:', error);
      console.error('❌ Error response data:', error.response?.data);
      console.error('❌ Error status:', error.response?.status);
      
      let errorMessage = 'An error occurred while creating the proposal.';
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
    const koreaDate = getCurrentKoreaDate();
    const futureDate = addDaysToKoreaDate(koreaDate, days);
    setDeadline(formatDateForInput(futureDate));
  };

  if (!user) {
    return (
      <div className="text-center py-6">
        <p className="text-gray-500 text-sm">Login required to create a proposal.</p>
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
            Create New Proposal
          </button>
        </div>
      ) : (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
          <div className="flex justify-between items-center mb-4">
            <h4 className="text-md font-semibold text-gray-900">Create New Proposal</h4>
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
                Proposal Description *
              </label>
              <textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={4}
                placeholder="Enter a detailed description of your governance proposal..."
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none text-sm"
                required
              />
              <div className="flex justify-between text-xs text-gray-500 mt-1">
                <span>Write freely</span>
                <span>{description.length} chars</span>
              </div>
            </div>

            <div>
              <label htmlFor="deadline" className="block text-xs font-medium text-gray-700 mb-1">
                Voting Deadline (Korea Time) *
              </label>
              
              {/* Quick deadline buttons */}
              <div className="flex gap-2 mb-2">
                <button
                  type="button"
                  onClick={() => setQuickDeadline(1)}
                  className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs hover:bg-gray-200 transition-colors"
                >
                  1 Day Later
                </button>
                <button
                  type="button"
                  onClick={() => setQuickDeadline(7)}
                  className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs hover:bg-gray-200 transition-colors"
                >
                  1 Week Later
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
                Use quick setup buttons or select date directly
              </p>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
              <h5 className="text-xs font-medium text-blue-900 mb-1">⚠️ Important Notice</h5>
              <ul className="text-xs text-blue-800 space-y-1">
                <li>• Proposal will be recorded on zkSync Era blockchain</li>
                <li>• Proposal content cannot be modified after creation</li>
                <li>• Gas fees are sponsored through paymaster</li>
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
                    Creating...
                  </span>
                ) : (
                  'Create Proposal'
                )}
              </button>
              
              <button
                type="button"
                onClick={handleReset}
                disabled={isSubmitting}
                className="px-3 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm"
              >
                Reset
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default CreateProposal;