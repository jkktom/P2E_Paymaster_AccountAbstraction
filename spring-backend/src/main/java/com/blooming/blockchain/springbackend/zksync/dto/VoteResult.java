package com.blooming.blockchain.springbackend.zksync.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

/**
 * 스마트 컨트랙트 투표 결과 DTO
 */
@Data
@Builder
public class VoteResult {
    private String txHash;
    private Integer proposalId;
    private boolean support;
    private BigInteger votingPower;
    private boolean success;
    private String errorMessage;
}