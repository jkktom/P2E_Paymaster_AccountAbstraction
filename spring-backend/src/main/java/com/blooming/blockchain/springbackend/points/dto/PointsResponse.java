package com.blooming.blockchain.springbackend.points.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointsResponse {
    private boolean success;
    private Integer balance;
    private Integer totalEarned;
    private Integer pointsToToken;
    private Integer subToMain;
    private Long tokenBalance;
    private String updatedAt;
    private String message;
    private Integer mainPointsReceived;
    private Integer conversionRate;
}
