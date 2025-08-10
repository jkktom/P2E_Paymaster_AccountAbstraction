package com.blooming.blockchain.springbackend.userdetail.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_point_tokens")
public class UserPointToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private byte id;

    @Column(name = "user_google_id", nullable = false, unique = true)
    private String userGoogleId;

    @Column(nullable = false)
    @Min(0)
    private int mainPoint;

    @Column(nullable = false)
    @Min(0)
    private int subPoint;

    @Column(nullable = false)
    @Min(0)
    private long tokenBalance;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
