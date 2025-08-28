package com.blooming.blockchain.springbackend.zksync.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletBalanceResponse {
    private String address;
    private String ethBalance;
    private String ethBalanceFormatted;
    private String governanceTokenBalance;
    private String governanceTokenBalanceFormatted;
}