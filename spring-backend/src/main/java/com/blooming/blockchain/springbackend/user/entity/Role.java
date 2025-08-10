package com.blooming.blockchain.springbackend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {
    @Id
    private Byte id; // 1 = ADMIN, 2 = USER

    @Column(unique = true, nullable = false, length = 10)
    private String name; // "ADMIN", "USER"
}
