package com.blooming.blockchain.springbackend.pointtransaction.service;

import com.blooming.blockchain.springbackend.pointtransaction.entity.PointTransaction;
import com.blooming.blockchain.springbackend.pointtransaction.repository.PointTransactionRepository;
import com.blooming.blockchain.springbackend.userdetail.service.UserPointTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointTransactionServiceTest {

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private UserPointTokenService userPointTokenService;

    @InjectMocks
    private PointTransactionService pointTransactionService;

    private String testUserGoogleId;

    @BeforeEach
    void setUp() {
        testUserGoogleId = "test-google-id-123";
    }

    @Test
    void earnMainPoints_WhenSuccessful_ShouldConfirmTransaction() {
        // Given
        Integer amount = 100;
        Byte sourceId = 1;
        String description = "Test earning";
        
        PointTransaction mockTransaction = PointTransaction.createMainEarn(testUserGoogleId, amount, sourceId, description);
        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenReturn(mockTransaction);
        when(userPointTokenService.addMainPoints(testUserGoogleId, amount))
                .thenReturn(true);

        // When
        PointTransaction result = pointTransactionService.earnMainPoints(testUserGoogleId, amount, sourceId, description);

        // Then
        assertThat(result).isNotNull();
        verify(pointTransactionRepository, times(2)).save(any(PointTransaction.class));
        verify(userPointTokenService).addMainPoints(testUserGoogleId, amount);
    }

    @Test
    void earnMainPoints_WhenBalanceUpdateFails_ShouldFailTransaction() {
        // Given
        Integer amount = 100;
        Byte sourceId = 1;
        
        PointTransaction mockTransaction = PointTransaction.createMainEarn(testUserGoogleId, amount, sourceId);
        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenReturn(mockTransaction);
        when(userPointTokenService.addMainPoints(testUserGoogleId, amount))
                .thenReturn(false);

        // When
        PointTransaction result = pointTransactionService.earnMainPoints(testUserGoogleId, amount, sourceId);

        // Then
        assertThat(result).isNotNull();
        verify(pointTransactionRepository, times(2)).save(any(PointTransaction.class));
        verify(userPointTokenService).addMainPoints(testUserGoogleId, amount);
    }

    @Test
    void earnSubPoints_WhenSuccessful_ShouldConfirmTransaction() {
        // Given
        Integer amount = 200;
        Byte sourceId = 7;
        
        PointTransaction mockTransaction = PointTransaction.createSubEarn(testUserGoogleId, amount, sourceId);
        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenReturn(mockTransaction);
        when(userPointTokenService.addSubPoints(testUserGoogleId, amount))
                .thenReturn(true);

        // When
        PointTransaction result = pointTransactionService.earnSubPoints(testUserGoogleId, amount, sourceId);

        // Then
        assertThat(result).isNotNull();
        verify(pointTransactionRepository, times(2)).save(any(PointTransaction.class));
        verify(userPointTokenService).addSubPoints(testUserGoogleId, amount);
    }

    @Test
    void convertSubToMainPoints_WhenSuccessful_ShouldConfirmTransaction() {
        // Given
        Integer subPointsSpent = 100;
        Integer expectedMainPoints = subPointsSpent / PointTransaction.getSubToMainRatio();
        
        PointTransaction mockTransaction = PointTransaction.createSubToMainConversion(testUserGoogleId, subPointsSpent);
        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenReturn(mockTransaction);
        when(userPointTokenService.convertSubToMainPoints(testUserGoogleId, subPointsSpent, expectedMainPoints))
                .thenReturn(true);

        // When
        PointTransaction result = pointTransactionService.convertSubToMainPoints(testUserGoogleId, subPointsSpent);

        // Then
        assertThat(result).isNotNull();
        verify(pointTransactionRepository, times(2)).save(any(PointTransaction.class));
        verify(userPointTokenService).convertSubToMainPoints(testUserGoogleId, subPointsSpent, expectedMainPoints);
    }

    @Test
    void convertSubToMainPoints_WhenInsufficientPoints_ShouldReturnNull() {
        // Given
        Integer subPointsSpent = 5; // Less than ratio, will result in 0 main points

        // When
        PointTransaction result = pointTransactionService.convertSubToMainPoints(testUserGoogleId, subPointsSpent);

        // Then
        assertThat(result).isNull();
        verify(pointTransactionRepository, never()).save(any());
        verify(userPointTokenService, never()).convertSubToMainPoints(anyString(), anyInt(), anyInt());
    }

    @Test
    void exchangeMainPointsToTokens_WhenSuccessful_ShouldConfirmTransaction() {
        // Given
        Integer mainPointsSpent = 100;
        Long expectedTokens = (long) (mainPointsSpent / PointTransaction.getMainToTokenRatio());
        
        PointTransaction mockTransaction = PointTransaction.createMainToTokenExchange(testUserGoogleId, mainPointsSpent);
        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenReturn(mockTransaction);
        when(userPointTokenService.exchangeMainPointsToTokens(testUserGoogleId, mainPointsSpent, expectedTokens))
                .thenReturn(true);

        // When
        PointTransaction result = pointTransactionService.exchangeMainPointsToTokens(testUserGoogleId, mainPointsSpent);

        // Then
        assertThat(result).isNotNull();
        verify(pointTransactionRepository, times(2)).save(any(PointTransaction.class));
        verify(userPointTokenService).exchangeMainPointsToTokens(testUserGoogleId, mainPointsSpent, expectedTokens);
    }

    @Test
    void exchangeMainPointsToTokens_WhenInsufficientPoints_ShouldReturnNull() {
        // Given
        Integer mainPointsSpent = 5; // Less than ratio, will result in 0 tokens

        // When
        PointTransaction result = pointTransactionService.exchangeMainPointsToTokens(testUserGoogleId, mainPointsSpent);

        // Then
        assertThat(result).isNull();
        verify(pointTransactionRepository, never()).save(any());
        verify(userPointTokenService, never()).exchangeMainPointsToTokens(anyString(), anyInt(), anyLong());
    }

    @Test
    void getTransactionHistory_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = Pageable.ofSize(10);
        List<PointTransaction> transactions = List.of(
                PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1),
                PointTransaction.createSubEarn(testUserGoogleId, 200, (byte) 7)
        );
        Page<PointTransaction> mockPage = new PageImpl<>(transactions, pageable, transactions.size());
        
        when(pointTransactionRepository.findByUserGoogleIdOrderByCreatedAtDesc(testUserGoogleId, pageable))
                .thenReturn(mockPage);

        // When
        Page<PointTransaction> result = pointTransactionService.getTransactionHistory(testUserGoogleId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(pointTransactionRepository).findByUserGoogleIdOrderByCreatedAtDesc(testUserGoogleId, pageable);
    }

    @Test
    void getPendingTransactions_ShouldReturnPendingTransactions() {
        // Given
        List<PointTransaction> pendingTransactions = List.of(
                PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1)
        );
        
        when(pointTransactionRepository.findByUserGoogleIdAndTransactionStatusId(testUserGoogleId, (byte) 1))
                .thenReturn(pendingTransactions);

        // When
        List<PointTransaction> result = pointTransactionService.getPendingTransactions(testUserGoogleId);

        // Then
        assertThat(result).hasSize(1);
        verify(pointTransactionRepository).findByUserGoogleIdAndTransactionStatusId(testUserGoogleId, (byte) 1);
    }

    @Test
    void getUserStatistics_ShouldReturnCompleteStatistics() {
        // Given
        when(pointTransactionRepository.getTotalMainPointsEarned(testUserGoogleId))
                .thenReturn(500);
        when(pointTransactionRepository.getTotalSubPointsEarned(testUserGoogleId))
                .thenReturn(1000);
        when(pointTransactionRepository.getTotalMainPointsExchanged(testUserGoogleId))
                .thenReturn(100);
        when(pointTransactionRepository.getTotalSubPointsConverted(testUserGoogleId))
                .thenReturn(200);
        when(pointTransactionRepository.countByUserGoogleId(testUserGoogleId))
                .thenReturn(25L);

        // When
        PointTransactionService.UserPointStatistics result = pointTransactionService.getUserStatistics(testUserGoogleId);

        // Then
        assertThat(result.getTotalMainPointsEarned()).isEqualTo(500);
        assertThat(result.getTotalSubPointsEarned()).isEqualTo(1000);
        assertThat(result.getTotalMainPointsExchanged()).isEqualTo(100);
        assertThat(result.getTotalSubPointsConverted()).isEqualTo(200);
        assertThat(result.getTotalTransactions()).isEqualTo(25L);
    }

    @Test
    void getTransactionById_WhenExists_ShouldReturnTransaction() {
        // Given
        Integer transactionId = 1;
        PointTransaction mockTransaction = PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1);
        when(pointTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(mockTransaction));

        // When
        Optional<PointTransaction> result = pointTransactionService.getTransactionById(transactionId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockTransaction);
    }

    @Test
    void hasPendingTransactions_WhenPendingExists_ShouldReturnTrue() {
        // Given
        when(pointTransactionRepository.existsByUserGoogleIdAndTransactionStatusId(testUserGoogleId, (byte) 1))
                .thenReturn(true);

        // When
        boolean result = pointTransactionService.hasPendingTransactions(testUserGoogleId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasPendingTransactions_WhenNoPending_ShouldReturnFalse() {
        // Given
        when(pointTransactionRepository.existsByUserGoogleIdAndTransactionStatusId(testUserGoogleId, (byte) 1))
                .thenReturn(false);

        // When
        boolean result = pointTransactionService.hasPendingTransactions(testUserGoogleId);

        // Then
        assertThat(result).isFalse();
    }
}