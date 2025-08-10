package com.blooming.blockchain.springbackend.global.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "transaction_status")
public class TransactionStatus {
    @Id
    private Byte id; // 1 = PENDING, 2 = CONFIRMED, 3 = FAILED

    @Column(unique = true, nullable = false, length = 20)
    private String name; // "PENDING", "CONFIRMED", "FAILED"

}
