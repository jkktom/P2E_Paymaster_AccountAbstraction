package com.blooming.blockchain.springbackend.proposal.repository;

import com.blooming.blockchain.springbackend.proposal.entity.UserVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserVote Repository - 사용자 투표 기록 데이터 접근 계층
 */
@Repository
public interface UserVoteRepository extends JpaRepository<UserVote, Long> {

    // =============== 기본 조회 메서드 ===============

    /**
     * 특정 제안에 대한 특정 사용자의 투표 조회
     */
    Optional<UserVote> findByProposalIdAndUserGoogleId(Long proposalId, String userGoogleId);

    /**
     * 특정 제안에 대한 특정 사용자가 투표했는지 확인
     */
    boolean existsByProposalIdAndUserGoogleId(Long proposalId, String userGoogleId);

    // =============== 사용자별 조회 메서드 ===============

    /**
     * 특정 사용자의 모든 투표 기록 조회 (최신순)
     */
    List<UserVote> findByUserGoogleIdOrderByVotedAtDesc(String userGoogleId);

    /**
     * 특정 사용자의 투표 기록 페이징 조회
     */
    Page<UserVote> findByUserGoogleId(String userGoogleId, Pageable pageable);

    /**
     * 특정 사용자의 찬성 투표 기록 조회
     */
    List<UserVote> findByUserGoogleIdAndSupportTrueOrderByVotedAtDesc(String userGoogleId);

    /**
     * 특정 사용자의 반대 투표 기록 조회
     */
    List<UserVote> findByUserGoogleIdAndSupportFalseOrderByVotedAtDesc(String userGoogleId);

    /**
     * 특정 사용자의 투표 수 조회
     */
    long countByUserGoogleId(String userGoogleId);

    /**
     * 특정 사용자의 찬성 투표 수 조회
     */
    long countByUserGoogleIdAndSupportTrue(String userGoogleId);

    /**
     * 특정 사용자의 반대 투표 수 조회
     */
    long countByUserGoogleIdAndSupportFalse(String userGoogleId);

    // =============== 제안별 조회 메서드 ===============

    /**
     * 특정 제안의 모든 투표 기록 조회
     */
    List<UserVote> findByProposalIdOrderByVotedAtDesc(Long proposalId);

    /**
     * 특정 제안의 투표 기록 페이징 조회
     */
    Page<UserVote> findByProposalId(Long proposalId, Pageable pageable);

    /**
     * 특정 제안의 찬성 투표 기록 조회
     */
    List<UserVote> findByProposalIdAndSupportTrueOrderByVotingPowerDesc(Long proposalId);

    /**
     * 특정 제안의 반대 투표 기록 조회
     */
    List<UserVote> findByProposalIdAndSupportFalseOrderByVotingPowerDesc(Long proposalId);

    /**
     * 특정 제안의 투표자 수 조회
     */
    long countByProposalId(Long proposalId);

    /**
     * 특정 제안의 찬성 투표자 수 조회
     */
    long countByProposalIdAndSupportTrue(Long proposalId);

    /**
     * 특정 제안의 반대 투표자 수 조회
     */
    long countByProposalIdAndSupportFalse(Long proposalId);

    // =============== 투표력별 조회 메서드 ===============

    /**
     * 특정 투표력 이상으로 투표한 기록 조회
     */
    @Query("SELECT uv FROM UserVote uv WHERE uv.votingPower >= :minPower ORDER BY uv.votingPower DESC")
    List<UserVote> findByVotingPowerGreaterThanEqual(@Param("minPower") BigInteger minPower);

    /**
     * 특정 제안에서 가장 큰 투표력을 가진 투표들 조회 (상위 N개)
     */
    @Query("SELECT uv FROM UserVote uv WHERE uv.proposalId = :proposalId ORDER BY uv.votingPower DESC")
    List<UserVote> findTopVotesByPower(@Param("proposalId") Long proposalId, Pageable pageable);

    /**
     * 특정 사용자의 평균 투표력 조회
     */
    @Query("SELECT AVG(CAST(uv.votingPower AS double)) FROM UserVote uv WHERE uv.userGoogleId = :userGoogleId")
    Double getAverageVotingPowerByUser(@Param("userGoogleId") String userGoogleId);

    // =============== 시간별 조회 메서드 ===============

    /**
     * 특정 기간 내 투표 기록 조회
     */
    @Query("SELECT uv FROM UserVote uv WHERE uv.votedAt BETWEEN :startTime AND :endTime ORDER BY uv.votedAt DESC")
    List<UserVote> findByVotedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 최근 투표 기록 조회 (지정된 시간 이후)
     */
    @Query("SELECT uv FROM UserVote uv WHERE uv.votedAt >= :since ORDER BY uv.votedAt DESC")
    List<UserVote> findRecentVotes(@Param("since") LocalDateTime since);

    /**
     * 특정 사용자의 최근 투표 기록 조회
     */
    @Query("SELECT uv FROM UserVote uv WHERE uv.userGoogleId = :userGoogleId AND uv.votedAt >= :since ORDER BY uv.votedAt DESC")
    List<UserVote> findRecentVotesByUser(@Param("userGoogleId") String userGoogleId, 
                                        @Param("since") LocalDateTime since);

    // =============== 집계 조회 메서드 ===============

    /**
     * 특정 제안의 찬성 투표력 합계
     */
    @Query("SELECT COALESCE(SUM(uv.votingPower), 0) FROM UserVote uv WHERE uv.proposalId = :proposalId AND uv.support = true")
    BigInteger sumForVotesByProposal(@Param("proposalId") Long proposalId);

    /**
     * 특정 제안의 반대 투표력 합계
     */
    @Query("SELECT COALESCE(SUM(uv.votingPower), 0) FROM UserVote uv WHERE uv.proposalId = :proposalId AND uv.support = false")
    BigInteger sumAgainstVotesByProposal(@Param("proposalId") Long proposalId);

    /**
     * 특정 제안의 총 투표력 합계
     */
    @Query("SELECT COALESCE(SUM(uv.votingPower), 0) FROM UserVote uv WHERE uv.proposalId = :proposalId")
    BigInteger sumTotalVotesByProposal(@Param("proposalId") Long proposalId);

    /**
     * 특정 사용자의 총 투표력 합계
     */
    @Query("SELECT COALESCE(SUM(uv.votingPower), 0) FROM UserVote uv WHERE uv.userGoogleId = :userGoogleId")
    BigInteger sumVotingPowerByUser(@Param("userGoogleId") String userGoogleId);

    // =============== 지갑 주소별 조회 메서드 ===============

    /**
     * 특정 지갑 주소의 투표 기록 조회
     */
    List<UserVote> findByVoterWalletAddressOrderByVotedAtDesc(String walletAddress);

    /**
     * 특정 지갑 주소가 특정 제안에 투표했는지 확인
     */
    boolean existsByProposalIdAndVoterWalletAddress(Long proposalId, String walletAddress);

    // =============== 트랜잭션 해시별 조회 메서드 ===============

    /**
     * 트랜잭션 해시로 투표 기록 조회
     */
    Optional<UserVote> findByTxHash(String txHash);

    /**
     * 블록체인에서 확인된 투표 기록들 조회 (txHash가 있는)
     */
    @Query("SELECT uv FROM UserVote uv WHERE uv.txHash IS NOT NULL AND uv.txHash != '' ORDER BY uv.votedAt DESC")
    List<UserVote> findConfirmedVotes();

    /**
     * 블록체인에서 미확인된 투표 기록들 조회 (txHash가 없는)
     */
    @Query("SELECT uv FROM UserVote uv WHERE uv.txHash IS NULL OR uv.txHash = '' ORDER BY uv.votedAt DESC")
    List<UserVote> findUnconfirmedVotes();

    // =============== 복합 조회 메서드 ===============

    /**
     * 특정 사용자가 특정 기간 내에 투표한 제안들 조회
     */
    @Query("SELECT DISTINCT uv.proposalId FROM UserVote uv WHERE uv.userGoogleId = :userGoogleId AND uv.votedAt BETWEEN :startTime AND :endTime")
    List<Long> findProposalIdsVotedByUserInPeriod(@Param("userGoogleId") String userGoogleId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 특정 제안에 투표한 사용자들의 Google ID 목록 조회
     */
    @Query("SELECT DISTINCT uv.userGoogleId FROM UserVote uv WHERE uv.proposalId = :proposalId")
    List<String> findVotersByProposal(@Param("proposalId") Long proposalId);

    /**
     * 여러 제안에 모두 투표한 사용자들 조회
     */
    @Query("SELECT uv.userGoogleId FROM UserVote uv WHERE uv.proposalId IN :proposalIds GROUP BY uv.userGoogleId HAVING COUNT(DISTINCT uv.proposalId) = :proposalCount")
    List<String> findUsersWhoVotedOnAllProposals(@Param("proposalIds") List<Long> proposalIds, 
                                                 @Param("proposalCount") long proposalCount);

    // =============== 통계 메서드 ===============

    /**
     * 전체 투표 수 조회
     */
    @Query("SELECT COUNT(uv) FROM UserVote uv")
    long getTotalVoteCount();

    /**
     * 전체 찬성 투표 수 조회
     */
    long countBySupportTrue();

    /**
     * 전체 반대 투표 수 조회
     */
    long countBySupportFalse();

    /**
     * 활성 투표자 수 조회 (최소 한 번은 투표한 사용자 수)
     */
    @Query("SELECT COUNT(DISTINCT uv.userGoogleId) FROM UserVote uv")
    long getActiveVoterCount();

    /**
     * 특정 기간 내 투표자 수 조회
     */
    @Query("SELECT COUNT(DISTINCT uv.userGoogleId) FROM UserVote uv WHERE uv.votedAt BETWEEN :startTime AND :endTime")
    long getVoterCountInPeriod(@Param("startTime") LocalDateTime startTime, 
                              @Param("endTime") LocalDateTime endTime);

    // =============== 삭제 메서드 ===============

    /**
     * 특정 제안의 모든 투표 기록 삭제
     */
    void deleteByProposalId(Long proposalId);

    /**
     * 특정 사용자의 모든 투표 기록 삭제
     */
    void deleteByUserGoogleId(String userGoogleId);

    /**
     * 특정 사용자의 특정 제안 투표 기록 삭제
     */
    void deleteByProposalIdAndUserGoogleId(Long proposalId, String userGoogleId);
}