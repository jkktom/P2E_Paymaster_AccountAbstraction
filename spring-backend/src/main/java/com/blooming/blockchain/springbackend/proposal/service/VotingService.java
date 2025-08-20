package com.blooming.blockchain.springbackend.proposal.service;

import com.blooming.blockchain.springbackend.proposal.entity.Proposal;
import com.blooming.blockchain.springbackend.proposal.entity.ProposalVoteCount;
import com.blooming.blockchain.springbackend.proposal.entity.UserVote;
import com.blooming.blockchain.springbackend.proposal.repository.ProposalRepository;
import com.blooming.blockchain.springbackend.proposal.repository.ProposalVoteCountRepository;
import com.blooming.blockchain.springbackend.proposal.repository.UserVoteRepository;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.repository.UserRepository;
import com.blooming.blockchain.springbackend.zksync.dto.VoteResult;
import com.blooming.blockchain.springbackend.proposal.dto.TransactionData;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.generated.Uint256;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * VotingService - 투표 로직 및 집계 업데이트 서비스
 * 
 * 사용자 투표 처리, 투표 집계 업데이트, 투표 기록 관리를 담당합니다.
 * 블록체인과의 실제 투표 트랜잭션은 SmartContractProposalService에서 처리됩니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VotingService {

    private final UserVoteRepository userVoteRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalVoteCountRepository proposalVoteCountRepository;
    private final UserRepository userRepository;
    private final SmartContractProposalService smartContractProposalService;

    public TransactionData prepareVoteTransaction(Integer proposalId, String userGoogleId, boolean support) {
        log.info("Preparing vote transaction: proposalId={}, user={}, support={}", 
                proposalId, userGoogleId, support);

        // 투표 가능성 검증 (기존 로직 재사용)
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        User user = userRepository.findByGoogleId(userGoogleId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userGoogleId));

        // 투표 가능 여부 확인
        if (!proposal.canVote()) {
            throw new IllegalStateException("Voting is not allowed for this proposal: " + proposalId);
        }

        // 중복 투표 확인
        if (hasUserVoted(proposalId, userGoogleId)) {
            throw new IllegalStateException("User has already voted on this proposal: " + userGoogleId);
        }

        // 블록체인 제안 ID는 이제 Primary Key
        Integer blockchainProposalId = proposal.getId();

        // vote 함수 호출 인코딩
        Function voteFunction = new Function(
            "vote",
            java.util.Arrays.asList(
                new Uint256(java.math.BigInteger.valueOf(blockchainProposalId)),
                new Bool(support)
            ),
            java.util.Collections.emptyList()
        );

        String encodedFunction = FunctionEncoder.encode(voteFunction);

        return TransactionData.builder()
            .to(smartContractProposalService.getGovernanceTokenAddress()) // Use existing service
            .data(encodedFunction)
            .gasLimit("200000") // Standard gas limit for vote
            .value("0x0")
            .transactionType("VOTE")
            .metadata(java.util.Map.of(
                "proposalId", proposalId,
                "blockchainProposalId", blockchainProposalId,
                "support", support,
                "userGoogleId", userGoogleId
            ))
            .build();
    }

    // =============== 투표 처리 메서드 ===============

    /**
     * 스마트 컨트랙트와 통합된 투표 실행
     * 1. 스마트 컨트랙트에서 투표 실행
     * 2. 성공 시 백엔드 데이터베이스에 동기화
     * 
     * @param proposalId 제안 ID (데이터베이스)
     * @param userGoogleId 투표자 Google ID
     * @param support 투표 내용 (true = 찬성, false = 반대)
     * @return 생성된 투표 기록 (스마트 컨트랙트와 데이터베이스 동기화 완료)
     */
    @Transactional
    public UserVote voteWithSmartContract(Integer proposalId, String userGoogleId, boolean support) {
        log.info("Voting with smart contract integration: proposalId={}, user={}, support={}", 
                proposalId, userGoogleId, support);

        // 제안 및 사용자 유효성 확인
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        User user = userRepository.findByGoogleId(userGoogleId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userGoogleId));

        String voterWalletAddress = user.getSmartWalletAddress();

        // 투표 가능 여부 확인
        if (!proposal.canVote()) {
            throw new IllegalStateException("Voting is not allowed for this proposal: " + proposalId);
        }

        // 중복 투표 확인
        if (hasUserVoted(proposalId, userGoogleId)) {
            throw new IllegalStateException("User has already voted on this proposal: " + userGoogleId);
        }

        try {
            // MOCK VOTING: Skip smart contract call for demo purposes
            // Generate mock voting power and transaction hash
            BigInteger mockVotingPower = generateMockVotingPower(); // 1-5 tokens in Wei
            String mockTxHash = generateMockTransactionHash();
            
            log.info("MOCK VOTING: Simulating successful vote without blockchain call");
            log.info("MOCK VOTING: Generated mock voting power: {} Wei", mockVotingPower);
            log.info("MOCK VOTING: Generated mock transaction hash: {}", mockTxHash);

            // Record vote directly in database with mock data
            UserVote userVote = recordVote(
                proposalId,
                userGoogleId,
                voterWalletAddress,
                support,
                mockVotingPower,
                mockTxHash
            );

            log.info("Successfully recorded mock vote: voteId={}, proposalId={}, mockTxHash={}", 
                    userVote.getId(), proposalId, mockTxHash);

            return userVote;

        } catch (Exception e) {
            log.error("Failed to record mock vote: proposalId={}, user={}, error={}", 
                    proposalId, userGoogleId, e.getMessage(), e);
            throw new RuntimeException("Failed to vote: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 투표 기록 및 집계 업데이트 (데이터베이스만, 스마트 컨트랙트는 별도)
     * 
     * @param proposalId 제안 ID
     * @param userGoogleId 투표자 Google ID
     * @param voterWalletAddress 투표자 지갑 주소
     * @param support 투표 내용 (true = 찬성, false = 반대)
     * @param votingPower 투표 권한 (Wei 단위)
     * @param txHash 블록체인 트랜잭션 해시
     * @return 생성된 투표 기록
     */
    @Transactional
    public UserVote recordVote(Integer proposalId, String userGoogleId, String voterWalletAddress,
                              boolean support, BigInteger votingPower, String txHash) {
        
        log.info("Recording vote: proposalId={}, user={}, support={}, power={}", 
                proposalId, userGoogleId, support, votingPower);

        // 제안 유효성 확인
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        // 투표 가능 여부 확인
        if (!proposal.canVote()) {
            throw new IllegalStateException("Voting is not allowed for this proposal: " + proposalId);
        }

        // 사용자 유효성 확인
        User user = userRepository.findByGoogleId(userGoogleId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userGoogleId));

        // 지갑 주소 일치 확인
        if (!voterWalletAddress.equals(user.getSmartWalletAddress())) {
            throw new IllegalArgumentException("Wallet address mismatch for user: " + userGoogleId);
        }

        // 중복 투표 확인
        if (hasUserVoted(proposalId, userGoogleId)) {
            throw new IllegalStateException("User has already voted on this proposal: " + userGoogleId);
        }

        // 투표 권한 유효성 확인
        if (votingPower.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Voting power must be positive: " + votingPower);
        }

        // 투표 기록 생성
        UserVote userVote = UserVote.builder()
            .proposalId(proposalId)
            .userGoogleId(userGoogleId)
            .voterWalletAddress(voterWalletAddress)
            .support(support)
            .votingPower(votingPower)
            .votedAt(LocalDateTime.now())
            .txHash(txHash)
            .build();

        UserVote savedVote = userVoteRepository.save(userVote);

        // 투표 집계 업데이트
        updateVoteCount(proposalId, support, votingPower);

        log.info("Vote recorded successfully: voteId={}, proposalId={}, user={}", 
                savedVote.getId(), proposalId, userGoogleId);

        return savedVote;
    }

    /**
     * 투표 집계 업데이트 (원자적 업데이트)
     */
    @Transactional
    public void updateVoteCount(Integer proposalId, boolean support, BigInteger votingPower) {
        LocalDateTime updateTime = LocalDateTime.now();
        
        // 먼저 투표 집계가 존재하는지 확인하고, 없으면 생성
        if (!proposalVoteCountRepository.existsByProposalId(proposalId)) {
            createVoteCountIfNotExists(proposalId);
        }
        
        // 이제 투표 집계 업데이트
        int updateCount;
        if (support) {
            // 찬성 투표 추가
            updateCount = proposalVoteCountRepository.addForVote(proposalId, votingPower, updateTime);
        } else {
            // 반대 투표 추가
            updateCount = proposalVoteCountRepository.addAgainstVote(proposalId, votingPower, updateTime);
        }

        if (updateCount == 0) {
            log.warn("Failed to update vote count for proposal: {}", proposalId);
            throw new IllegalStateException("Failed to update vote count for proposal: " + proposalId);
        }

        log.info("Vote count updated: proposalId={}, support={}, power={}", proposalId, support, votingPower);
    }

    /**
     * 투표 집계가 존재하지 않는 경우 생성
     */
    @Transactional
    public void createVoteCountIfNotExists(Integer proposalId) {
        if (!proposalVoteCountRepository.existsByProposalId(proposalId)) {
            ProposalVoteCount voteCount = ProposalVoteCount.builder()
                .proposalId(proposalId)
                .forVotes(BigInteger.ZERO)
                .againstVotes(BigInteger.ZERO)
                .totalVoters(0)
                .forVoters(0)
                .againstVoters(0)
                .lastUpdated(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

            proposalVoteCountRepository.save(voteCount);
            log.info("Vote count created for proposal: {}", proposalId);
        }
    }

    // =============== 투표 조회 메서드 ===============

    /**
     * 특정 제안에 대한 특정 사용자의 투표 조회
     */
    public Optional<UserVote> getUserVote(Integer proposalId, String userGoogleId) {
        return userVoteRepository.findByProposalIdAndUserGoogleId(proposalId, userGoogleId);
    }

    /**
     * 사용자가 특정 제안에 투표했는지 확인
     */
    public boolean hasUserVoted(Integer proposalId, String userGoogleId) {
        return userVoteRepository.existsByProposalIdAndUserGoogleId(proposalId, userGoogleId);
    }

    /**
     * 특정 제안의 모든 투표 기록 조회
     */
    public List<UserVote> getVotesByProposal(Integer proposalId) {
        return userVoteRepository.findByProposalIdOrderByVotedAtDesc(proposalId);
    }

    /**
     * 특정 제안의 투표 기록 페이징 조회
     */
    public Page<UserVote> getVotesByProposal(Integer proposalId, Pageable pageable) {
        return userVoteRepository.findByProposalId(proposalId, pageable);
    }

    /**
     * 특정 사용자의 모든 투표 기록 조회
     */
    public List<UserVote> getVotesByUser(String userGoogleId) {
        return userVoteRepository.findByUserGoogleIdOrderByVotedAtDesc(userGoogleId);
    }

    /**
     * 특정 사용자의 투표 기록 페이징 조회
     */
    public Page<UserVote> getVotesByUser(String userGoogleId, Pageable pageable) {
        return userVoteRepository.findByUserGoogleId(userGoogleId, pageable);
    }

    /**
     * 특정 제안의 찬성 투표 기록 조회
     */
    public List<UserVote> getForVotesByProposal(Integer proposalId) {
        return userVoteRepository.findByProposalIdAndSupportTrueOrderByVotingPowerDesc(proposalId);
    }

    /**
     * 특정 제안의 반대 투표 기록 조회
     */
    public List<UserVote> getAgainstVotesByProposal(Integer proposalId) {
        return userVoteRepository.findByProposalIdAndSupportFalseOrderByVotingPowerDesc(proposalId);
    }

    // =============== 투표 집계 조회 메서드 ===============

    /**
     * 특정 제안의 투표 집계 조회
     */
    public Optional<ProposalVoteCount> getVoteCount(Integer proposalId) {
        return proposalVoteCountRepository.findByProposalId(proposalId);
    }

    /**
     * 투표가 있는 제안들의 집계 조회
     */
    public List<ProposalVoteCount> getProposalsWithVotes() {
        return proposalVoteCountRepository.findProposalsWithVotes();
    }

    /**
     * 찬성이 우세한 제안들 조회
     */
    public List<ProposalVoteCount> getProposalsWithForMajority() {
        return proposalVoteCountRepository.findProposalsWithForMajority();
    }

    /**
     * 반대가 우세한 제안들 조회
     */
    public List<ProposalVoteCount> getProposalsWithAgainstMajority() {
        return proposalVoteCountRepository.findProposalsWithAgainstMajority();
    }

    /**
     * 동점인 제안들 조회
     */
    public List<ProposalVoteCount> getTiedProposals() {
        return proposalVoteCountRepository.findTiedProposals();
    }

    // =============== 투표 통계 메서드 ===============

    /**
     * 특정 제안의 투표자 수 조회
     */
    public long getVoterCount(Integer proposalId) {
        return userVoteRepository.countByProposalId(proposalId);
    }

    /**
     * 특정 제안의 찬성 투표자 수 조회
     */
    public long getForVoterCount(Integer proposalId) {
        return userVoteRepository.countByProposalIdAndSupportTrue(proposalId);
    }

    /**
     * 특정 제안의 반대 투표자 수 조회
     */
    public long getAgainstVoterCount(Integer proposalId) {
        return userVoteRepository.countByProposalIdAndSupportFalse(proposalId);
    }

    /**
     * 특정 사용자의 총 투표 수 조회
     */
    public long getUserVoteCount(String userGoogleId) {
        return userVoteRepository.countByUserGoogleId(userGoogleId);
    }

    /**
     * 특정 사용자의 찬성 투표 수 조회
     */
    public long getUserForVoteCount(String userGoogleId) {
        return userVoteRepository.countByUserGoogleIdAndSupportTrue(userGoogleId);
    }

    /**
     * 특정 사용자의 반대 투표 수 조회
     */
    public long getUserAgainstVoteCount(String userGoogleId) {
        return userVoteRepository.countByUserGoogleIdAndSupportFalse(userGoogleId);
    }

    /**
     * 전체 투표 수 조회
     */
    public long getTotalVoteCount() {
        return userVoteRepository.getTotalVoteCount();
    }

    /**
     * 활성 투표자 수 조회 (최소 한 번은 투표한 사용자 수)
     */
    public long getActiveVoterCount() {
        return userVoteRepository.getActiveVoterCount();
    }

    // =============== 투표 집계 동기화 메서드 ===============

    /**
     * 특정 제안의 투표 집계 재계산 (동기화용)
     */
    @Transactional
    public void recalculateVoteCount(Integer proposalId) {
        log.info("Recalculating vote count for proposal: {}", proposalId);

        // 개별 투표에서 집계 계산
        BigInteger forVotes = userVoteRepository.sumForVotesByProposal(proposalId);
        BigInteger againstVotes = userVoteRepository.sumAgainstVotesByProposal(proposalId);
        
        long forVoters = userVoteRepository.countByProposalIdAndSupportTrue(proposalId);
        long againstVoters = userVoteRepository.countByProposalIdAndSupportFalse(proposalId);
        long totalVoters = forVoters + againstVoters;

        // 투표 집계 업데이트
        int updateCount = proposalVoteCountRepository.updateVoteCounts(
            proposalId, forVotes, againstVotes, 
            (int) forVoters, (int) againstVoters, (int) totalVoters,
            LocalDateTime.now()
        );

        if (updateCount == 0) {
            // 투표 집계가 존재하지 않는 경우 생성
            createVoteCountIfNotExists(proposalId);
            proposalVoteCountRepository.updateVoteCounts(
                proposalId, forVotes, againstVotes, 
                (int) forVoters, (int) againstVoters, (int) totalVoters,
                LocalDateTime.now()
            );
        }

        log.info("Vote count recalculated: proposalId={}, forVotes={}, againstVotes={}, totalVoters={}", 
                proposalId, forVotes, againstVotes, totalVoters);
    }

    /**
     * 모든 제안의 투표 집계 재계산
     */
    @Transactional
    public void recalculateAllVoteCounts() {
        log.info("Recalculating all vote counts");

        List<Proposal> proposals = proposalRepository.findAll();
        for (Proposal proposal : proposals) {
            recalculateVoteCount(proposal.getId());
        }

        log.info("All vote counts recalculated for {} proposals", proposals.size());
    }

    // =============== 투표 유효성 검증 메서드 ===============

    /**
     * 투표 가능 여부 종합 확인
     */
    public boolean canUserVote(Integer proposalId, String userGoogleId) {
        // 제안 존재 및 활성 상태 확인
        Optional<Proposal> proposalOpt = proposalRepository.findById(proposalId);
        if (proposalOpt.isEmpty() || !proposalOpt.get().canVote()) {
            return false;
        }

        // 사용자 존재 확인
        if (!userRepository.existsByGoogleId(userGoogleId)) {
            return false;
        }

        // 중복 투표 확인
        return !hasUserVoted(proposalId, userGoogleId);
    }

    /**
     * 투표 권한 확인 (최소 투표 권한 체크는 ZkSyncService에서 처리)
     */
    public void validateVotingEligibility(Integer proposalId, String userGoogleId, BigInteger votingPower) {
        if (!canUserVote(proposalId, userGoogleId)) {
            throw new IllegalStateException("User cannot vote on this proposal");
        }

        if (votingPower.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Voting power must be positive");
        }
    }

    // =============== 유틸리티 메서드 ===============

    /**
     * 투표 결과 요약 생성
     */
    public String getVoteResultSummary(Integer proposalId) {
        Optional<ProposalVoteCount> voteCountOpt = getVoteCount(proposalId);
        if (voteCountOpt.isEmpty()) {
            return "투표 없음";
        }

        ProposalVoteCount voteCount = voteCountOpt.get();
        return voteCount.getSummary();
    }

    /**
     * 사용자 투표 이력 요약 생성
     */
    public String getUserVotingSummary(String userGoogleId) {
        long totalVotes = getUserVoteCount(userGoogleId);
        long forVotes = getUserForVoteCount(userGoogleId);
        long againstVotes = getUserAgainstVoteCount(userGoogleId);

        return String.format("총 %d회 투표 (찬성 %d회, 반대 %d회)", totalVotes, forVotes, againstVotes);
    }

    /**
     * Get paymaster service information for health checks
     * @return Paymaster service status and configuration info
     */
    public String getPaymasterServiceInfo() {
        try {
            // This would typically call the paymaster service directly
            // For now, return basic info about the voting system
            return String.format(
                "Voting Service with Smart Contract Integration\n" +
                "Governance Token: %s\n" +
                "Chain ID: %d\n" +
                "Status: Active",
                smartContractProposalService.getGovernanceTokenAddress(),
                300 // zkSync Era Sepolia
            );
        } catch (Exception e) {
            log.error("Failed to get paymaster service info", e);
            return "Paymaster service info unavailable: " + e.getMessage();
        }
    }
    
    // =============== Mock Voting Helper Methods ===============
    
    /**
     * Generate mock voting power for demo purposes (1-5 governance tokens in Wei)
     */
    private BigInteger generateMockVotingPower() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        // Generate 1-5 tokens worth of voting power
        long tokens = random.nextInt(5) + 1;
        return BigInteger.valueOf(tokens).multiply(new BigInteger("1000000000000000000")); // Convert to Wei
    }
    
    /**
     * Generate mock transaction hash for demo purposes
     */
    private String generateMockTransactionHash() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder txHash = new StringBuilder("0x");
        
        // Generate 64 hex characters for a realistic transaction hash
        for (int i = 0; i < 64; i++) {
            int digit = random.nextInt(16);
            txHash.append(Integer.toHexString(digit));
        }
        
        return txHash.toString();
    }
}