package com.blooming.blockchain.springbackend.proposal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * ProposalVoteCount Entity - 실시간 투표 집계 관리
 * 
 * 이 엔티티는 각 제안의 투표 결과를 실시간으로 집계하여 저장합니다.
 * 매번 개별 투표를 집계할 필요 없이 O(1) 시간에 투표 현황을 조회할 수 있습니다.
 */
@Entity
@Table(name = "proposal_vote_counts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProposalVoteCount {

    /**
     * Proposal과 1:1 관계 - 같은 ID 사용
     */
    @Id
    private Integer proposalId;

    /**
     * 연관된 제안
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", insertable = false, updatable = false)
    private Proposal proposal;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version")
    private Long version = 0L;

    /**
     * 찬성 투표 총 토큰 수 (Wei 단위로 저장)
     */
    @Column(name = "for_votes", precision = 30, scale = 0, nullable = false)
    @NotNull
    @Builder.Default
    private BigInteger forVotes = BigInteger.ZERO;

    /**
     * 반대 투표 총 토큰 수 (Wei 단위로 저장)
     */
    @Column(name = "against_votes", precision = 30, scale = 0, nullable = false)
    @NotNull
    @Builder.Default
    private BigInteger againstVotes = BigInteger.ZERO;

    /**
     * 총 투표자 수
     */
    @Column(name = "total_voters", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer totalVoters = 0;

    /**
     * 찬성 투표자 수
     */
    @Column(name = "for_voters", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer forVoters = 0;

    /**
     * 반대 투표자 수
     */
    @Column(name = "against_voters", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer againstVoters = 0;

    /**
     * 마지막 업데이트 시간
     */
    @Column(name = "last_updated", nullable = false)
    @NotNull
    private LocalDateTime lastUpdated;

    /**
     * 데이터베이스 레코드 생성 시간
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // =============== 계산된 필드들 (DB에 저장되지 않음) ===============

    /**
     * 총 투표 토큰 수 계산
     */
    public BigInteger getTotalVotes() {
        return forVotes.add(againstVotes);
    }

    /**
     * 찬성 비율 계산 (0.0 ~ 100.0)
     */
    public double getForPercentage() {
        BigInteger total = getTotalVotes();
        if (total.equals(BigInteger.ZERO)) {
            return 0.0;
        }
        
        BigDecimal forDecimal = new BigDecimal(forVotes);
        BigDecimal totalDecimal = new BigDecimal(total);
        BigDecimal percentage = forDecimal
            .multiply(BigDecimal.valueOf(100))
            .divide(totalDecimal, 2, RoundingMode.HALF_UP);
        
        return percentage.doubleValue();
    }

    /**
     * 반대 비율 계산 (0.0 ~ 100.0)
     */
    public double getAgainstPercentage() {
        return 100.0 - getForPercentage();
    }

    /**
     * Wei 단위의 토큰을 Ether 단위로 변환 (표시용)
     */
    public BigDecimal getForVotesInEther() {
        return new BigDecimal(forVotes).divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
    }

    /**
     * Wei 단위의 토큰을 Ether 단위로 변환 (표시용)
     */
    public BigDecimal getAgainstVotesInEther() {
        return new BigDecimal(againstVotes).divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
    }

    /**
     * 총 투표 토큰을 Ether 단위로 변환 (표시용)
     */
    public BigDecimal getTotalVotesInEther() {
        return new BigDecimal(getTotalVotes()).divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
    }

    // =============== 비즈니스 로직 메서드 ===============

    /**
     * 찬성 투표 추가
     */
    public void addForVote(BigInteger votingPower) {
        this.forVotes = this.forVotes.add(votingPower);
        this.forVoters++;
        this.totalVoters++;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 반대 투표 추가
     */
    public void addAgainstVote(BigInteger votingPower) {
        this.againstVotes = this.againstVotes.add(votingPower);
        this.againstVoters++;
        this.totalVoters++;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 투표 여부 확인
     */
    public boolean hasAnyVotes() {
        return totalVoters > 0;
    }

    /**
     * 제안이 통과되었는지 확인 (찬성 > 반대)
     */
    public boolean isPassed() {
        return forVotes.compareTo(againstVotes) > 0;
    }

    /**
     * 투표가 무승부인지 확인
     */
    public boolean isTied() {
        return forVotes.equals(againstVotes) && hasAnyVotes();
    }

    /**
     * 특정 임계값 이상의 찬성률을 얻었는지 확인
     */
    public boolean hasPassedWithThreshold(double thresholdPercentage) {
        return getForPercentage() >= thresholdPercentage;
    }

    /**
     * 요약 정보 문자열 생성
     */
    public String getSummary() {
        return String.format("투표자 %d명, 찬성 %.1f%% (%s BLOOM), 반대 %.1f%% (%s BLOOM)",
            totalVoters,
            getForPercentage(),
            getForVotesInEther().setScale(2, RoundingMode.HALF_UP),
            getAgainstPercentage(),
            getAgainstVotesInEther().setScale(2, RoundingMode.HALF_UP));
    }
}