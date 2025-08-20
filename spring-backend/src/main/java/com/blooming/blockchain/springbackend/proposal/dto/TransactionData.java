package com.blooming.blockchain.springbackend.proposal.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

/**
 * Transaction data for frontend to execute
 * Used in Web3Auth ephemeral key approach
 */
@Data
@Builder
public class TransactionData {
    
    /**
     * Target contract address
     */
    private String to;
    
    /**
     * Encoded function call data
     */
    private String data;
    
    /**
     * Estimated gas limit
     */
    private String gasLimit;
    
    /**
     * Value to send (usually "0x0" for contract calls)
     */
    @Builder.Default
    private String value = "0x0";
    
    /**
     * Transaction type identifier for frontend
     */
    private String transactionType;
    
    /**
     * Additional metadata for the transaction
     */
    private Object metadata;
}