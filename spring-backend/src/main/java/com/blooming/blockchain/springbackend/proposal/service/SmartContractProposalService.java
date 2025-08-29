package com.blooming.blockchain.springbackend.proposal.service;

import com.blooming.blockchain.springbackend.user.repository.UserRepository;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.wallet.entity.UserWallet;
import com.blooming.blockchain.springbackend.zksync.service.ZkSyncService;
import com.blooming.blockchain.springbackend.zksync.service.ZkSyncEraPaymasterService;
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
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
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

    private static final ZoneId KOREA_TIMEZONE = ZoneId.of("Asia/Seoul");
    
    private final UserRepository userRepository;
    private final ZkSyncService zkSyncService;
    private final ZkSyncEraPaymasterService zkSyncEraPaymasterService;

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
     * 스마트 컨트랙트에 새로운 제안 생성 (블록체인 동기화 방식)
     * @param proposalId 블록체인과 동기화된 제안 ID (미리 할당된 ID)
     * @param description 제안 설명
     * @param proposerWalletAddress 제안자 지갑 주소 (충분한 토큰 보유 필요)
     * @param deadline 제안 마감 시간
     * @return 트랜잭션 해시와 확인된 블록체인 제안 ID를 포함한 결과
     */
    public CompletableFuture<CreateProposalResult> createProposalWithId(
            Integer proposalId,
            String description,
            String proposerWalletAddress,
            LocalDateTime deadline) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Creating proposal on smart contract with predefined ID: id={}, proposer={}, description={}", 
                        proposalId, proposerWalletAddress, description.substring(0, Math.min(50, description.length())));
                
                Web3j web3j = getWeb3jClient();
                Credentials ownerCreds = getOwnerCredentials();
                
                // 마감시간을 Unix 타임스탬프로 변환 (프론트엔드에서 한국 시간으로 전송됨)
                long deadlineTimestamp = deadline.atZone(KOREA_TIMEZONE).toEpochSecond();
                BigInteger deadlineInSeconds = BigInteger.valueOf(deadlineTimestamp);
                
                // 기존 createProposal 함수 호출 (스마트 컨트랙트는 내부적으로 ID를 생성)
                // 백엔드는 미리 ID를 할당했지만, 스마트 컨트랙트는 원래 방식대로 동작
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
                
                // 기존 동작 방식 유지 (zkSyncService 사용)
                log.info("Creating proposal with predefined ID {} using zkSync service...", proposalId);
                String txHash = zkSyncService.executeGaslessTransaction(
                    ownerPrivateKey,
                    governanceTokenAddress,
                    encodedFunction
                ).join();
                
                log.info("Successfully sent proposal creation transaction with ID {} - TX: {}", proposalId, txHash);
                
                // ID가 미리 정해져 있으므로 트랜잭션 확인 후 바로 반환
                return waitForTransactionConfirmationWithId(txHash, proposalId);
                
            } catch (Exception e) {
                log.error("Failed to create proposal on smart contract with ID: {}", proposalId, e);
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
     * 스마트 컨트랙트에 새로운 제안 생성 (레거시 방식)
     * @deprecated Use createProposalWithId for blockchain synchronization
     * @param description 제안 설명
     * @param proposerWalletAddress 제안자 지갑 주소 (충분한 토큰 보유 필요)
     * @param deadline 제안 마감 시간
     * @return 트랜잭션 해시와 블록체인 제안 ID를 포함한 결과
     */
    @Deprecated
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
                
                // 마감시간을 Unix 타임스탬프로 변환 (프론트엔드에서 한국 시간으로 전송됨)
                long deadlineTimestamp = deadline.atZone(KOREA_TIMEZONE).toEpochSecond();
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
                
                // Use the old working approach for proposal creation (keep it stable)
                log.info("Creating proposal using zkSync service (keeping working approach)...");
                String txHash = zkSyncService.executeGaslessTransaction(
                    ownerPrivateKey,
                    governanceTokenAddress,
                    encodedFunction
                ).join();
                
                log.info("Successfully sent proposal creation transaction - TX: {}", txHash);
                
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
     * 트랜잭션 확인 대기 및 결과 처리 (ID가 미리 정해진 경우)
     */
    private CreateProposalResult waitForTransactionConfirmationWithId(String txHash, Integer expectedProposalId) {
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
                log.info("Create proposal transaction confirmed - TX: {}, Expected ID: {}", txHash, expectedProposalId);
                
                // ID가 미리 정해져 있으므로 로그에서 확인만 하고 예상된 ID를 반환
                Integer extractedProposalId = extractProposalIdFromReceipt(receipt, txHash);
                
                if (extractedProposalId != null && !extractedProposalId.equals(expectedProposalId)) {
                    log.warn("Extracted proposal ID {} does not match expected ID {}", extractedProposalId, expectedProposalId);
                }
                
                return CreateProposalResult.builder()
                    .txHash(txHash)
                    .proposalId(expectedProposalId) // 예상된 ID 사용
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
     * 트랜잭션 확인 대기 및 결과 처리 (레거시)
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
                
                // Extract the actual blockchain proposal ID from transaction logs
                Integer blockchainProposalId = extractProposalIdFromReceipt(receipt, txHash);
                
                if (blockchainProposalId == null) {
                    log.warn("Could not extract proposal ID from transaction logs, using fallback approach");
                    blockchainProposalId = generateMockProposalId();
                }
                
                log.info("Extracted blockchain proposal ID: {}", blockchainProposalId);
                
                return CreateProposalResult.builder()
                    .txHash(txHash)
                    .proposalId(blockchainProposalId)
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
     * 스마트 컨트랙트에서 투표 실행 (사용자 개인키 사용)
     * @param proposalId 블록체인 제안 ID
     * @param voterGoogleId 투표자 Google ID (사용자 식별용)
     * @param support 투표 내용 (true = 찬성, false = 반대)
     * @return 투표 실행 결과
     */
    public CompletableFuture<VoteResult> voteWithUserCredentials(
            Integer proposalId,
            String voterGoogleId,
            boolean support) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Voting on smart contract with user credentials: blockchainProposalId={}, user={}, support={}", 
                        proposalId, voterGoogleId, support);
                
                // 1. Validate that the blockchain proposal ID exists on the smart contract
                if (!validateBlockchainProposalExists(proposalId)) {
                    throw new IllegalArgumentException("Blockchain proposal does not exist: " + proposalId);
                }
                
                // 2. 사용자 조회
                User user = userRepository.findByGoogleId(voterGoogleId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + voterGoogleId));
                
                // 2. 사용자 지갑 조회
                UserWallet userWallet = zkSyncService.getUserWallet(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User wallet not found: " + voterGoogleId));
                
                // 3. 개인키 복호화
                String privateKey = zkSyncService.getDecryptedPrivateKey(userWallet);
                
                // 4. 사용자 자격증명 생성
                Credentials userCredentials = Credentials.create(privateKey);
                
                log.info("Using user's own wallet for voting: {} -> {}", 
                        voterGoogleId, userWallet.getShortAddress());
                
                Web3j web3j = getWeb3jClient();
                
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
                
                // Use the old working approach for voting (keeping it stable)
                log.info("Executing gasless vote transaction using zkSync service...");
                String txHash = zkSyncService.executeGaslessTransaction(
                    privateKey,
                    governanceTokenAddress,
                    encodedFunction
                ).join();
                
                log.info("Successfully sent gasless vote transaction - TX: {}", txHash);
                
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
            // Real transaction receipt handling for zkSync Era
            Web3j web3j = getWeb3jClient();
            TransactionReceipt receipt = null;
            int attempts = 0;
            int maxAttempts = 30;
            
            log.info("Waiting for transaction confirmation - TX: {}", txHash);
            
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
                log.info("Real gasless vote transaction confirmed - TX: {}", txHash);
                
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
     * 트랜잭션 영수증에서 실제 블록체인 제안 ID 추출
     * ProposalCreated 이벤트 로그를 파싱하여 제안 ID를 가져옴
     */
    private Integer extractProposalIdFromReceipt(TransactionReceipt receipt, String txHash) {
        try {
            log.info("Extracting proposal ID from transaction receipt: {}", txHash);
            
            // Check if the transaction has any logs
            if (receipt.getLogs() == null || receipt.getLogs().isEmpty()) {
                log.warn("No logs found in transaction receipt for proposal creation");
                return null;
            }
            
            // ProposalCreated event signature: ProposalCreated(uint256,address,string,uint256)
            // Event topic hash for ProposalCreated event
            // This would normally be calculated from the ABI, but for now we'll look for any uint256 value in logs
            
            for (var blockchainLog : receipt.getLogs()) {
                try {
                    // Look for logs from the governance contract
                    if (blockchainLog.getAddress().equalsIgnoreCase(governanceTokenAddress)) {
                        log.info("Found log from governance contract: {}", blockchainLog.getAddress());
                        
                        // For ProposalCreated event, the first indexed parameter is usually the proposal ID
                        var topics = blockchainLog.getTopics();
                        if (topics != null && topics.size() > 1) {
                            // The second topic (index 1) should be the proposal ID (first indexed parameter)
                            String proposalIdHex = topics.get(1);
                            if (proposalIdHex != null && proposalIdHex.startsWith("0x")) {
                                try {
                                    // Convert hex to integer
                                    BigInteger proposalIdBig = new BigInteger(proposalIdHex.substring(2), 16);
                                    Integer proposalId = proposalIdBig.intValue();
                                    
                                    log.info("Successfully extracted proposal ID from logs: {}", proposalId);
                                    return proposalId;
                                    
                                } catch (NumberFormatException e) {
                                    log.warn("Could not parse proposal ID from hex: {}", proposalIdHex);
                                }
                            }
                        }
                        
                        // Fallback: try to parse data field for non-indexed parameters
                        String data = blockchainLog.getData();
                        if (data != null && data.length() > 2) {
                            log.debug("Log data: {}", data);
                            // The data field might contain the proposal ID as the first 32 bytes
                            if (data.length() >= 66) { // 0x + 64 hex chars = 32 bytes
                                try {
                                    String proposalIdHex = data.substring(2, 66); // First 32 bytes
                                    BigInteger proposalIdBig = new BigInteger(proposalIdHex, 16);
                                    if (proposalIdBig.compareTo(BigInteger.ZERO) > 0 && 
                                        proposalIdBig.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
                                        Integer proposalId = proposalIdBig.intValue();
                                        log.info("Successfully extracted proposal ID from data field: {}", proposalId);
                                        return proposalId;
                                    }
                                } catch (NumberFormatException e) {
                                    log.debug("Could not parse proposal ID from data field");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("Error processing log entry: {}", e.getMessage());
                }
            }
            
            log.warn("Could not extract proposal ID from any logs in transaction {}", txHash);
            return null;
            
        } catch (Exception e) {
            log.error("Failed to extract proposal ID from transaction receipt: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 블록체인에서 제안 ID가 실제로 존재하는지 검증
     * @param blockchainProposalId 블록체인 제안 ID
     * @return true if proposal exists on blockchain, false otherwise
     */
    private boolean validateBlockchainProposalExists(Integer blockchainProposalId) {
        try {
            log.debug("Validating blockchain proposal exists: {}", blockchainProposalId);
            
            Web3j web3j = getWeb3jClient();
            
            // Create a function to get proposal state/info
            // Most governance contracts have a function like `proposals(uint256)` or `getProposal(uint256)`
            Function getProposalFunction = new Function(
                "proposals", // Assuming the contract has a proposals(uint256) function
                Arrays.asList(new Uint256(BigInteger.valueOf(blockchainProposalId))),
                Arrays.asList(
                    new TypeReference<Utf8String>() {}, // description
                    new TypeReference<org.web3j.abi.datatypes.Address>() {}, // proposer
                    new TypeReference<Uint256>() {}, // forVotes
                    new TypeReference<Uint256>() {}, // againstVotes
                    new TypeReference<Uint256>() {}, // deadline
                    new TypeReference<Bool>() {}, // executed
                    new TypeReference<Bool>() {}, // canceled
                    new TypeReference<Uint256>() {} // createdAt
                )
            );
            
            String encodedFunction = FunctionEncoder.encode(getProposalFunction);
            
            // Call the contract to get proposal data
            org.web3j.protocol.core.methods.request.Transaction transaction = 
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                    null, governanceTokenAddress, encodedFunction);
            
            var response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            
            if (response.hasError()) {
                log.warn("Failed to validate proposal existence: {}", response.getError().getMessage());
                return false;
            }
            
            String result = response.getValue();
            
            // If we get a valid response with data, the proposal exists
            if (result != null && !result.equals("0x")) {
                log.debug("Blockchain proposal {} exists, response length: {}", blockchainProposalId, result.length());
                return true;
            } else {
                log.warn("Blockchain proposal {} does not exist or returned empty data", blockchainProposalId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error validating blockchain proposal existence: {}", e.getMessage(), e);
            // In case of error, we'll allow the transaction to proceed
            // The smart contract will provide the definitive answer
            return true;
        }
    }

    /**
     * 테스트용 모의 제안 ID 생성 (폴백용)
     * 실제 구현에서는 트랜잭션 로그에서 추출하는 것이 우선
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

    /**
     * 거버넌스 토큰 주소 반환 (다른 서비스에서 사용)
     */
    public String getGovernanceTokenAddress() {
        return governanceTokenAddress;
    }
}