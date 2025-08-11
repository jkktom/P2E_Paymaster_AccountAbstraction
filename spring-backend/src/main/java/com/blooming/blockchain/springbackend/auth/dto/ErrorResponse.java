package com.blooming.blockchain.springbackend.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private boolean success;
    private String message;
    private String error;
}
