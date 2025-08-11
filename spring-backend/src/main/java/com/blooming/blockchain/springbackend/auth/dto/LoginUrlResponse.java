package com.blooming.blockchain.springbackend.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginUrlResponse {
    private boolean success;
    private String loginUrl;
    private String message;
}
