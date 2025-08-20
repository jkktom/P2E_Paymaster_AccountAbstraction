package com.blooming.blockchain.springbackend.proposal.dto;

import com.blooming.blockchain.springbackend.proposal.entity.UserVote;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 투표 응답 DTO
 */
@Data
@Builder
public class VoteResponse {
    
    private Long id;
    private Integer proposalId;
    private String userGoogleId;
    private String voterWalletAddress;
    private boolean support;
    private BigInteger votingPower;
    private LocalDateTime votedAt;
    private String txHash;
    
    public static VoteResponse fromEntity(UserVote userVote) {
        return VoteResponse.builder()
                .id(userVote.getId())
                .proposalId(userVote.getProposalId())
                .userGoogleId(userVote.getUserGoogleId())
                .voterWalletAddress(userVote.getVoterWalletAddress())
                .support(userVote.getSupport())
                .votingPower(userVote.getVotingPower())
                .votedAt(userVote.getVotedAt())
                .txHash(userVote.getTxHash())
                .build();
    }
}