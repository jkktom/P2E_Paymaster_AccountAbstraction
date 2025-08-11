package com.blooming.blockchain.springbackend.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String googleId;
    private String email;
    private String name;
    private String avatar;
    private String smartWalletAddress;
    private int roleId;
    private LocalDateTime createdAt;
}
