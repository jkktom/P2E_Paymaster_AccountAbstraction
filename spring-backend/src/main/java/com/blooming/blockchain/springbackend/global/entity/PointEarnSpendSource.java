package com.blooming.blockchain.springbackend.global.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "point_earn_spend_sources")
@NoArgsConstructor
public class PointEarnSpendSource {
    @Id
    private Byte id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // "TASK_COMPLETION", "EVENT_REWARD", "ADMIN_GRANT"

    @Column
    private String type; //EARNING, SPENDING

    // Static reference data for database initialization
    public static final Object[][] REFERENCE_DATA = {
        // {id, name, type, pointType}
        // MAIN point transactions (1-6):
        {1, "MAIN_TASK_COMPLETION", "EARNING", "MAIN"},
        {2, "MAIN_EVENT_REWARD", "EARNING", "MAIN"},
        {3, "MAIN_ADMIN_GRANT", "EARNING", "MAIN"},
        {4, "MAIN_OTHERS_EARN", "EARNING", "MAIN"},
        {5, "MAIN_EXCHANGE", "SPENDING", "MAIN"}, // main points → governance tokens
        {6, "MAIN_SPEND_OTHERS", "SPENDING", "MAIN"},
        
        // SUB point transactions (7-12):
        {7, "SUB_TASK_COMPLETION", "EARNING", "SUB"},
        {8, "SUB_EVENT_REWARD", "EARNING", "SUB"},
        {9, "SUB_ADMIN_GRANT", "EARNING", "SUB"},
        {10, "SUB_OTHERS_EARN", "EARNING", "SUB"},
        {11, "SUB_CONVERSION", "SPENDING", "SUB"}, // sub points → main points
        {12, "SUB_SPEND_OTHERS", "SPENDING", "SUB"}
    };

    // Constructor for creating source records
    public PointEarnSpendSource(Byte id, String name, String type, String pointType) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.pointType = pointType;
    }

    // Helper methods to determine point type from sourceId
    public static String getPointTypeFromSourceId(Byte sourceId) {
        return sourceId <= 6 ? "MAIN" : "SUB";
    }

    public static boolean isEarningSource(Byte sourceId) {
        // Earning: 1-4 (MAIN), 7-10 (SUB)
        return (sourceId >= 1 && sourceId <= 4) || (sourceId >= 7 && sourceId <= 10);
    }

    public static boolean isSpendingSource(Byte sourceId) {
        // Spending: 5-6 (MAIN), 11-12 (SUB) 
        return (sourceId >= 5 && sourceId <= 6) || (sourceId >= 11 && sourceId <= 12);
    }
}