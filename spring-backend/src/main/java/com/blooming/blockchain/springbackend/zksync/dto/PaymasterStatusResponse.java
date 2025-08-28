package com.blooming.blockchain.springbackend.zksync.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymasterStatusResponse {
    private String address;
    private String balance;
    private String balanceInEth;
    private boolean isActive;
    private String governanceTokenAddress;
}