package com.blooming.blockchain.springbackend.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointGrantResponse {
    private boolean success;
    private TransactionInfo transaction;
    private String message;

    @Data
    @Builder
    public static class TransactionInfo {
        private Integer id;
        private Integer amount;
        private String pointType;
        private String status;
    }
}