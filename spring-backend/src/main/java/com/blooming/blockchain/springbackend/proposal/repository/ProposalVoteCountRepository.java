package com.blooming.blockchain.springbackend.proposal.repository;

import com.blooming.blockchain.springbackend.proposal.entity.ProposalVoteCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ProposalVoteCount Repository - 제안 투표 집계 데이터 접근 계층
 */
@Repository
public interface ProposalVoteCountRepository extends JpaRepository<ProposalVoteCount, Long> {

    // =============== 기본 조회 메서드 ===============

    /**
     * 제안 ID로 투표 집계 조회
     */
    Optional<ProposalVoteCount> findByProposalId(Long proposalId);

    /**
     * 제안 ID들에 대한 투표 집계 일괄 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.proposalId IN :proposalIds")
    List<ProposalVoteCount> findByProposalIds(@Param("proposalIds") List<Long> proposalIds);

    // =============== 투표 현황별 조회 메서드 ===============

    /**
     * 투표가 있는 제안들의 집계 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.totalVoters > 0 ORDER BY pvc.totalVoters DESC")
    List<ProposalVoteCount> findProposalsWithVotes();

    /**
     * 투표가 없는 제안들의 집계 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.totalVoters = 0")
    List<ProposalVoteCount> findProposalsWithoutVotes();

    /**
     * 특정 투표자 수 이상인 제안들 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.totalVoters >= :minVoters ORDER BY pvc.totalVoters DESC")
    List<ProposalVoteCount> findProposalsWithMinVoters(@Param("minVoters") Integer minVoters);

    // =============== 투표 결과별 조회 메서드 ===============

    /**
     * 찬성이 우세한 제안들 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.forVotes > pvc.againstVotes ORDER BY pvc.forVotes DESC")
    List<ProposalVoteCount> findProposalsWithForMajority();

    /**
     * 반대가 우세한 제안들 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.againstVotes > pvc.forVotes ORDER BY pvc.againstVotes DESC")
    List<ProposalVoteCount> findProposalsWithAgainstMajority();

    /**
     * 동점인 제안들 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.forVotes = pvc.againstVotes AND pvc.totalVoters > 0")
    List<ProposalVoteCount> findTiedProposals();

    // =============== 투표량 기준 조회 메서드 ===============

    /**
     * 특정 투표량 이상의 찬성을 받은 제안들 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.forVotes >= :minForVotes ORDER BY pvc.forVotes DESC")
    List<ProposalVoteCount> findProposalsWithMinForVotes(@Param("minForVotes") BigInteger minForVotes);

    /**
     * 총 투표량이 특정 값 이상인 제안들 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE (pvc.forVotes + pvc.againstVotes) >= :minTotalVotes ORDER BY (pvc.forVotes + pvc.againstVotes) DESC")
    List<ProposalVoteCount> findProposalsWithMinTotalVotes(@Param("minTotalVotes") BigInteger minTotalVotes);

    // =============== 시간 기준 조회 메서드 ===============

    /**
     * 최근 업데이트된 투표 집계 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.lastUpdated >= :since ORDER BY pvc.lastUpdated DESC")
    List<ProposalVoteCount> findRecentlyUpdated(@Param("since") LocalDateTime since);

    /**
     * 특정 기간 내에 투표가 있었던 제안들 조회
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc WHERE pvc.lastUpdated BETWEEN :startTime AND :endTime ORDER BY pvc.lastUpdated DESC")
    List<ProposalVoteCount> findUpdatedBetween(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    // =============== 통계 조회 메서드 ===============

    /**
     * 전체 투표 수 합계
     */
    @Query("SELECT SUM(pvc.totalVoters) FROM ProposalVoteCount pvc")
    Long getTotalVoteCount();

    /**
     * 전체 찬성 투표량 합계
     */
    @Query("SELECT SUM(pvc.forVotes) FROM ProposalVoteCount pvc")
    BigInteger getTotalForVotes();

    /**
     * 전체 반대 투표량 합계
     */
    @Query("SELECT SUM(pvc.againstVotes) FROM ProposalVoteCount pvc")
    BigInteger getTotalAgainstVotes();

    /**
     * 평균 투표자 수
     */
    @Query("SELECT AVG(CAST(pvc.totalVoters AS double)) FROM ProposalVoteCount pvc WHERE pvc.totalVoters > 0")
    Double getAverageVoterCount();

    // =============== 순위 조회 메서드 ===============

    /**
     * 가장 많은 투표를 받은 제안들 (상위 N개)
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc ORDER BY pvc.totalVoters DESC")
    List<ProposalVoteCount> findTopProposalsByVoteCount(org.springframework.data.domain.Pageable pageable);

    /**
     * 가장 많은 찬성 투표를 받은 제안들 (상위 N개)
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc ORDER BY pvc.forVotes DESC")
    List<ProposalVoteCount> findTopProposalsByForVotes(org.springframework.data.domain.Pageable pageable);

    /**
     * 가장 많은 총 투표량을 받은 제안들 (상위 N개)
     */
    @Query("SELECT pvc FROM ProposalVoteCount pvc ORDER BY (pvc.forVotes + pvc.againstVotes) DESC")
    List<ProposalVoteCount> findTopProposalsByTotalVotePower(org.springframework.data.domain.Pageable pageable);

    // =============== 업데이트 메서드 ===============

    /**
     * 찬성 투표 추가 (원자적 업데이트)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProposalVoteCount pvc SET pvc.forVotes = pvc.forVotes + :votingPower, pvc.forVoters = pvc.forVoters + 1, pvc.totalVoters = pvc.totalVoters + 1, pvc.lastUpdated = :updateTime WHERE pvc.proposalId = :proposalId")
    int addForVote(@Param("proposalId") Long proposalId, 
                   @Param("votingPower") BigInteger votingPower, 
                   @Param("updateTime") LocalDateTime updateTime);

    /**
     * 반대 투표 추가 (원자적 업데이트)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProposalVoteCount pvc SET pvc.againstVotes = pvc.againstVotes + :votingPower, pvc.againstVoters = pvc.againstVoters + 1, pvc.totalVoters = pvc.totalVoters + 1, pvc.lastUpdated = :updateTime WHERE pvc.proposalId = :proposalId")
    int addAgainstVote(@Param("proposalId") Long proposalId, 
                       @Param("votingPower") BigInteger votingPower, 
                       @Param("updateTime") LocalDateTime updateTime);

    /**
     * 투표 집계 완전 재계산 (동기화용)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProposalVoteCount pvc SET pvc.forVotes = :forVotes, pvc.againstVotes = :againstVotes, pvc.forVoters = :forVoters, pvc.againstVoters = :againstVoters, pvc.totalVoters = :totalVoters, pvc.lastUpdated = :updateTime WHERE pvc.proposalId = :proposalId")
    int updateVoteCounts(@Param("proposalId") Long proposalId,
                        @Param("forVotes") BigInteger forVotes,
                        @Param("againstVotes") BigInteger againstVotes,
                        @Param("forVoters") Integer forVoters,
                        @Param("againstVoters") Integer againstVoters,
                        @Param("totalVoters") Integer totalVoters,
                        @Param("updateTime") LocalDateTime updateTime);

    // =============== 존재 여부 확인 메서드 ===============

    /**
     * 특정 제안의 투표 집계가 존재하는지 확인
     */
    boolean existsByProposalId(Long proposalId);

    /**
     * 투표가 있는 제안인지 확인
     */
    @Query("SELECT CASE WHEN pvc.totalVoters > 0 THEN true ELSE false END FROM ProposalVoteCount pvc WHERE pvc.proposalId = :proposalId")
    Boolean hasVotes(@Param("proposalId") Long proposalId);

    // =============== 삭제 메서드 ===============

    /**
     * 특정 제안의 투표 집계 삭제
     */
    void deleteByProposalId(Long proposalId);
}