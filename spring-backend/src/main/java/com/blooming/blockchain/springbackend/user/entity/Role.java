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
@Table(name = "roles")
public class Role {
    @Id
    private Byte id; // 1 = ADMIN, 2 = USER

    @Column(unique = true, nullable = false, length = 10)
    private String name; // "ADMIN", "USER"

    // Static reference data for database initialization
    public static final Object[][] REFERENCE_DATA = {
        // {id, name}
        {1, "ADMIN"},
        {2, "USER"}
    };
}
