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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * UserVote Entity - Google ID 기반 개별 투표 기록
 * 
 * 이 엔티티는 각 사용자의 개별 투표를 기록합니다.
 * Google ID를 기반으로 사용자를 식별하며, 중복 투표를 방지합니다.
 */
@Entity
@Table(name = "user_votes", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_user_votes_proposal_user", 
           columnNames = {"proposal_id", "user_google_id"}
       ),
       indexes = {
           @Index(name = "idx_user_votes_user", columnList = "user_google_id"),
           @Index(name = "idx_user_votes_proposal", columnList = "proposal_id"),
           @Index(name = "idx_user_votes_support", columnList = "support"),
           @Index(name = "idx_user_votes_voted_at", columnList = "voted_at")
       })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연관된 제안 ID
     */
    @Column(name = "proposal_id", nullable = false)
    @NotNull
    private Integer proposalId;

    /**
     * 연관된 제안 엔티티 (지연 로딩)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", insertable = false, updatable = false)
    private Proposal proposal;

    /**
     * 투표자의 Google ID (프로젝트의 사용자 식별 시스템)
     */
    @Column(name = "user_google_id", length = 50, nullable = false)
    @NotBlank
    private String userGoogleId;

    /**
     * 투표자의 지갑 주소 (블록체인 검증용)
     */
    @Column(name = "voter_wallet_address", length = 42, nullable = false)
    @NotBlank
    private String voterWalletAddress;

    /**
     * 투표 내용 (true = 찬성, false = 반대)
     */
    @Column(name = "support", nullable = false)
    @NotNull
    private Boolean support;

    /**
     * 투표 당시의 투표 권한 (토큰 보유량, Wei 단위)
     */
    @Column(name = "voting_power", precision = 30, scale = 0, nullable = false)
    @NotNull
    private BigInteger votingPower;

    /**
     * 투표 시간 (사용자가 실제 투표한 시간)
     */
    @Column(name = "voted_at", nullable = false)
    @NotNull
    private LocalDateTime votedAt;

    /**
     * 블록체인 트랜잭션 해시
     */
    @Column(name = "tx_hash", length = 66)
    private String txHash;

    /**
     * 데이터베이스 기록 시간 (투표 시간과 구분)
     */
    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;


    // =============== 계산된 필드들 (DB에 저장되지 않음) ===============

    /**
     * 투표 권한을 Ether 단위로 변환 (표시용)
     */
    public BigDecimal getVotingPowerInEther() {
        return new BigDecimal(votingPower)
            .divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
    }

    /**
     * 투표 내용을 문자열로 반환
     */
    public String getSupportText() {
        return support ? "찬성" : "반대";
    }

    /**
     * 투표 내용을 영문으로 반환 (API 응답용)
     */
    public String getSupportValue() {
        return support ? "FOR" : "AGAINST";
    }


    // =============== 비즈니스 로직 메서드 ===============

    /**
     * 찬성 투표인지 확인
     */
    public boolean isForVote() {
        return Boolean.TRUE.equals(support);
    }

    /**
     * 반대 투표인지 확인
     */
    public boolean isAgainstVote() {
        return Boolean.FALSE.equals(support);
    }


    /**
     * 투표 파워가 특정 임계값 이상인지 확인
     */
    public boolean hasSignificantVotingPower(BigInteger threshold) {
        return votingPower.compareTo(threshold) >= 0;
    }

    /**
     * 블록체인에서 확인된 투표인지 확인
     */
    public boolean isConfirmedOnBlockchain() {
        return txHash != null && !txHash.trim().isEmpty();
    }

    /**
     * 투표 요약 정보 생성
     */
    public String getVoteSummary() {
        return String.format("%s 투표 - %s BLOOM",
            getSupportText(),
            getVotingPowerInEther().setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 투표 검증 - 기본적인 데이터 무결성 확인
     */
    public boolean isValid() {
        return userGoogleId != null && !userGoogleId.trim().isEmpty()
            && voterWalletAddress != null && voterWalletAddress.matches("^0x[a-fA-F0-9]{40}$")
            && support != null
            && votingPower != null && votingPower.compareTo(BigInteger.ZERO) > 0
            && votedAt != null;
    }

    // =============== 정적 팩토리 메서드 ===============

    /**
     * 찬성 투표 생성
     */
    public static UserVote createForVote(Integer proposalId, String userGoogleId, String walletAddress,
                                        BigInteger votingPower, String txHash) {
        return UserVote.builder()
            .proposalId(proposalId)
            .userGoogleId(userGoogleId)
            .voterWalletAddress(walletAddress)
            .support(true)
            .votingPower(votingPower)
            .votedAt(LocalDateTime.now())
            .txHash(txHash)
            .build();
    }

    /**
     * 반대 투표 생성
     */
    public static UserVote createAgainstVote(Integer proposalId, String userGoogleId, String walletAddress,
                                           BigInteger votingPower, String txHash) {
        return UserVote.builder()
            .proposalId(proposalId)
            .userGoogleId(userGoogleId)
            .voterWalletAddress(walletAddress)
            .support(false)
            .votingPower(votingPower)
            .votedAt(LocalDateTime.now())
            .txHash(txHash)
            .build();
    }
}