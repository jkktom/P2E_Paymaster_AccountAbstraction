package com.blooming.blockchain.springbackend.proposal;

import com.blooming.blockchain.springbackend.proposal.entity.Proposal;
import com.blooming.blockchain.springbackend.proposal.entity.ProposalVoteCount;
import com.blooming.blockchain.springbackend.proposal.entity.UserVote;
import com.blooming.blockchain.springbackend.proposal.repository.ProposalRepository;
import com.blooming.blockchain.springbackend.proposal.repository.ProposalVoteCountRepository;
import com.blooming.blockchain.springbackend.proposal.repository.UserVoteRepository;
import com.blooming.blockchain.springbackend.proposal.service.ProposalService;
import com.blooming.blockchain.springbackend.proposal.service.VotingService;
import com.blooming.blockchain.springbackend.user.entity.Role;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 제안 생성 및 투표 플로우 통합 테스트
 * 
 * 테스트 시나리오:
 * 1. 테스트 사용자 생성
 * 2. 제안 생성 (DB 저장)
 * 3. 제안자가 자신의 제안에 투표
 * 4. 다른 사용자들이 투표
 * 5. 투표 집계 확인
 * 6. DB 상태 검증
 */
@DataJpaTest
@Import({ProposalService.class, VotingService.class})
@ActiveProfiles("test")
@Transactional
class ProposalVotingIntegrationTest {

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private VotingService votingService;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ProposalVoteCountRepository proposalVoteCountRepository;

    @Autowired
    private UserVoteRepository userVoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testProposer;
    private User testVoter1;
    private User testVoter2;
    private User testVoter3;

    @BeforeEach
    void setUp() {
        // 테스트 사용자들 생성
        testProposer = createTestUser("proposer123", "proposer@test.com", "Test Proposer", "0x1234567890123456789012345678901234567890");
        testVoter1 = createTestUser("voter001", "voter1@test.com", "Test Voter 1", "0x2345678901234567890123456789012345678901");
        testVoter2 = createTestUser("voter002", "voter2@test.com", "Test Voter 2", "0x3456789012345678901234567890123456789012");
        testVoter3 = createTestUser("voter003", "voter3@test.com", "Test Voter 3", "0x4567890123456789012345678901234567890123");
    }

    @Test
    @DisplayName("완전한 제안-투표 플로우 테스트")
    void testCompleteProposalVotingFlow() {
        // Given: 제안 생성 데이터
        String description = "블록체인 거버넌스 토큰의 스테이킹 보상을 20%에서 25%로 인상하는 제안입니다.";
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        Integer blockchainProposalId = 1;
        LocalDateTime createdAt = LocalDateTime.now();
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        // When: 제안 생성
        Proposal proposal = proposalService.createProposal(
            description, 
            testProposer.getGoogleId(), 
            testProposer.getSmartWalletAddress(),
            deadline, 
            blockchainProposalId, 
            createdAt, 
            txHash
        );

        // Then: 제안이 올바르게 생성되었는지 확인
        assertThat(proposal).isNotNull();
        assertThat(proposal.getId()).isNotNull();
        assertThat(proposal.getDescription()).isEqualTo(description);
        assertThat(proposal.getProposerGoogleId()).isEqualTo(testProposer.getGoogleId());
        assertThat(proposal.getBlockchainProposalId()).isEqualTo(blockchainProposalId);
        assertThat(proposal.getExecuted()).isFalse();
        assertThat(proposal.getCanceled()).isFalse();
        assertThat(proposal.canVote()).isTrue();

        // 초기 투표 집계 확인
        Optional<ProposalVoteCount> initialVoteCount = proposalVoteCountRepository.findByProposalId(proposal.getId());
        assertThat(initialVoteCount).isPresent();
        assertThat(initialVoteCount.get().getForVotes()).isEqualTo(BigInteger.ZERO);
        assertThat(initialVoteCount.get().getAgainstVotes()).isEqualTo(BigInteger.ZERO);
        assertThat(initialVoteCount.get().getTotalVoters()).isEqualTo(0);

        // Given: 투표 데이터 (제안자가 찬성 투표)
        BigInteger proposerVotingPower = new BigInteger("5000000000000000000"); // 5 tokens
        String proposerTxHash = "0x1111111111111111111111111111111111111111111111111111111111111111";

        // When: 제안자가 자신의 제안에 찬성 투표
        UserVote proposerVote = votingService.recordVote(
            proposal.getId(),
            testProposer.getGoogleId(),
            testProposer.getSmartWalletAddress(),
            true, // 찬성
            proposerVotingPower,
            proposerTxHash
        );

        // Flush to ensure the @Modifying query is executed
        entityManager.flush();
        entityManager.clear();

        // Then: 제안자 투표 기록 확인
        assertThat(proposerVote).isNotNull();
        assertThat(proposerVote.getSupport()).isTrue();
        assertThat(proposerVote.getVotingPower()).isEqualTo(proposerVotingPower);
        assertThat(proposerVote.getUserGoogleId()).isEqualTo(testProposer.getGoogleId());

        // 투표 집계 업데이트 확인
        ProposalVoteCount afterProposerVote = proposalVoteCountRepository.findByProposalId(proposal.getId()).get();
        assertThat(afterProposerVote.getForVotes()).isEqualTo(proposerVotingPower);
        assertThat(afterProposerVote.getAgainstVotes()).isEqualTo(BigInteger.ZERO);
        assertThat(afterProposerVote.getTotalVoters()).isEqualTo(1);
        assertThat(afterProposerVote.getForVoters()).isEqualTo(1);
        assertThat(afterProposerVote.getAgainstVoters()).isEqualTo(0);

        // Given: 추가 투표자들 데이터
        BigInteger voter1Power = new BigInteger("3000000000000000000"); // 3 tokens - 찬성
        BigInteger voter2Power = new BigInteger("7000000000000000000"); // 7 tokens - 반대  
        BigInteger voter3Power = new BigInteger("2000000000000000000"); // 2 tokens - 찬성

        // When: 다른 사용자들이 투표
        UserVote vote1 = votingService.recordVote(
            proposal.getId(), testVoter1.getGoogleId(), testVoter1.getSmartWalletAddress(),
            true, voter1Power, "0x2222222222222222222222222222222222222222222222222222222222222222"
        );

        UserVote vote2 = votingService.recordVote(
            proposal.getId(), testVoter2.getGoogleId(), testVoter2.getSmartWalletAddress(),
            false, voter2Power, "0x3333333333333333333333333333333333333333333333333333333333333333"
        );

        UserVote vote3 = votingService.recordVote(
            proposal.getId(), testVoter3.getGoogleId(), testVoter3.getSmartWalletAddress(),
            true, voter3Power, "0x4444444444444444444444444444444444444444444444444444444444444444"
        );

        // Flush to ensure all @Modifying queries are executed
        entityManager.flush();
        entityManager.clear();

        // Then: 모든 투표 기록 확인
        assertThat(vote1.getSupport()).isTrue();
        assertThat(vote2.getSupport()).isFalse();
        assertThat(vote3.getSupport()).isTrue();

        // 최종 투표 집계 확인
        ProposalVoteCount finalVoteCount = proposalVoteCountRepository.findByProposalId(proposal.getId()).get();
        
        // 찬성: 5 + 3 + 2 = 10 tokens
        BigInteger expectedForVotes = proposerVotingPower.add(voter1Power).add(voter3Power);
        // 반대: 7 tokens
        BigInteger expectedAgainstVotes = voter2Power;

        assertThat(finalVoteCount.getForVotes()).isEqualTo(expectedForVotes);
        assertThat(finalVoteCount.getAgainstVotes()).isEqualTo(expectedAgainstVotes);
        assertThat(finalVoteCount.getTotalVoters()).isEqualTo(4);
        assertThat(finalVoteCount.getForVoters()).isEqualTo(3);
        assertThat(finalVoteCount.getAgainstVoters()).isEqualTo(1);

        // 계산된 필드 확인
        assertThat(finalVoteCount.getTotalVotes()).isEqualTo(expectedForVotes.add(expectedAgainstVotes));
        assertThat(finalVoteCount.isPassed()).isTrue(); // 찬성이 반대보다 많음
        assertThat(finalVoteCount.getForPercentage()).isGreaterThan(50.0);

        // DB에서 투표 기록 확인
        List<UserVote> allVotes = userVoteRepository.findByProposalIdOrderByVotedAtDesc(proposal.getId());
        assertThat(allVotes).hasSize(4);

        // 찬성/반대 투표 분리 확인
        List<UserVote> forVotes = userVoteRepository.findByProposalIdAndSupportTrueOrderByVotingPowerDesc(proposal.getId());
        List<UserVote> againstVotes = userVoteRepository.findByProposalIdAndSupportFalseOrderByVotingPowerDesc(proposal.getId());
        
        assertThat(forVotes).hasSize(3);
        assertThat(againstVotes).hasSize(1);

        // 투표력 순서 확인 (찬성: 5, 3, 2 순서)
        assertThat(forVotes.get(0).getVotingPower()).isEqualTo(proposerVotingPower); // 5 tokens
        assertThat(forVotes.get(1).getVotingPower()).isEqualTo(voter1Power); // 3 tokens
        assertThat(forVotes.get(2).getVotingPower()).isEqualTo(voter3Power); // 2 tokens
    }

    @Test
    @DisplayName("중복 투표 방지 테스트")
    void testDuplicateVotePrevention() {
        // Given: 제안 생성
        Proposal proposal = createTestProposal();

        // When: 첫 번째 투표
        votingService.recordVote(
            proposal.getId(),
            testVoter1.getGoogleId(),
            testVoter1.getSmartWalletAddress(),
            true,
            new BigInteger("1000000000000000000"),
            "0x1111111111111111111111111111111111111111111111111111111111111111"
        );

        // Then: 같은 사용자가 다시 투표하면 예외 발생
        assertThatThrownBy(() -> votingService.recordVote(
            proposal.getId(),
            testVoter1.getGoogleId(),
            testVoter1.getSmartWalletAddress(),
            false,
            new BigInteger("2000000000000000000"),
            "0x2222222222222222222222222222222222222222222222222222222222222222"
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("User has already voted");
    }

    @Test
    @DisplayName("만료된 제안 투표 방지 테스트")
    void testExpiredProposalVotingPrevention() {
        // Given: 짧은 기간의 제안 생성 (1초 후 마감)
        LocalDateTime shortDeadline = LocalDateTime.now().plusSeconds(1);
        Proposal shortProposal = proposalService.createProposal(
            "짧은 기간 제안",
            testProposer.getGoogleId(),
            testProposer.getSmartWalletAddress(),
            shortDeadline,
            999,
            LocalDateTime.now(),
            "0xshort"
        );

        // When: 제안이 만료될 때까지 잠시 대기
        try {
            Thread.sleep(1100); // 1.1초 대기하여 제안 만료
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then: 만료된 제안에 투표하면 예외 발생
        assertThatThrownBy(() -> votingService.recordVote(
            shortProposal.getId(),
            testVoter1.getGoogleId(),
            testVoter1.getSmartWalletAddress(),
            true,
            new BigInteger("1000000000000000000"),
            "0x1111111111111111111111111111111111111111111111111111111111111111"
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Voting is not allowed");
    }

    @Test
    @DisplayName("투표 집계 재계산 테스트")
    void testVoteCountRecalculation() {
        // Given: 제안 생성 및 투표
        Proposal proposal = createTestProposal();
        
        votingService.recordVote(proposal.getId(), testVoter1.getGoogleId(), testVoter1.getSmartWalletAddress(),
            true, new BigInteger("3000000000000000000"), "0x1111");
        votingService.recordVote(proposal.getId(), testVoter2.getGoogleId(), testVoter2.getSmartWalletAddress(),
            false, new BigInteger("2000000000000000000"), "0x2222");

        // EntityManager flush and clear to ensure all changes are persisted
        entityManager.flush();
        entityManager.clear();

        // 집계를 임의로 변경 (동기화 오류 시뮬레이션)
        ProposalVoteCount voteCount = proposalVoteCountRepository.findByProposalId(proposal.getId()).get();
        voteCount.setForVotes(BigInteger.ZERO);
        voteCount.setAgainstVotes(BigInteger.ZERO);
        voteCount.setTotalVoters(0);
        voteCount.setForVoters(0);
        voteCount.setAgainstVoters(0);
        proposalVoteCountRepository.save(voteCount);

        // EntityManager flush to ensure changes are persisted
        entityManager.flush();

        // When: 투표 집계 재계산
        votingService.recalculateVoteCount(proposal.getId());

        // EntityManager flush and clear to ensure the update query is executed
        entityManager.flush();
        entityManager.clear();

        // Then: 올바른 집계로 복구됨
        ProposalVoteCount recalculatedVoteCount = proposalVoteCountRepository.findByProposalId(proposal.getId()).get();
        assertThat(recalculatedVoteCount.getForVotes()).isEqualTo(new BigInteger("3000000000000000000"));
        assertThat(recalculatedVoteCount.getAgainstVotes()).isEqualTo(new BigInteger("2000000000000000000"));
        assertThat(recalculatedVoteCount.getTotalVoters()).isEqualTo(2);
        assertThat(recalculatedVoteCount.getForVoters()).isEqualTo(1);
        assertThat(recalculatedVoteCount.getAgainstVoters()).isEqualTo(1);
    }

    @Test
    @DisplayName("사용자별 투표 이력 조회 테스트")
    void testUserVotingHistory() {
        // Given: 여러 제안 생성 및 투표
        Proposal proposal1 = createTestProposal();
        Proposal proposal2 = proposalService.createProposal(
            "두 번째 제안", testProposer.getGoogleId(), testProposer.getSmartWalletAddress(),
            LocalDateTime.now().plusDays(5), 2, LocalDateTime.now(), "0xsecond"
        );

        // testVoter1이 두 제안에 모두 투표
        votingService.recordVote(proposal1.getId(), testVoter1.getGoogleId(), testVoter1.getSmartWalletAddress(),
            true, new BigInteger("1000000000000000000"), "0x1111");
        votingService.recordVote(proposal2.getId(), testVoter1.getGoogleId(), testVoter1.getSmartWalletAddress(),
            false, new BigInteger("2000000000000000000"), "0x2222");

        // When: 사용자 투표 이력 조회
        List<UserVote> userVotes = votingService.getVotesByUser(testVoter1.getGoogleId());

        // Then: 올바른 투표 이력 확인
        assertThat(userVotes).hasSize(2);
        assertThat(votingService.getUserVoteCount(testVoter1.getGoogleId())).isEqualTo(2);
        assertThat(votingService.getUserForVoteCount(testVoter1.getGoogleId())).isEqualTo(1);
        assertThat(votingService.getUserAgainstVoteCount(testVoter1.getGoogleId())).isEqualTo(1);
    }

    // =============== 헬퍼 메서드 ===============

    private User createTestUser(String googleId, String email, String name, String walletAddress) {
        User user = User.builder()
            .googleId(googleId)
            .email(email)
            .name(name)
            .smartWalletAddress(walletAddress)
            .roleId((byte) 2) // USER role ID = 2
            .build();
        return userRepository.save(user);
    }

    private Proposal createTestProposal() {
        return proposalService.createProposal(
            "테스트 제안: 거버넌스 토큰 배분 정책 변경",
            testProposer.getGoogleId(),
            testProposer.getSmartWalletAddress(),
            LocalDateTime.now().plusDays(7),
            1,
            LocalDateTime.now(),
            "0xtest123"
        );
    }
}