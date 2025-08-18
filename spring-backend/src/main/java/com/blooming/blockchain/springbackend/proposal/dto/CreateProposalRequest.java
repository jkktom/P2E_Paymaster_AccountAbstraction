package com.blooming.blockchain.springbackend.proposal.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 제안 생성 요청 DTO
 */
@Data
public class CreateProposalRequest {
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;
    
    @NotBlank(message = "Proposer Google ID is required")
    private String proposerGoogleId;
    
    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;
}