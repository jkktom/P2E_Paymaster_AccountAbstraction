package com.blooming.blockchain.springbackend.proposal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Proposal Entity - 스마트 컨트랙트 구조와 정확히 일치
 * 
 * Smart Contract Proposal struct:
 * - string description
 * - address proposer
 * - uint256 forVotes (집계는 별도 엔티티에서 관리)
 * - uint256 againstVotes (집계는 별도 엔티티에서 관리)
 * - uint256 deadline
 * - bool executed
 * - bool canceled
 * - uint256 createdAt
 */
@Entity
@Table(name = "proposals", indexes = {
    @Index(name = "idx_proposals_blockchain_id", columnList = "blockchainProposalId"),
    @Index(name = "idx_proposals_status", columnList = "executed, canceled, deadline"),
    @Index(name = "idx_proposals_proposer_google", columnList = "proposerGoogleId"),
    @Index(name = "idx_proposals_deadline", columnList = "deadline")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 스마트 컨트랙트의 proposal ID (1부터 시작하는 순차 ID)
     */
    @Column(name = "blockchain_proposal_id", unique = true, nullable = false)
    @NotNull
    private Integer blockchainProposalId;

    /**
     * 제안 설명 (스마트 컨트랙트의 description)
     */
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String description;

    /**
     * 제안자의 지갑 주소 (스마트 컨트랙트의 proposer)
     */
    @Column(name = "proposer_address", length = 42, nullable = false)
    @NotBlank
    private String proposerAddress;

    /**
     * 투표 마감 시간 (스마트 컨트랙트의 deadline)
     */
    @Column(name = "deadline", nullable = false)
    @NotNull
    private LocalDateTime deadline;

    /**
     * 실행 여부 (스마트 컨트랙트의 executed)
     */
    @Column(name = "executed", nullable = false)
    @Builder.Default
    private Boolean executed = false;

    /**
     * 취소 여부 (스마트 컨트랙트의 canceled)
     */
    @Column(name = "canceled", nullable = false)
    @Builder.Default
    private Boolean canceled = false;

    /**
     * 생성 시간 (스마트 컨트랙트의 createdAt)
     */
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    // =============== 백엔드 전용 필드 (스마트 컨트랙트에는 없음) ===============

    /**
     * 제안 생성 트랜잭션 해시
     */
    @Column(name = "tx_hash", length = 66)
    private String txHash;

    /**
     * 제안자의 Google ID (백엔드 사용자 시스템과 연결)
     */
    @Column(name = "proposer_google_id", length = 50)
    private String proposerGoogleId;

    /**
     * 데이터베이스 레코드 생성 시간 (블록체인 생성 시간과 구분)
     */
    @Column(name = "db_created_at", nullable = false, updatable = false)
    private LocalDateTime dbCreatedAt;

    @PrePersist
    protected void onCreate() {
        dbCreatedAt = LocalDateTime.now();
    }

    // =============== 비즈니스 로직 메서드 ===============

    /**
     * 제안이 활성 상태인지 확인 (투표 가능)
     */
    public boolean isActive() {
        return !executed && !canceled && LocalDateTime.now().isBefore(deadline);
    }

    /**
     * 제안이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * 제안이 완료된 상태인지 확인 (실행됨 또는 취소됨)
     */
    public boolean isCompleted() {
        return executed || canceled;
    }

    /**
     * 투표 가능한 상태인지 확인
     */
    public boolean canVote() {
        return isActive();
    }
}