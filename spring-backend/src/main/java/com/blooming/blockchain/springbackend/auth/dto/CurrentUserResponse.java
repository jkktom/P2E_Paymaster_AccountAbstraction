package com.blooming.blockchain.springbackend.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentUserResponse {
    private boolean success;
    private UserResponse user;
    private BalanceResponse balance;
}
