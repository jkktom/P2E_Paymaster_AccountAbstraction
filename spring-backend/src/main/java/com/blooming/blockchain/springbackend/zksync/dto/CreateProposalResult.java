package com.blooming.blockchain.springbackend.zksync.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 스마트 컨트랙트 제안 생성 결과 DTO
 */
@Data
@Builder
public class CreateProposalResult {
    private String txHash;
    private Integer proposalId;
    private boolean success;
    private String errorMessage;
}