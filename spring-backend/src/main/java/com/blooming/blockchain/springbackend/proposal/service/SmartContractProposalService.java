package com.blooming.blockchain.springbackend.proposal.service;

import com.blooming.blockchain.springbackend.zksync.dto.CreateProposalResult;
import com.blooming.blockchain.springbackend.zksync.dto.VoteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Bool;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * 스마트 컨트랙트 제안 관련 작업을 담당하는 서비스
 * 제안 생성, 투표 등 블록체인 상호작용을 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmartContractProposalService {

    @Value("${app.zksync.rpc-url:https://sepolia.era.zksync.dev}")
    private String zkSyncRpcUrl;

    @Value("${app.zksync.governance.token.address:0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e}")
    private String governanceTokenAddress;

    @Value("${app.zksync.chain-id:300}")
    private Integer chainId;

    @Value("${app.zksync.owner.private-key:your-owner-private-key-here}")
    private String ownerPrivateKey;

    private Web3j web3j;
    private Credentials ownerCredentials;

    private Web3j getWeb3jClient() {
        if (web3j == null) {
            web3j = Web3j.build(new HttpService(zkSyncRpcUrl));
        }
        return web3j;
    }

    private Credentials getOwnerCredentials() {
        if (ownerCredentials == null) {
            if (ownerPrivateKey == null || ownerPrivateKey.equals("your-owner-private-key-here")) {
                throw new RuntimeException("Owner private key not configured. Set ZKSYNC_OWNER_PRIVATE_KEY environment variable.");
            }
            try {
                String cleanPrivateKey = ownerPrivateKey.startsWith("0x") ? 
                    ownerPrivateKey.substring(2) : ownerPrivateKey;
                ownerCredentials = Credentials.create(cleanPrivateKey);
                log.info("Owner credentials initialized for address: {}", ownerCredentials.getAddress());
            } catch (Exception e) {
                throw new RuntimeException("Failed to create owner credentials from private key", e);
            }
        }
        return ownerCredentials;
    }

    /**
     * 스마트 컨트랙트에 새로운 제안 생성
     * @param description 제안 설명
     * @param proposerWalletAddress 제안자 지갑 주소 (충분한 토큰 보유 필요)
     * @param deadline 제안 마감 시간
     * @return 트랜잭션 해시와 블록체인 제안 ID를 포함한 결과
     */
    public CompletableFuture<CreateProposalResult> createProposal(
            String description,
            String proposerWalletAddress,
            LocalDateTime deadline) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Creating proposal on smart contract: proposer={}, description={}", 
                        proposerWalletAddress, description.substring(0, Math.min(50, description.length())));
                
                Web3j web3j = getWeb3jClient();
                Credentials ownerCreds = getOwnerCredentials();
                
                // 마감시간을 Unix 타임스탬프로 변환
                long deadlineTimestamp = deadline.toEpochSecond(ZoneOffset.UTC);
                BigInteger deadlineInSeconds = BigInteger.valueOf(deadlineTimestamp);
                
                // createProposal 함수 호출 생성
                Function createProposalFunction = new Function(
                    "createProposal",
                    Arrays.asList(
                        new Utf8String(description),
                        new Uint256(deadlineInSeconds)
                    ),
                    Arrays.asList(new TypeReference<Uint256>() {}) // 제안 ID 반환
                );
                
                // 함수 호출 인코딩
                String encodedFunction = FunctionEncoder.encode(createProposalFunction);
                
                // 트랜잭션 매니저 생성
                RawTransactionManager transactionManager = new RawTransactionManager(
                    web3j, ownerCreds, chainId.longValue());
                
                // 트랜잭션 전송
                org.web3j.protocol.core.methods.response.EthSendTransaction ethSendTransaction = 
                    transactionManager.sendTransaction(
                        DefaultGasProvider.GAS_PRICE,
                        DefaultGasProvider.GAS_LIMIT,
                        governanceTokenAddress,
                        encodedFunction,
                        BigInteger.ZERO
                    );
                
                if (ethSendTransaction.hasError()) {
                    throw new RuntimeException("Create proposal transaction failed: " + ethSendTransaction.getError().getMessage());
                }
                
                String txHash = ethSendTransaction.getTransactionHash();
                log.info("Successfully sent create proposal transaction - TX: {}", txHash);
                
                // 트랜잭션 영수증을 기다려서 제안 ID 획득
                return waitForTransactionConfirmation(txHash);
                
            } catch (Exception e) {
                log.error("Failed to create proposal on smart contract", e);
                return CreateProposalResult.builder()
                    .txHash(null)
                    .proposalId(null)
                    .success(false)
                    .errorMessage("Failed to create proposal: " + e.getMessage())
                    .build();
            }
        });
    }

    /**
     * 트랜잭션 확인 대기 및 결과 처리
     */
    private CreateProposalResult waitForTransactionConfirmation(String txHash) {
        try {
            Web3j web3j = getWeb3jClient();
            TransactionReceipt receipt = null;
            int attempts = 0;
            int maxAttempts = 30;
            
            while (receipt == null && attempts < maxAttempts) {
                Thread.sleep(1000);
                var receiptResponse = web3j.ethGetTransactionReceipt(txHash).send();
                if (receiptResponse.getTransactionReceipt().isPresent()) {
                    receipt = receiptResponse.getTransactionReceipt().get();
                    break;
                }
                attempts++;
            }
            
            if (receipt != null && receipt.isStatusOK()) {
                log.info("Create proposal transaction confirmed - TX: {}", txHash);
                
                // 실제 구현에서는 트랜잭션 로그를 파싱하여 실제 제안 ID를 추출
                // 현재는 테스트용 모의 제안 ID 생성
                Integer mockProposalId = generateMockProposalId();
                
                return CreateProposalResult.builder()
                    .txHash(txHash)
                    .proposalId(mockProposalId)
                    .success(true)
                    .build();
            } else {
                log.warn("Create proposal transaction may have failed - TX: {}", txHash);
                return CreateProposalResult.builder()
                    .txHash(txHash)
                    .proposalId(null)
                    .success(false)
                    .errorMessage("Transaction receipt indicates failure")
                    .build();
            }
        } catch (Exception e) {
            log.warn("Could not get transaction receipt for create proposal - TX: {} - Error: {}", txHash, e.getMessage());
            
            return CreateProposalResult.builder()
                .txHash(txHash)
                .proposalId(null)
                .success(false)
                .errorMessage("Could not confirm transaction: " + e.getMessage())
                .build();
        }
    }

    /**
     * 스마트 컨트랙트에서 투표 실행
     * @param proposalId 블록체인 제안 ID
     * @param voterWalletAddress 투표자 지갑 주소
     * @param support 투표 내용 (true = 찬성, false = 반대)
     * @return 투표 실행 결과
     */
    public CompletableFuture<VoteResult> vote(
            Integer proposalId,
            String voterWalletAddress,
            boolean support) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Voting on smart contract: proposalId={}, voter={}, support={}", 
                        proposalId, voterWalletAddress, support);
                
                Web3j web3j = getWeb3jClient();
                Credentials ownerCreds = getOwnerCredentials();
                
                // vote 함수 호출 생성
                Function voteFunction = new Function(
                    "vote",
                    Arrays.asList(
                        new Uint256(BigInteger.valueOf(proposalId)),
                        new Bool(support)
                    ),
                    Collections.emptyList() // 반환값 없음
                );
                
                // 함수 호출 인코딩
                String encodedFunction = FunctionEncoder.encode(voteFunction);
                
                // 트랜잭션 매니저 생성
                RawTransactionManager transactionManager = new RawTransactionManager(
                    web3j, ownerCreds, chainId.longValue());
                
                // 트랜잭션 전송
                org.web3j.protocol.core.methods.response.EthSendTransaction ethSendTransaction = 
                    transactionManager.sendTransaction(
                        DefaultGasProvider.GAS_PRICE,
                        DefaultGasProvider.GAS_LIMIT,
                        governanceTokenAddress,
                        encodedFunction,
                        BigInteger.ZERO
                    );
                
                if (ethSendTransaction.hasError()) {
                    throw new RuntimeException("Vote transaction failed: " + ethSendTransaction.getError().getMessage());
                }
                
                String txHash = ethSendTransaction.getTransactionHash();
                log.info("Successfully sent vote transaction - TX: {}", txHash);
                
                // 트랜잭션 확인 대기
                return waitForVoteConfirmation(txHash, proposalId, support);
                
            } catch (Exception e) {
                log.error("Failed to vote on smart contract", e);
                return VoteResult.builder()
                    .txHash(null)
                    .proposalId(proposalId)
                    .support(support)
                    .votingPower(BigInteger.ZERO)
                    .success(false)
                    .errorMessage("Failed to vote: " + e.getMessage())
                    .build();
            }
        });
    }

    /**
     * 투표 트랜잭션 확인 대기 및 결과 처리
     */
    private VoteResult waitForVoteConfirmation(String txHash, Integer proposalId, boolean support) {
        try {
            Web3j web3j = getWeb3jClient();
            TransactionReceipt receipt = null;
            int attempts = 0;
            int maxAttempts = 30;
            
            while (receipt == null && attempts < maxAttempts) {
                Thread.sleep(1000);
                var receiptResponse = web3j.ethGetTransactionReceipt(txHash).send();
                if (receiptResponse.getTransactionReceipt().isPresent()) {
                    receipt = receiptResponse.getTransactionReceipt().get();
                    break;
                }
                attempts++;
            }
            
            if (receipt != null && receipt.isStatusOK()) {
                log.info("Vote transaction confirmed - TX: {}", txHash);
                
                // 실제 구현에서는 트랜잭션 로그를 파싱하여 실제 투표 권한을 추출
                // 현재는 테스트용 모의 투표 권한 생성
                BigInteger mockVotingPower = generateMockVotingPower();
                
                return VoteResult.builder()
                    .txHash(txHash)
                    .proposalId(proposalId)
                    .support(support)
                    .votingPower(mockVotingPower)
                    .success(true)
                    .build();
            } else {
                log.warn("Vote transaction may have failed - TX: {}", txHash);
                return VoteResult.builder()
                    .txHash(txHash)
                    .proposalId(proposalId)
                    .support(support)
                    .votingPower(BigInteger.ZERO)
                    .success(false)
                    .errorMessage("Transaction receipt indicates failure")
                    .build();
            }
        } catch (Exception e) {
            log.warn("Could not get transaction receipt for vote - TX: {} - Error: {}", txHash, e.getMessage());
            
            return VoteResult.builder()
                .txHash(txHash)
                .proposalId(proposalId)
                .support(support)
                .votingPower(BigInteger.ZERO)
                .success(false)
                .errorMessage("Could not confirm transaction: " + e.getMessage())
                .build();
        }
    }

    /**
     * 테스트용 모의 제안 ID 생성
     * 실제 구현에서는 트랜잭션 로그에서 추출
     */
    private Integer generateMockProposalId() {
        SecureRandom random = new SecureRandom();
        return random.nextInt(1000) + 1;
    }

    /**
     * 테스트용 모의 투표 권한 생성
     * 실제 구현에서는 트랜잭션 로그에서 추출
     */
    private BigInteger generateMockVotingPower() {
        SecureRandom random = new SecureRandom();
        // 1-10 토큰 범위의 임의 투표 권한 (Wei 단위)
        long tokens = random.nextInt(10) + 1;
        return BigInteger.valueOf(tokens).multiply(BigInteger.TEN.pow(18));
    }
}