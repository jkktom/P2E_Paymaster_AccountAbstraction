package com.blooming.blockchain.springbackend.pointtransaction.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointTransactionTest {

    private final String testUserGoogleId = "test-google-id-123";

    @Test
    void createMainEarn_WithDescription_ShouldCreateCorrectTransaction() {
        // Given
        Integer amount = 100;
        Byte sourceId = 1;
        String description = "Task completion";

        // When
        PointTransaction transaction = PointTransaction.createMainEarn(testUserGoogleId, amount, sourceId, description);

        // Then
        assertThat(transaction.getUserGoogleId()).isEqualTo(testUserGoogleId);
        assertThat(transaction.getPointTypeId()).isEqualTo((byte) 1);
        assertThat(transaction.getMainEarnAmount()).isEqualTo(amount);
        assertThat(transaction.getSourceId()).isEqualTo(sourceId);
        assertThat(transaction.getDescription()).isEqualTo(description);
        assertThat(transaction.getTransactionStatusId()).isEqualTo((byte) 1); // PENDING
        assertThat(transaction.getSubEarnAmount()).isNull();
        assertThat(transaction.getMainExchangedAmount()).isNull();
        assertThat(transaction.getSubConvertedAmount()).isNull();
    }

    @Test
    void createMainEarn_WithoutDescription_ShouldCreateTransactionWithNullDescription() {
        // Given
        Integer amount = 100;
        Byte sourceId = 1;

        // When
        PointTransaction transaction = PointTransaction.createMainEarn(testUserGoogleId, amount, sourceId);

        // Then
        assertThat(transaction.getUserGoogleId()).isEqualTo(testUserGoogleId);
        assertThat(transaction.getPointTypeId()).isEqualTo((byte) 1);
        assertThat(transaction.getMainEarnAmount()).isEqualTo(amount);
        assertThat(transaction.getSourceId()).isEqualTo(sourceId);
        assertThat(transaction.getDescription()).isNull();
    }

    @Test
    void createSubEarn_ShouldCreateCorrectTransaction() {
        // Given
        Integer amount = 200;
        Byte sourceId = 7;

        // When
        PointTransaction transaction = PointTransaction.createSubEarn(testUserGoogleId, amount, sourceId);

        // Then
        assertThat(transaction.getUserGoogleId()).isEqualTo(testUserGoogleId);
        assertThat(transaction.getPointTypeId()).isEqualTo((byte) 2);
        assertThat(transaction.getSubEarnAmount()).isEqualTo(amount);
        assertThat(transaction.getSourceId()).isEqualTo(sourceId);
        assertThat(transaction.getMainEarnAmount()).isNull();
        assertThat(transaction.getMainExchangedAmount()).isNull();
        assertThat(transaction.getSubConvertedAmount()).isNull();
    }

    @Test
    void createSubToMainConversion_ShouldCreateCorrectTransaction() {
        // Given
        Integer subPointsSpent = 100;

        // When
        PointTransaction transaction = PointTransaction.createSubToMainConversion(testUserGoogleId, subPointsSpent);

        // Then
        assertThat(transaction.getUserGoogleId()).isEqualTo(testUserGoogleId);
        assertThat(transaction.getPointTypeId()).isEqualTo((byte) 2);
        assertThat(transaction.getSourceId()).isEqualTo((byte) 11); // SUB_CONVERSION
        assertThat(transaction.getSubConvertedAmount()).isEqualTo(subPointsSpent);
        assertThat(transaction.getMainEarnAmount()).isEqualTo(subPointsSpent / PointTransaction.getSubToMainRatio());
    }

    @Test
    void createMainToTokenExchange_ShouldCreateCorrectTransaction() {
        // Given
        Integer mainPointsSpent = 100;

        // When
        PointTransaction transaction = PointTransaction.createMainToTokenExchange(testUserGoogleId, mainPointsSpent);

        // Then
        assertThat(transaction.getUserGoogleId()).isEqualTo(testUserGoogleId);
        assertThat(transaction.getPointTypeId()).isEqualTo((byte) 1);
        assertThat(transaction.getSourceId()).isEqualTo((byte) 5); // MAIN_EXCHANGE
        assertThat(transaction.getMainExchangedAmount()).isEqualTo(mainPointsSpent);
        assertThat(transaction.getSubEarnAmount()).isNull();
        assertThat(transaction.getMainEarnAmount()).isNull();
        assertThat(transaction.getSubConvertedAmount()).isNull();
    }

    @Test
    void confirmTransaction_ShouldSetStatusToConfirmed() {
        // Given
        PointTransaction transaction = PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1);

        // When
        transaction.confirmTransaction();

        // Then
        assertThat(transaction.getTransactionStatusId()).isEqualTo((byte) 2); // CONFIRMED
        assertThat(transaction.isConfirmed()).isTrue();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.isFailed()).isFalse();
    }

    @Test
    void failTransaction_ShouldSetStatusToFailed() {
        // Given
        PointTransaction transaction = PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1);

        // When
        transaction.failTransaction();

        // Then
        assertThat(transaction.getTransactionStatusId()).isEqualTo((byte) 3); // FAILED
        assertThat(transaction.isFailed()).isTrue();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.isConfirmed()).isFalse();
    }

    @Test
    void getTransactionAmount_ForEarningTransaction_ShouldReturnEarnAmount() {
        // Given
        PointTransaction mainEarn = PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1);
        PointTransaction subEarn = PointTransaction.createSubEarn(testUserGoogleId, 200, (byte) 7);

        // When & Then
        assertThat(mainEarn.getTransactionAmount()).isEqualTo(100);
        assertThat(subEarn.getTransactionAmount()).isEqualTo(200);
    }

    @Test
    void getTransactionAmount_ForSpendingTransaction_ShouldReturnSpendAmount() {
        // Given
        PointTransaction conversion = PointTransaction.createSubToMainConversion(testUserGoogleId, 100);
        PointTransaction exchange = PointTransaction.createMainToTokenExchange(testUserGoogleId, 50);

        // When & Then
        assertThat(conversion.getTransactionAmount()).isEqualTo(100);
        assertThat(exchange.getTransactionAmount()).isEqualTo(50);
    }

    @Test
    void getTransactionType_ShouldReturnCorrectType() {
        // Given
        PointTransaction earning = PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1);
        PointTransaction spending = PointTransaction.createSubToMainConversion(testUserGoogleId, 100);

        // When & Then
        assertThat(earning.getTransactionType()).isEqualTo("EARNING");
        assertThat(spending.getTransactionType()).isEqualTo("SPENDING");
    }

    @Test
    void getPointType_ShouldReturnCorrectPointType() {
        // Given
        PointTransaction mainTransaction = PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1);
        PointTransaction subTransaction = PointTransaction.createSubEarn(testUserGoogleId, 200, (byte) 7);

        // When & Then
        assertThat(mainTransaction.getPointType()).isEqualTo("MAIN");
        assertThat(subTransaction.getPointType()).isEqualTo("SUB");
    }

    @Test
    void setSubToMainRatio_WithValidValue_ShouldSetRatio() {
        // Given
        Byte newRatio = 5;

        // When
        PointTransaction.setSubToMainRatio(newRatio);

        // Then
        assertThat(PointTransaction.getSubToMainRatio()).isEqualTo(newRatio);
    }

    @Test
    void setSubToMainRatio_WithInvalidValue_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> PointTransaction.setSubToMainRatio((byte) 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sub-to-Main ratio must be between 1 and 100");

        assertThatThrownBy(() -> PointTransaction.setSubToMainRatio((byte) 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sub-to-Main ratio must be between 1 and 100");
    }

    @Test
    void setMainToTokenRatio_WithValidValue_ShouldSetRatio() {
        // Given
        Byte newRatio = 20;

        // When
        PointTransaction.setMainToTokenRatio(newRatio);

        // Then
        assertThat(PointTransaction.getMainToTokenRatio()).isEqualTo(newRatio);
    }

    @Test
    void setMainToTokenRatio_WithInvalidValue_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> PointTransaction.setMainToTokenRatio((byte) 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Main-to-Token ratio must be between 1 and 100");

        assertThatThrownBy(() -> PointTransaction.setMainToTokenRatio((byte) 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Main-to-Token ratio must be between 1 and 100");
    }

    @Test
    void initialStatusCheck_ShouldBePending() {
        // Given
        PointTransaction transaction = PointTransaction.createMainEarn(testUserGoogleId, 100, (byte) 1);

        // When & Then
        assertThat(transaction.isPending()).isTrue();
        assertThat(transaction.isConfirmed()).isFalse();
        assertThat(transaction.isFailed()).isFalse();
    }
}