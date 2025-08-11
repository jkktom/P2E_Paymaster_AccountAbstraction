package com.blooming.blockchain.springbackend.pointtransaction.entity;

import com.blooming.blockchain.springbackend.global.entity.PointEarnSpendSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions", indexes = {
    @Index(name = "idx_point_tx_user_date", columnList = "userGoogleId, createdAt"),
    @Index(name = "idx_point_tx_type", columnList = "pointTypeId"),
    @Index(name = "idx_point_tx_source", columnList = "sourceId")
})
@Getter
@Setter
@NoArgsConstructor
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_google_id", nullable = false)
    private String userGoogleId;

    @Column(name = "point_type_id", nullable = false)
    private Byte pointTypeId; // 1 = MAIN, 2 = SUB

    @Column(name = "main_earned")
    @Min(0)
    private Integer mainEarnAmount;

    @Column(name = "main_exchanged")
    @Min(0)
    private Integer mainExchangedAmount;

    @Column(name = "sub_earned")
    @Min(0)
    private Integer subEarnAmount;

    @Column(name = "sub_converted")
    @Min(0)
    private Integer subConvertedAmount;

    @Column(name = "source_id", nullable = false)
    private Byte sourceId; 
    // MAIN: 1=MAIN_TASK_COMPLETION, 2=MAIN_EVENT_REWARD, 3=MAIN_ADMIN_GRANT, 4=MAIN_OTHERS_EARN, 5=MAIN_EXCHANGE, 6=MAIN_SPEND_OTHERS
    // SUB: 7=SUB_TASK_COMPLETION, 8=SUB_EVENT_REWARD, 9=SUB_ADMIN_GRANT, 10=SUB_OTHERS_EARN, 11=SUB_CONVERSION, 12=SUB_SPEND_OTHERS 

    @Column(name = "transaction_status_id", nullable = false)
    private Byte transactionStatusId = 1; // 1 = PENDING, 2 = CONFIRMED, 3 = FAILED

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Configurable ratios (1-100 range)
    private static Byte SUB_TO_MAIN_RATIO = 10;
    private static Byte MAIN_TO_TOKEN_RATIO = 10;

    // Private constructor for internal use
    private PointTransaction(String userGoogleId, Byte pointTypeId, Byte sourceId, String description) {
        this.userGoogleId = userGoogleId;
        this.pointTypeId = pointTypeId;
        this.sourceId = sourceId;
        this.description = description;
        this.transactionStatusId = 1; // PENDING
    }

    // Static factory method for main point earning (with description)
    public static PointTransaction createMainEarn(String userGoogleId, Integer amount, Byte sourceId, String description) {
        PointTransaction transaction = new PointTransaction(userGoogleId, (byte) 1, sourceId, description);
        transaction.mainEarnAmount = amount;
        return transaction;
    }

    // Static factory method for main point earning (without description)
    public static PointTransaction createMainEarn(String userGoogleId, Integer amount, Byte sourceId) {
        return createMainEarn(userGoogleId, amount, sourceId, null);
    }

    // Static factory method for sub point earning (with description)
    public static PointTransaction createSubEarn(String userGoogleId, Integer amount, Byte sourceId, String description) {
        PointTransaction transaction = new PointTransaction(userGoogleId, (byte) 2, sourceId, description);
        transaction.subEarnAmount = amount;
        return transaction;
    }

    // Static factory method for sub point earning (without description)
    public static PointTransaction createSubEarn(String userGoogleId, Integer amount, Byte sourceId) {
        return createSubEarn(userGoogleId, amount, sourceId, null);
    }

    // Static factory method for sub to main point conversion (with description)
    public static PointTransaction createSubToMainConversion(String userGoogleId, Integer subPointsSpent, String description) {
        PointTransaction transaction = new PointTransaction(userGoogleId, (byte) 2, (byte) 11, description); // sourceId 11 = SUB_CONVERSION
        transaction.subConvertedAmount = subPointsSpent;
        transaction.mainEarnAmount = subPointsSpent / SUB_TO_MAIN_RATIO;
        return transaction;
    }

    // Static factory method for sub to main point conversion (without description)
    public static PointTransaction createSubToMainConversion(String userGoogleId, Integer subPointsSpent) {
        return createSubToMainConversion(userGoogleId, subPointsSpent, null);
    }

    // Static factory method for main to token exchange (with description)
    public static PointTransaction createMainToTokenExchange(String userGoogleId, Integer mainPointsSpent, String description) {
        PointTransaction transaction = new PointTransaction(userGoogleId, (byte) 1, (byte) 5, description); // sourceId 5 = MAIN_EXCHANGE
        transaction.mainExchangedAmount = mainPointsSpent;
        return transaction;
    }

    // Static factory method for main to token exchange (without description)
    public static PointTransaction createMainToTokenExchange(String userGoogleId, Integer mainPointsSpent) {
        return createMainToTokenExchange(userGoogleId, mainPointsSpent, null);
    }

    // Helper method to confirm transaction
    public void confirmTransaction() {
        this.transactionStatusId = 2; // CONFIRMED
    }

    // Helper method to fail transaction
    public void failTransaction() {
        this.transactionStatusId = 3; // FAILED
    }

    // Status check methods
    public boolean isConfirmed() {
        return this.transactionStatusId == 2;
    }

    public boolean isPending() {
        return this.transactionStatusId == 1;
    }

    public boolean isFailed() {
        return this.transactionStatusId == 3;
    }

    // Get total transaction amount based on transaction type
    public Integer getTransactionAmount() {
        if (PointEarnSpendSource.isEarningSource(sourceId)) {
            return mainEarnAmount != null ? mainEarnAmount : subEarnAmount;
        } else {
            return mainExchangedAmount != null ? mainExchangedAmount : subConvertedAmount;
        }
    }

    // Get transaction type based on sourceId
    public String getTransactionType() {
        return PointEarnSpendSource.isEarningSource(sourceId) ? "EARNING" : "SPENDING";
    }

    // Get point type string
    public String getPointType() {
        return PointEarnSpendSource.getPointTypeFromSourceId(sourceId);
    }

    // Ratio configuration methods
    public static void setSubToMainRatio(Byte ratio) {
        if (ratio < 1 || ratio > 100) {
            throw new IllegalArgumentException("Sub-to-Main ratio must be between 1 and 100");
        }
        SUB_TO_MAIN_RATIO = ratio;
    }

    public static void setMainToTokenRatio(Byte ratio) {
        if (ratio < 1 || ratio > 100) {
            throw new IllegalArgumentException("Main-to-Token ratio must be between 1 and 100");
        }
        MAIN_TO_TOKEN_RATIO = ratio;
    }

    public static Byte getSubToMainRatio() {
        return SUB_TO_MAIN_RATIO;
    }

    public static Byte getMainToTokenRatio() {
        return MAIN_TO_TOKEN_RATIO;
    }
}
