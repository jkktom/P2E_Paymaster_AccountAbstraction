package com.blooming.blockchain.springbackend.proposal.repository;

import com.blooming.blockchain.springbackend.proposal.entity.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Proposal Repository - 제안 데이터 접근 계층
 */
@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    // =============== 기본 조회 메서드 ===============

    /**
     * 블록체인 제안 ID로 조회
     */
    Optional<Proposal> findByBlockchainProposalId(Integer blockchainProposalId);

    /**
     * 블록체인 제안 ID 존재 여부 확인
     */
    boolean existsByBlockchainProposalId(Integer blockchainProposalId);

    // =============== 상태별 조회 메서드 ===============

    /**
     * 활성 제안 조회 (실행되지 않고, 취소되지 않고, 마감되지 않은)
     */
    @Query("SELECT p FROM Proposal p WHERE p.executed = false AND p.canceled = false AND p.deadline > :now ORDER BY p.createdAt DESC")
    List<Proposal> findActiveProposals(@Param("now") LocalDateTime now);

    /**
     * 활성 제안 페이징 조회
     */
    @Query("SELECT p FROM Proposal p WHERE p.executed = false AND p.canceled = false AND p.deadline > :now")
    Page<Proposal> findActiveProposals(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 완료된 제안 조회 (실행되었거나 취소된)
     */
    @Query("SELECT p FROM Proposal p WHERE p.executed = true OR p.canceled = true ORDER BY p.updatedAt DESC")
    Page<Proposal> findCompletedProposals(Pageable pageable);

    /**
     * 만료된 제안 조회 (마감일이 지났지만 실행되지 않은)
     */
    @Query("SELECT p FROM Proposal p WHERE p.executed = false AND p.canceled = false AND p.deadline <= :now ORDER BY p.deadline DESC")
    Page<Proposal> findExpiredProposals(@Param("now") LocalDateTime now, Pageable pageable);

    // =============== 제안자별 조회 메서드 ===============

    /**
     * 특정 사용자가 생성한 제안 조회 (Google ID 기준)
     */
    List<Proposal> findByProposerGoogleIdOrderByCreatedAtDesc(String proposerGoogleId);

    /**
     * 특정 사용자가 생성한 제안 페이징 조회
     */
    Page<Proposal> findByProposerGoogleId(String proposerGoogleId, Pageable pageable);

    /**
     * 특정 지갑 주소가 생성한 제안 조회
     */
    List<Proposal> findByProposerAddressOrderByCreatedAtDesc(String proposerAddress);

    // =============== 시간 범위 조회 메서드 ===============

    /**
     * 특정 기간 내 생성된 제안 조회
     */
    @Query("SELECT p FROM Proposal p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Proposal> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 기간 내 마감되는 제안 조회
     */
    @Query("SELECT p FROM Proposal p WHERE p.deadline BETWEEN :startDate AND :endDate AND p.executed = false AND p.canceled = false ORDER BY p.deadline ASC")
    List<Proposal> findByDeadlineBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    // =============== 검색 메서드 ===============

    /**
     * 제안 설명에 특정 키워드가 포함된 제안 검색
     */
    @Query("SELECT p FROM Proposal p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.createdAt DESC")
    Page<Proposal> searchByDescription(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 활성 제안 중에서 키워드 검색
     */
    @Query("SELECT p FROM Proposal p WHERE p.executed = false AND p.canceled = false AND p.deadline > :now AND LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.createdAt DESC")
    Page<Proposal> searchActiveProposals(@Param("keyword") String keyword, @Param("now") LocalDateTime now, Pageable pageable);

    // =============== 통계 조회 메서드 ===============

    /**
     * 전체 제안 수 조회
     */
    @Query("SELECT COUNT(p) FROM Proposal p")
    long countAllProposals();

    /**
     * 활성 제안 수 조회
     */
    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.executed = false AND p.canceled = false AND p.deadline > :now")
    long countActiveProposals(@Param("now") LocalDateTime now);

    /**
     * 특정 사용자가 생성한 제안 수 조회
     */
    long countByProposerGoogleId(String proposerGoogleId);

    /**
     * 실행된 제안 수 조회
     */
    long countByExecutedTrue();

    /**
     * 취소된 제안 수 조회
     */
    long countByCanceledTrue();

    // =============== 복합 조회 메서드 ===============

    /**
     * 특정 사용자의 활성 제안 조회
     */
    @Query("SELECT p FROM Proposal p WHERE p.proposerGoogleId = :googleId AND p.executed = false AND p.canceled = false AND p.deadline > :now ORDER BY p.createdAt DESC")
    List<Proposal> findActiveProposalsByUser(@Param("googleId") String googleId, @Param("now") LocalDateTime now);

    /**
     * 곧 마감되는 제안 조회 (지정된 시간 내에 마감)
     */
    @Query("SELECT p FROM Proposal p WHERE p.executed = false AND p.canceled = false AND p.deadline BETWEEN :now AND :soonDeadline ORDER BY p.deadline ASC")
    List<Proposal> findProposalsEndingSoon(@Param("now") LocalDateTime now, @Param("soonDeadline") LocalDateTime soonDeadline);

    /**
     * 최근 생성된 제안 조회 (제한된 개수)
     */
    @Query("SELECT p FROM Proposal p ORDER BY p.createdAt DESC")
    List<Proposal> findRecentProposals(Pageable pageable);

    // =============== 삭제 및 업데이트 메서드 ===============

    /**
     * 만료된 제안들의 상태 확인용 조회
     * (배치 작업에서 사용 - 만료되었지만 아직 처리되지 않은 제안들)
     */
    @Query("SELECT p FROM Proposal p WHERE p.executed = false AND p.canceled = false AND p.deadline <= :cutoffTime")
    List<Proposal> findUnprocessedExpiredProposals(@Param("cutoffTime") LocalDateTime cutoffTime);

    // =============== 존재 여부 확인 메서드 ===============

    /**
     * 특정 사용자가 활성 제안을 가지고 있는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Proposal p WHERE p.proposerGoogleId = :googleId AND p.executed = false AND p.canceled = false AND p.deadline > :now")
    boolean hasActiveProposals(@Param("googleId") String googleId, @Param("now") LocalDateTime now);
}