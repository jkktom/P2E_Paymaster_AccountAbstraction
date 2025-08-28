package com.blooming.blockchain.springbackend.zksync.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenExchangeResponse {
    private boolean success;
    private Integer mainPointsExchanged;
    private Integer tokensReceived;
    private String walletAddress;
    private String transactionHash;
    private String explorerUrl;
    private Integer tokenTransactionId;
    private Integer newMainPointBalance;
    private Long newTokenBalance;
    private String newTokenBalanceFormatted;
    private String message;
}