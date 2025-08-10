package com.blooming.blockchain.springbackend.pointtransaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "main_point_transactions")
@Getter
@Setter
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_google_id", nullable = false, unique = true)
    private String userGoogleId;

    //point type // 1 = MAIN, 2 = SUB

    //transaction amount (how much points being earned/spent)
    @Column(nullable = false)
    @Min(0)
    private int transaction_amount;

    //transaction status 1 = PENDING, 2 = CONFIRMED, 3 = FAILED

    //(true/false) applied to userpointtoken entity, token_transaction entity



}
