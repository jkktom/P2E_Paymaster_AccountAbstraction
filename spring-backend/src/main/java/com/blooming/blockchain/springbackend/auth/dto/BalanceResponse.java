package com.blooming.blockchain.springbackend.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BalanceResponse {
    private int mainPoint;
    private int subPoint;
    private long tokenBalance;
    private LocalDateTime updatedAt;
}
