package com.blooming.blockchain.springbackend.global.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointType {
    @Id
    private Byte id; // 1 = MAIN, 2 = SUB

    @Column(unique = true, nullable = false, length = 10)
    private String name; // "MAIN", "SUB"
}
