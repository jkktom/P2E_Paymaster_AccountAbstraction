package com.blooming.blockchain.springbackend.proposal.dto;

import com.blooming.blockchain.springbackend.proposal.entity.Proposal;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 제안 응답 DTO
 */
@Data
@Builder
public class ProposalResponse {
    
    private Integer id;
    private Integer blockchainProposalId;
    private String description;
    private String proposerAddress;
    private String proposerGoogleId;
    private LocalDateTime deadline;
    private boolean executed;
    private boolean canceled;
    private LocalDateTime createdAt;
    private String txHash;
    
    // 상태 정보
    private boolean isActive;
    private boolean canVote;
    private boolean isExpired;
    
    public static ProposalResponse fromEntity(Proposal proposal) {
        return ProposalResponse.builder()
                .id(proposal.getId())
                .blockchainProposalId(proposal.getId())
                .description(proposal.getDescription())
                .proposerAddress(proposal.getProposerAddress())
                .proposerGoogleId(proposal.getProposerGoogleId())
                .deadline(proposal.getDeadline())
                .executed(proposal.getExecuted())
                .canceled(proposal.getCanceled())
                .createdAt(proposal.getCreatedAt())
                .txHash(proposal.getTxHash())
                .isActive(proposal.isActive())
                .canVote(proposal.canVote())
                .isExpired(proposal.isExpired())
                .build();
    }
}