package com.blooming.blockchain.springbackend.global.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "point_earn_spend_sources")
@NoArgsConstructor
public class PointEarnSpend {
    @Id
    private Byte id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // "TASK_COMPLETION", "EVENT_REWARD", "ADMIN_GRANT"

    @Column
    private String type; //EARNING, SPENDING

    // in total
    // 1 : TASK_COMPLETION : EARNING ,
    // 2 : EVENT_REWARD : EARNING,
    // 3 : ADMIN_GRANT : EARNING,
    // 4 : OTHERS_E : EARNING,
    // 5 : CONVERSION : SPENDING, sub to main conversion
    // 6 : EXCHANGE : SPENDING, main to token conversion
    // 7 : OTHERS_S : SPENDING
}