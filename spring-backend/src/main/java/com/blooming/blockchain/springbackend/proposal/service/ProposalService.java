package com.blooming.blockchain.springbackend.proposal.service;

import com.blooming.blockchain.springbackend.proposal.entity.Proposal;
import com.blooming.blockchain.springbackend.proposal.entity.ProposalVoteCount;
import com.blooming.blockchain.springbackend.proposal.repository.ProposalRepository;
import com.blooming.blockchain.springbackend.proposal.repository.ProposalVoteCountRepository;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.repository.UserRepository;
import com.blooming.blockchain.springbackend.zksync.dto.CreateProposalResult;
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
 * ProposalService - 제안 관리 서비스
 * 
 * 기본 CRUD 로직 및 제안 상태 관리를 담당합니다.
 * 스마트 컨트랙트와의 연동은 SmartContractProposalService에서 처리됩니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProposalVoteCountRepository proposalVoteCountRepository;
    private final UserRepository userRepository;
    private final SmartContractProposalService smartContractProposalService;
    private final BlockchainProposalIdManager blockchainProposalIdManager;

    // =============== 조회 메서드 ===============

    /**
     * 모든 제안 조회 (페이징)
     */
    public Page<Proposal> getAllProposals(Pageable pageable) {
        return proposalRepository.findAll(pageable);
    }

    /**
     * 제안 ID로 조회
     */
    public Optional<Proposal> getProposalById(Integer proposalId) {
        return proposalRepository.findById(proposalId);
    }

    /**
     * 블록체인 제안 ID로 조회
     */
    public Optional<Proposal> getProposalByBlockchainId(Integer blockchainProposalId) {
        return proposalRepository.findById(blockchainProposalId);
    }

    /**
     * 제안 존재 여부 확인
     */
    public boolean existsById(Integer proposalId) {
        return proposalRepository.existsById(proposalId);
    }

    /**
     * 블록체인 제안 ID 존재 여부 확인
     */
    public boolean existsByBlockchainId(Integer blockchainProposalId) {
        return proposalRepository.existsById(blockchainProposalId);
    }

    // =============== 상태별 조회 메서드 ===============

    /**
     * 활성 제안 조회 (투표 가능한)
     */
    public List<Proposal> getActiveProposals() {
        return proposalRepository.findActiveProposals(LocalDateTime.now());
    }

    /**
     * 활성 제안 페이징 조회
     */
    public Page<Proposal> getActiveProposals(Pageable pageable) {
        return proposalRepository.findActiveProposals(LocalDateTime.now(), pageable);
    }

    /**
     * 완료된 제안 조회 (실행되었거나 취소된)
     */
    public Page<Proposal> getCompletedProposals(Pageable pageable) {
        return proposalRepository.findCompletedProposals(pageable);
    }

    /**
     * 만료된 제안 조회 (마감일이 지났지만 실행되지 않은)
     */
    public Page<Proposal> getExpiredProposals(Pageable pageable) {
        return proposalRepository.findExpiredProposals(LocalDateTime.now(), pageable);
    }

    /**
     * 곧 마감되는 제안 조회 (24시간 내)
     */
    public List<Proposal> getProposalsEndingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soonDeadline = now.plusHours(24);
        return proposalRepository.findProposalsEndingSoon(now, soonDeadline);
    }

    // =============== 사용자별 조회 메서드 ===============

    /**
     * 특정 사용자가 생성한 제안 조회
     */
    public List<Proposal> getProposalsByUser(String userGoogleId) {
        return proposalRepository.findByProposerGoogleIdOrderByCreatedAtDesc(userGoogleId);
    }

    /**
     * 특정 사용자가 생성한 제안 페이징 조회
     */
    public Page<Proposal> getProposalsByUser(String userGoogleId, Pageable pageable) {
        return proposalRepository.findByProposerGoogleId(userGoogleId, pageable);
    }

    /**
     * 특정 사용자의 활성 제안 조회
     */
    public List<Proposal> getActiveProposalsByUser(String userGoogleId) {
        return proposalRepository.findActiveProposalsByUser(userGoogleId, LocalDateTime.now());
    }

    /**
     * 특정 사용자가 활성 제안을 가지고 있는지 확인
     */
    public boolean hasActiveProposals(String userGoogleId) {
        return proposalRepository.hasActiveProposals(userGoogleId, LocalDateTime.now());
    }

    // =============== 검색 메서드 ===============

    /**
     * 제안 설명으로 검색
     */
    public Page<Proposal> searchProposals(String keyword, Pageable pageable) {
        return proposalRepository.searchByDescription(keyword, pageable);
    }

    /**
     * 활성 제안 중에서 검색
     */
    public Page<Proposal> searchActiveProposals(String keyword, Pageable pageable) {
        return proposalRepository.searchActiveProposals(keyword, LocalDateTime.now(), pageable);
    }

    // =============== 통계 메서드 ===============

    /**
     * 전체 제안 수 조회
     */
    public long getTotalProposalCount() {
        return proposalRepository.countAllProposals();
    }

    /**
     * 활성 제안 수 조회
     */
    public long getActiveProposalCount() {
        return proposalRepository.countActiveProposals(LocalDateTime.now());
    }

    /**
     * 특정 사용자가 생성한 제안 수 조회
     */
    public long getProposalCountByUser(String userGoogleId) {
        return proposalRepository.countByProposerGoogleId(userGoogleId);
    }

    /**
     * 실행된 제안 수 조회
     */
    public long getExecutedProposalCount() {
        return proposalRepository.countByExecutedTrue();
    }

    /**
     * 취소된 제안 수 조회
     */
    public long getCanceledProposalCount() {
        return proposalRepository.countByCanceledTrue();
    }

    // =============== 생성 메서드 ===============

    /**
     * 스마트 컨트랙트와 통합된 제안 생성 (새로운 블록체인 동기화 방식)
     * 1. BlockchainProposalIdManager에서 다음 제안 ID 획득
     * 2. 해당 ID로 스마트 컨트랙트에 제안 생성
     * 3. 성공 시 같은 ID로 백엔드 데이터베이스에 저장
     * 
     * @param description 제안 설명
     * @param proposerGoogleId 제안자 Google ID
     * @param deadline 투표 마감일
     * @return 생성된 제안 (스마트 컨트랙트와 데이터베이스 완전 동기화)
     */
    @Transactional
    public Proposal createProposalWithSmartContract(String description, String proposerGoogleId, LocalDateTime deadline) {
        log.info("Creating proposal with blockchain ID synchronization: proposer={}, description={}", 
                proposerGoogleId, description.substring(0, Math.min(50, description.length())));

        // 제안자 유효성 확인
        Optional<User> proposer = userRepository.findByGoogleId(proposerGoogleId);
        if (proposer.isEmpty()) {
            throw new IllegalArgumentException("Proposer not found: " + proposerGoogleId);
        }

        User proposerUser = proposer.get();
        String proposerWalletAddress = proposerUser.getSmartWalletAddress();

        // 마감일 유효성 확인
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }

        // 1. 블록체인과 동기화된 다음 제안 ID 획득
        Integer nextProposalId = blockchainProposalIdManager.getNextProposalId();
        log.info("Using synchronized proposal ID: {}", nextProposalId);

        try {
            // 2. 지정된 ID로 스마트 컨트랙트에 제안 생성
            // Note: SmartContractProposalService를 업데이트해서 ID를 받도록 해야 함
            CreateProposalResult smartContractResult = smartContractProposalService
                .createProposalWithId(nextProposalId, description, proposerWalletAddress, deadline)
                .join(); // 블로킹 호출 - 트랜잭션 내에서 처리

            if (!smartContractResult.isSuccess()) {
                throw new RuntimeException("Smart contract proposal creation failed: " + smartContractResult.getErrorMessage());
            }

            // 3. 같은 ID로 백엔드 데이터베이스에 저장
            Proposal proposal = createProposal(
                nextProposalId, // ID를 직접 지정
                description,
                proposerGoogleId,
                proposerWalletAddress,
                deadline,
                LocalDateTime.now(), // 현재 시간을 생성 시간으로 사용
                smartContractResult.getTxHash()
            );

            // 4. ID Manager에 성공 확인
            blockchainProposalIdManager.confirmProposalCreated(nextProposalId);

            log.info("Successfully created proposal with blockchain synchronization: proposalId={}, txHash={}", 
                    proposal.getId(), smartContractResult.getTxHash());

            return proposal;

        } catch (Exception e) {
            log.error("Failed to create proposal with blockchain synchronization: proposalId={}, proposer={}, error={}", 
                    nextProposalId, proposerGoogleId, e.getMessage(), e);
            throw new RuntimeException("Failed to create proposal: " + e.getMessage(), e);
        }
    }

    /**
     * 새 제안 생성 (블록체인 동기화 방식) - ID를 먼저 지정
     * 
     * @param proposalId 블록체인과 동기화된 제안 ID (Primary Key)
     * @param description 제안 설명
     * @param proposerGoogleId 제안자 Google ID
     * @param proposerWalletAddress 제안자 지갑 주소
     * @param deadline 투표 마감일
     * @param createdAt 블록체인에서의 생성 시간
     * @param txHash 생성 트랜잭션 해시
     * @return 생성된 제안
     */
    @Transactional
    public Proposal createProposal(Integer proposalId, String description, String proposerGoogleId, 
                                  String proposerWalletAddress, LocalDateTime deadline,
                                  LocalDateTime createdAt, String txHash) {
        
        log.info("Creating proposal with ID: id={}, description={}, proposer={}, deadline={}", 
                proposalId, description, proposerGoogleId, deadline);

        // 제안 ID 중복 확인 (이제 Primary Key이므로)
        if (proposalRepository.existsById(proposalId)) {
            throw new IllegalArgumentException("Proposal ID already exists: " + proposalId);
        }

        // 제안자 유효성 확인
        Optional<User> proposer = userRepository.findByGoogleId(proposerGoogleId);
        if (proposer.isEmpty()) {
            throw new IllegalArgumentException("Proposer not found: " + proposerGoogleId);
        }

        // 지갑 주소 일치 확인
        if (!proposerWalletAddress.equals(proposer.get().getSmartWalletAddress())) {
            throw new IllegalArgumentException("Wallet address mismatch for user: " + proposerGoogleId);
        }

        // 마감일 유효성 확인
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }

        // 제안 생성
        Proposal proposal = Proposal.builder()
                .id(proposalId) // 블록체인과 동기화된 ID 직접 지정
                .description(description)
                .proposerAddress(proposerWalletAddress)
                .deadline(deadline)
                .executed(false)
                .canceled(false)
                .createdAt(createdAt)
                .txHash(txHash)
                .proposerGoogleId(proposerGoogleId)
                .build();

        Proposal savedProposal = proposalRepository.save(proposal);

        // 투표 집계 초기화
        createInitialVoteCount(proposalId);

        log.info("Successfully created proposal: id={}, txHash={}", savedProposal.getId(), txHash);

        return savedProposal;
    }

    /**
     * 레거시 제안 생성 (데이터베이스에만 저장, 블록체인 연동은 별도)
     * @deprecated Use createProposal(Integer proposalId, ...) instead for blockchain synchronization
     */
    @Deprecated
    @Transactional
    public Proposal createProposal(String description, String proposerGoogleId, 
                                  String proposerWalletAddress, LocalDateTime deadline,
                                  Integer blockchainProposalId, LocalDateTime createdAt, 
                                  String txHash) {
        
        log.info("Creating proposal: description={}, proposer={}, deadline={}, blockchainId={}", 
                description, proposerGoogleId, deadline, blockchainProposalId);

        // 블록체인 제안 ID 중복 확인
        if (existsByBlockchainId(blockchainProposalId)) {
            throw new IllegalArgumentException("Blockchain proposal ID already exists: " + blockchainProposalId);
        }

        // 제안자 유효성 확인
        Optional<User> proposer = userRepository.findByGoogleId(proposerGoogleId);
        if (proposer.isEmpty()) {
            throw new IllegalArgumentException("Proposer not found: " + proposerGoogleId);
        }

        // 지갑 주소 일치 확인
        if (!proposerWalletAddress.equals(proposer.get().getSmartWalletAddress())) {
            throw new IllegalArgumentException("Wallet address mismatch for user: " + proposerGoogleId);
        }

        // 마감일 유효성 확인
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }

        // 제안 생성
        Proposal proposal = Proposal.builder()
            .id(blockchainProposalId) // 블록체인 제안 ID를 Primary Key로 사용
            .description(description)
            .proposerAddress(proposerWalletAddress)
            .proposerGoogleId(proposerGoogleId)
            .deadline(deadline)
            .executed(false)
            .canceled(false)
            .createdAt(createdAt)
            .txHash(txHash)
            .build();

        Proposal savedProposal = proposalRepository.save(proposal);

        // 투표 집계 초기화
        createInitialVoteCount(savedProposal.getId());

        log.info("Proposal created successfully: id={}, blockchainId={}", 
                savedProposal.getId(), savedProposal.getId());

        return savedProposal;
    }

    /**
     * 투표 집계 초기화
     */
    @Transactional
    public void createInitialVoteCount(Integer proposalId) {
        if (proposalVoteCountRepository.existsByProposalId(proposalId)) {
            log.warn("Vote count already exists for proposal: {}", proposalId);
            return;
        }

        ProposalVoteCount voteCount = ProposalVoteCount.builder()
            .proposalId(proposalId)
            .forVotes(BigInteger.ZERO)
            .againstVotes(BigInteger.ZERO)
            .totalVoters(0)
            .forVoters(0)
            .againstVoters(0)
            .lastUpdated(LocalDateTime.now())
            .build();

        proposalVoteCountRepository.save(voteCount);
        log.info("Initial vote count created for proposal: {}", proposalId);
    }

    // =============== 상태 업데이트 메서드 ===============

    /**
     * 제안 실행 상태 업데이트
     */
    @Transactional
    public void markAsExecuted(Integer proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getExecuted()) {
            log.warn("Proposal already executed: {}", proposalId);
            return;
        }

        if (proposal.getCanceled()) {
            throw new IllegalStateException("Cannot execute canceled proposal: " + proposalId);
        }

        proposal.setExecuted(true);
        proposalRepository.save(proposal);

        log.info("Proposal marked as executed: {}", proposalId);
    }

    /**
     * 제안 취소 상태 업데이트
     */
    @Transactional
    public void markAsCanceled(Integer proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getCanceled()) {
            log.warn("Proposal already canceled: {}", proposalId);
            return;
        }

        if (proposal.getExecuted()) {
            throw new IllegalStateException("Cannot cancel executed proposal: " + proposalId);
        }

        proposal.setCanceled(true);
        proposalRepository.save(proposal);

        log.info("Proposal marked as canceled: {}", proposalId);
    }

    /**
     * 블록체인 제안 ID로 실행 상태 업데이트
     */
    @Transactional
    public void markAsExecutedByBlockchainId(Integer blockchainProposalId) {
        Proposal proposal = proposalRepository.findById(blockchainProposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + blockchainProposalId));

        markAsExecuted(proposal.getId());
    }

    /**
     * 블록체인 제안 ID로 취소 상태 업데이트
     */
    @Transactional
    public void markAsCanceledByBlockchainId(Integer blockchainProposalId) {
        Proposal proposal = proposalRepository.findById(blockchainProposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + blockchainProposalId));

        markAsExecuted(proposal.getId());
    }

    // =============== 유틸리티 메서드 ===============

    /**
     * 제안 유효성 검증
     */
    public void validateProposal(Integer proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (!proposal.canVote()) {
            throw new IllegalStateException("Proposal is not available for voting: " + proposalId);
        }
    }

    /**
     * 투표 가능 여부 확인
     */
    public boolean canVote(Integer proposalId) {
        return proposalRepository.findById(proposalId)
            .map(Proposal::canVote)
            .orElse(false);
    }

    /**
     * 제안 상태 요약 정보 생성
     */
    public String getProposalStatusSummary(Integer proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getExecuted()) {
            return "실행됨";
        } else if (proposal.getCanceled()) {
            return "취소됨";
        } else if (proposal.isExpired()) {
            return "만료됨";
        } else if (proposal.isActive()) {
            return "진행중";
        } else {
            return "알 수 없음";
        }
    }
}