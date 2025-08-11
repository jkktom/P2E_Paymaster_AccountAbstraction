package com.blooming.blockchain.springbackend.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateResponse {
    private boolean success;
    private boolean valid;
    private UserResponse user;
    private boolean shouldRefresh;
    private String message;
}
