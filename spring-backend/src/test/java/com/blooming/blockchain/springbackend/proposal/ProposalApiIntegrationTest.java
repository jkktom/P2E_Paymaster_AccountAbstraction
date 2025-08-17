package com.blooming.blockchain.springbackend.proposal;

import com.blooming.blockchain.springbackend.proposal.dto.CreateProposalRequest;
import com.blooming.blockchain.springbackend.proposal.dto.VoteRequest;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Proposal API 통합 테스트
 * 스마트 컨트랙트 통합을 포함한 전체 API 플로우 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProposalApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testProposer;
    private User testVoter1;
    private User testVoter2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // 테스트 사용자들 생성
        testProposer = createTestUser("test-proposer-123", "proposer@test.com", "Test Proposer", "0x1234567890123456789012345678901234567890");
        testVoter1 = createTestUser("test-voter-001", "voter1@test.com", "Test Voter 1", "0x2345678901234567890123456789012345678901");
        testVoter2 = createTestUser("test-voter-002", "voter2@test.com", "Test Voter 2", "0x3456789012345678901234567890123456789012");
    }

    @Test
    @DisplayName("완전한 제안-투표 API 플로우 테스트")
    void testCompleteProposalVotingApiFlow() throws Exception {
        // 1. 초기 상태 확인 - 제안 통계
        mockMvc.perform(get("/api/proposals/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProposals").exists())
                .andExpect(jsonPath("$.activeProposals").exists());

        // 2. 제안 생성 요청
        CreateProposalRequest createRequest = new CreateProposalRequest();
        createRequest.setDescription("테스트 제안: zkSync Era 거버넌스 토큰의 스테이킹 보상률을 20%에서 25%로 인상하는 안건입니다. 이는 더 많은 사용자의 참여를 유도하고 네트워크 보안을 강화하기 위한 조치입니다.");
        createRequest.setProposerGoogleId(testProposer.getGoogleId());
        createRequest.setDeadline(LocalDateTime.now().plusDays(7));

        String createRequestJson = objectMapper.writeValueAsString(createRequest);

        // 제안 생성 실행
        String createResponse = mockMvc.perform(post("/api/proposals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.blockchainProposalId").exists())
                .andExpect(jsonPath("$.description").value(createRequest.getDescription()))
                .andExpect(jsonPath("$.proposerGoogleId").value(testProposer.getGoogleId()))
                .andExpect(jsonPath("$.txHash").exists())
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.canVote").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 응답에서 제안 ID 추출
        Long proposalId = objectMapper.readTree(createResponse).get("id").asLong();

        // 3. 생성된 제안 조회
        mockMvc.perform(get("/api/proposals/{proposalId}", proposalId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(proposalId))
                .andExpect(jsonPath("$.isActive").value(true));

        // 4. 첫 번째 투표 (찬성)
        VoteRequest voteForRequest = new VoteRequest();
        voteForRequest.setProposalId(proposalId);
        voteForRequest.setUserGoogleId(testVoter1.getGoogleId());
        voteForRequest.setSupport(true);

        String voteForRequestJson = objectMapper.writeValueAsString(voteForRequest);

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(voteForRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.proposalId").value(proposalId))
                .andExpect(jsonPath("$.userGoogleId").value(testVoter1.getGoogleId()))
                .andExpect(jsonPath("$.support").value(true))
                .andExpect(jsonPath("$.votingPower").exists())
                .andExpect(jsonPath("$.txHash").exists());

        // 5. 두 번째 투표 (반대)
        VoteRequest voteAgainstRequest = new VoteRequest();
        voteAgainstRequest.setProposalId(proposalId);
        voteAgainstRequest.setUserGoogleId(testVoter2.getGoogleId());
        voteAgainstRequest.setSupport(false);

        String voteAgainstRequestJson = objectMapper.writeValueAsString(voteAgainstRequest);

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(voteAgainstRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.support").value(false));

        // 6. 투표 기록 조회
        mockMvc.perform(get("/api/votes/proposal/{proposalId}", proposalId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // 7. 찬성 투표만 조회
        mockMvc.perform(get("/api/votes/proposal/{proposalId}/for", proposalId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].support").value(true));

        // 8. 반대 투표만 조회
        mockMvc.perform(get("/api/votes/proposal/{proposalId}/against", proposalId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].support").value(false));

        // 9. 투표 통계 확인
        mockMvc.perform(get("/api/votes/stats/proposal/{proposalId}", proposalId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVoters").value(2))
                .andExpect(jsonPath("$.forVoters").value(1))
                .andExpect(jsonPath("$.againstVoters").value(1));

        // 10. 사용자 투표 이력 확인
        mockMvc.perform(get("/api/votes/user/{userGoogleId}", testVoter1.getGoogleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // 11. 활성 제안 목록에서 확인
        mockMvc.perform(get("/api/proposals/active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == " + proposalId + ")]").exists());
    }

    @Test
    @DisplayName("중복 투표 방지 테스트")
    void testDuplicateVotePrevention() throws Exception {
        // 제안 생성
        CreateProposalRequest createRequest = new CreateProposalRequest();
        createRequest.setDescription("중복 투표 방지 테스트용 제안입니다. 이는 시스템의 보안을 확인하기 위한 테스트 제안입니다.");
        createRequest.setProposerGoogleId(testProposer.getGoogleId());
        createRequest.setDeadline(LocalDateTime.now().plusDays(3));

        String createResponse = mockMvc.perform(post("/api/proposals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long proposalId = objectMapper.readTree(createResponse).get("id").asLong();

        // 첫 번째 투표
        VoteRequest voteRequest = new VoteRequest();
        voteRequest.setProposalId(proposalId);
        voteRequest.setUserGoogleId(testVoter1.getGoogleId());
        voteRequest.setSupport(true);

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isOk());

        // 같은 사용자의 두 번째 투표 시도 (실패해야 함)
        voteRequest.setSupport(false); // 다른 선택으로 변경

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 400 에러 예상
    }

    @Test
    @DisplayName("제안 검색 테스트")
    void testProposalSearch() throws Exception {
        // 검색용 제안 생성
        CreateProposalRequest createRequest = new CreateProposalRequest();
        createRequest.setDescription("검색 테스트용 특별한 키워드가 포함된 제안입니다. zkSync 네트워크 개선안입니다.");
        createRequest.setProposerGoogleId(testProposer.getGoogleId());
        createRequest.setDeadline(LocalDateTime.now().plusDays(5));

        mockMvc.perform(post("/api/proposals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // 키워드로 검색
        mockMvc.perform(get("/api/proposals/search")
                        .param("keyword", "zkSync")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.description =~ /.*zkSync.*/)]").exists());
    }

    @Test
    @DisplayName("사용자별 제안 조회 테스트")
    void testGetProposalsByUser() throws Exception {
        // 특정 사용자의 제안 2개 생성
        for (int i = 1; i <= 2; i++) {
            CreateProposalRequest createRequest = new CreateProposalRequest();
            createRequest.setDescription("사용자별 조회 테스트용 제안 " + i + "번입니다. 테스트를 위한 상세한 설명이 포함되어 있습니다.");
            createRequest.setProposerGoogleId(testProposer.getGoogleId());
            createRequest.setDeadline(LocalDateTime.now().plusDays(i + 1));

            mockMvc.perform(post("/api/proposals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());
        }

        // 해당 사용자의 제안 조회
        mockMvc.perform(get("/api/proposals/user/{userGoogleId}", testProposer.getGoogleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].proposerGoogleId").value(testProposer.getGoogleId()))
                .andExpect(jsonPath("$[1].proposerGoogleId").value(testProposer.getGoogleId()));
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
}