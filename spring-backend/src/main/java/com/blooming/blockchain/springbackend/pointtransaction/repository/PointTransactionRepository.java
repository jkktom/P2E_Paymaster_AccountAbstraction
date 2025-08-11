package com.blooming.blockchain.springbackend.pointtransaction.repository;

import com.blooming.blockchain.springbackend.pointtransaction.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Integer> {

    // Find all transactions for a user (paginated)
    Page<PointTransaction> findByUserGoogleIdOrderByCreatedAtDesc(String userGoogleId, Pageable pageable);

    // Find transactions by user and point type
    Page<PointTransaction> findByUserGoogleIdAndPointTypeIdOrderByCreatedAtDesc(String userGoogleId, Byte pointTypeId, Pageable pageable);

    // Find transactions by user and source ID
    Page<PointTransaction> findByUserGoogleIdAndSourceIdOrderByCreatedAtDesc(String userGoogleId, Byte sourceId, Pageable pageable);

    // Find transactions by user and status
    Page<PointTransaction> findByUserGoogleIdAndTransactionStatusIdOrderByCreatedAtDesc(String userGoogleId, Byte transactionStatusId, Pageable pageable);

    // Find transactions by user within date range
    Page<PointTransaction> findByUserGoogleIdAndCreatedAtBetweenOrderByCreatedAtDesc(String userGoogleId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find pending transactions for a user
    List<PointTransaction> findByUserGoogleIdAndTransactionStatusId(String userGoogleId, Byte transactionStatusId);

    // Count total transactions for a user
    long countByUserGoogleId(String userGoogleId);

    // Count transactions by user and point type
    long countByUserGoogleIdAndPointTypeId(String userGoogleId, Byte pointTypeId);

    // Count transactions by user and source
    long countByUserGoogleIdAndSourceId(String userGoogleId, Byte sourceId);

    // Get total main points earned by user
    @Query("SELECT COALESCE(SUM(pt.mainEarnAmount), 0) FROM PointTransaction pt WHERE pt.userGoogleId = :userGoogleId AND pt.transactionStatusId = 2")
    Integer getTotalMainPointsEarned(@Param("userGoogleId") String userGoogleId);

    // Get total sub points earned by user
    @Query("SELECT COALESCE(SUM(pt.subEarnAmount), 0) FROM PointTransaction pt WHERE pt.userGoogleId = :userGoogleId AND pt.transactionStatusId = 2")
    Integer getTotalSubPointsEarned(@Param("userGoogleId") String userGoogleId);

    // Get total main points exchanged by user
    @Query("SELECT COALESCE(SUM(pt.mainExchangedAmount), 0) FROM PointTransaction pt WHERE pt.userGoogleId = :userGoogleId AND pt.transactionStatusId = 2")
    Integer getTotalMainPointsExchanged(@Param("userGoogleId") String userGoogleId);

    // Get total sub points converted by user
    @Query("SELECT COALESCE(SUM(pt.subConvertedAmount), 0) FROM PointTransaction pt WHERE pt.userGoogleId = :userGoogleId AND pt.transactionStatusId = 2")
    Integer getTotalSubPointsConverted(@Param("userGoogleId") String userGoogleId);

    // Get recent earning transactions (last 10)
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userGoogleId = :userGoogleId AND pt.sourceId IN (1, 2, 3, 4, 7, 8, 9, 10) AND pt.transactionStatusId = 2 ORDER BY pt.createdAt DESC")
    List<PointTransaction> findRecentEarningTransactions(@Param("userGoogleId") String userGoogleId, Pageable pageable);

    // Get recent spending transactions (last 10)
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userGoogleId = :userGoogleId AND pt.sourceId IN (5, 6, 11, 12) AND pt.transactionStatusId = 2 ORDER BY pt.createdAt DESC")
    List<PointTransaction> findRecentSpendingTransactions(@Param("userGoogleId") String userGoogleId, Pageable pageable);

    // Get transactions by user and multiple source IDs
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userGoogleId = :userGoogleId AND pt.sourceId IN :sourceIds ORDER BY pt.createdAt DESC")
    Page<PointTransaction> findByUserGoogleIdAndSourceIdIn(@Param("userGoogleId") String userGoogleId, @Param("sourceIds") List<Byte> sourceIds, Pageable pageable);

    // Check if user has any pending transactions
    boolean existsByUserGoogleIdAndTransactionStatusId(String userGoogleId, Byte transactionStatusId);
}