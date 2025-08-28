package com.blooming.blockchain.springbackend.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsufficientPointsErrorResponse {
    private boolean success;
    private String error;
    private Integer currentMainPoints;
    private Integer requiredMainPoints;
}