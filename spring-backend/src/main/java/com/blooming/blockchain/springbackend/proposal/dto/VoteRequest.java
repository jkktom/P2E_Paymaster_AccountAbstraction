package com.blooming.blockchain.springbackend.proposal.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 투표 요청 DTO
 */
@Data
public class VoteRequest {
    
    @NotNull(message = "Proposal ID is required")
    private Long proposalId;
    
    @NotBlank(message = "User Google ID is required")
    private String userGoogleId;
    
    @NotNull(message = "Support is required")
    private Boolean support;
}