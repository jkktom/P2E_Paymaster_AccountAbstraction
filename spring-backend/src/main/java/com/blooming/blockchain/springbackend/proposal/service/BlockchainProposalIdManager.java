package com.blooming.blockchain.springbackend.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Blockchain Proposal ID Manager
 * 
 * Object-oriented component that maintains synchronization between backend and blockchain
 * proposal IDs. On startup, queries the blockchain for the current highest proposal ID
 * and provides thread-safe methods to generate the next sequential ID.
 * 
 * This eliminates the database auto-increment vs blockchain ID mismatch issue.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BlockchainProposalIdManager {

    @Value("${app.zksync.rpc-url:https://sepolia.era.zksync.dev}")
    private String zkSyncRpcUrl;

    @Value("${app.zksync.governance.token.address:0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e}")
    private String governanceTokenAddress;

    private Web3j web3j;
    private Integer currentHighestProposalId;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Initialize the manager by querying blockchain state on application startup
     */
    @PostConstruct
    public void initializeFromBlockchain() {
        try {
            log.info("Initializing BlockchainProposalIdManager from blockchain state...");
            
            // Initialize Web3j client
            this.web3j = Web3j.build(new HttpService(zkSyncRpcUrl));
            
            // Query blockchain for current highest proposal ID
            Integer blockchainHighestId = queryBlockchainForHighestProposalId();
            
            if (blockchainHighestId != null) {
                this.currentHighestProposalId = blockchainHighestId;
                log.info("Successfully initialized with blockchain proposal ID: {}", currentHighestProposalId);
            } else {
                // If blockchain query fails, start from 0 (first proposal will be ID 1)
                this.currentHighestProposalId = 0;
                log.warn("Could not query blockchain for proposal count, starting from ID 0");
            }
            
            log.info("BlockchainProposalIdManager ready - Next proposal ID will be: {}", 
                    currentHighestProposalId + 1);
            
        } catch (Exception e) {
            log.error("Failed to initialize BlockchainProposalIdManager from blockchain", e);
            // Fallback: start from 0
            this.currentHighestProposalId = 0;
            log.warn("Using fallback initialization - starting from proposal ID 0");
        }
    }

    /**
     * Get the next proposal ID to use for a new proposal
     * This method is thread-safe and ensures sequential ID generation
     * 
     * @return The next proposal ID that should be used for blockchain and database
     */
    public Integer getNextProposalId() {
        lock.lock();
        try {
            Integer nextId = currentHighestProposalId + 1;
            log.info("Generated next proposal ID: {}", nextId);
            return nextId;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Confirm that a proposal with the given ID has been successfully created
     * This updates our internal counter to maintain synchronization
     * 
     * @param confirmedProposalId The proposal ID that was successfully created on blockchain
     */
    public void confirmProposalCreated(Integer confirmedProposalId) {
        lock.lock();
        try {
            if (confirmedProposalId > currentHighestProposalId) {
                currentHighestProposalId = confirmedProposalId;
                log.info("Updated highest proposal ID to: {}", currentHighestProposalId);
            } else {
                log.warn("Confirmed proposal ID {} is not higher than current highest ID {}", 
                        confirmedProposalId, currentHighestProposalId);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the current highest proposal ID known to the system
     * 
     * @return Current highest proposal ID
     */
    public Integer getCurrentHighestProposalId() {
        lock.lock();
        try {
            return currentHighestProposalId;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Force refresh the proposal ID from blockchain
     * Useful for debugging or manual synchronization
     * 
     * @return true if refresh was successful, false otherwise
     */
    public boolean refreshFromBlockchain() {
        try {
            log.info("Manually refreshing proposal ID from blockchain...");
            Integer blockchainHighestId = queryBlockchainForHighestProposalId();
            
            if (blockchainHighestId != null) {
                lock.lock();
                try {
                    this.currentHighestProposalId = blockchainHighestId;
                    log.info("Successfully refreshed proposal ID to: {}", currentHighestProposalId);
                    return true;
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("Failed to refresh proposal ID from blockchain");
                return false;
            }
        } catch (Exception e) {
            log.error("Error refreshing proposal ID from blockchain", e);
            return false;
        }
    }

    /**
     * Query the blockchain smart contract for the current highest proposal ID
     * 
     * @return Highest proposal ID on blockchain, or null if query fails
     */
    private Integer queryBlockchainForHighestProposalId() {
        try {
            log.debug("Querying blockchain for current proposal count...");
            
            // Try to call a proposalCount() function if it exists
            // Most governance contracts have this function
            Function proposalCountFunction = new Function(
                "proposalCount",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {})
            );
            
            String encodedFunction = FunctionEncoder.encode(proposalCountFunction);
            
            Transaction transaction = Transaction.createEthCallTransaction(
                null, governanceTokenAddress, encodedFunction);
            
            EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            
            if (response.hasError()) {
                log.warn("Blockchain call failed: {}", response.getError().getMessage());
                return tryAlternativeMethod();
            }
            
            String result = response.getValue();
            if (result != null && !result.equals("0x")) {
                // Parse the result as a uint256
                BigInteger proposalCount = Numeric.toBigInt(result);
                Integer count = proposalCount.intValue();
                
                log.info("Blockchain reports proposal count: {}", count);
                return count;
            } else {
                log.warn("Empty response from blockchain proposal count query");
                return tryAlternativeMethod();
            }
            
        } catch (Exception e) {
            log.error("Error querying blockchain for proposal count", e);
            return tryAlternativeMethod();
        }
    }

    /**
     * Alternative method to query blockchain state
     * Try different function names that governance contracts might use
     */
    private Integer tryAlternativeMethod() {
        try {
            log.debug("Trying alternative method to get proposal count...");
            
            // Try nextProposalId() function
            Function nextProposalIdFunction = new Function(
                "nextProposalId",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {})
            );
            
            String encodedFunction = FunctionEncoder.encode(nextProposalIdFunction);
            
            Transaction transaction = Transaction.createEthCallTransaction(
                null, governanceTokenAddress, encodedFunction);
            
            EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            
            if (!response.hasError() && response.getValue() != null && !response.getValue().equals("0x")) {
                BigInteger nextId = Numeric.toBigInt(response.getValue());
                // If we get nextProposalId, the current highest is nextId - 1
                Integer currentHighest = nextId.intValue() - 1;
                log.info("Blockchain reports next proposal ID: {}, current highest: {}", 
                        nextId.intValue(), currentHighest);
                return Math.max(0, currentHighest); // Ensure we don't go below 0
            }
            
            log.warn("Alternative method also failed to get proposal count");
            return null;
            
        } catch (Exception e) {
            log.error("Alternative method failed", e);
            return null;
        }
    }

    /**
     * Get manager status for debugging and health checks
     */
    public String getStatus() {
        return String.format(
            "BlockchainProposalIdManager Status:\n" +
            "Current Highest ID: %d\n" +
            "Next ID: %d\n" +
            "Governance Contract: %s\n" +
            "RPC URL: %s",
            getCurrentHighestProposalId(),
            getCurrentHighestProposalId() + 1,
            governanceTokenAddress,
            zkSyncRpcUrl
        );
    }
}