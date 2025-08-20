package com.blooming.blockchain.springbackend.proposal.controller;

import com.blooming.blockchain.springbackend.proposal.dto.VoteRequest;
import com.blooming.blockchain.springbackend.proposal.dto.VoteResponse;
import com.blooming.blockchain.springbackend.proposal.dto.TransactionData;
import com.blooming.blockchain.springbackend.proposal.entity.UserVote;
import com.blooming.blockchain.springbackend.proposal.service.VotingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

/**
 * Voting REST API Controller
 */
@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
@Slf4j
public class VotingController {

    private final VotingService votingService;

    /**
     * 새로운 투표 실행 (스마트 컨트랙트 통합)
     */
    @PostMapping
    public ResponseEntity<VoteResponse> vote(@Valid @RequestBody VoteRequest request) {
        log.info("Voting: proposalId={}, user={}, support={}", 
                request.getProposalId(), request.getUserGoogleId(), request.getSupport());

        try {
            UserVote userVote = votingService.voteWithSmartContract(
                request.getProposalId(),
                request.getUserGoogleId(),
                request.getSupport()
            );

            VoteResponse response = VoteResponse.fromEntity(userVote);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to vote: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 투표 실행 (프론트엔드 호환성을 위한 별칭 엔드포인트)
     */
    @PostMapping("/vote")
    public ResponseEntity<VoteResponse> voteAlias(@Valid @RequestBody VoteRequest request) {
        return vote(request);
    }

    /**
     * Web3Auth용 투표 트랜잭션 데이터 준비
     * 프론트엔드가 ephemeral key로 실행할 트랜잭션 데이터 반환
     */
    @PostMapping("/prepare")
    public ResponseEntity<TransactionData> prepareVoteTransaction(@Valid @RequestBody VoteRequest request) {
        log.info("Preparing vote transaction: proposalId={}, user={}, support={}", 
                request.getProposalId(), request.getUserGoogleId(), request.getSupport());

        try {
            TransactionData transactionData = votingService.prepareVoteTransaction(
                request.getProposalId(),
                request.getUserGoogleId(),
                request.getSupport()
            );

            return ResponseEntity.ok(transactionData);

        } catch (Exception e) {
            log.error("Failed to prepare vote transaction: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 제안의 모든 투표 조회
     */
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<List<VoteResponse>> getVotesByProposal(@PathVariable Integer proposalId) {
        List<UserVote> votes = votingService.getVotesByProposal(proposalId);
        List<VoteResponse> responses = votes.stream()
                .map(VoteResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 사용자의 모든 투표 조회
     */
    @GetMapping("/user/{userGoogleId}")
    public ResponseEntity<List<VoteResponse>> getVotesByUser(@PathVariable String userGoogleId) {
        List<UserVote> votes = votingService.getVotesByUser(userGoogleId);
        List<VoteResponse> responses = votes.stream()
                .map(VoteResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 제안의 찬성 투표 조회
     */
    @GetMapping("/proposal/{proposalId}/for")
    public ResponseEntity<List<VoteResponse>> getForVotesByProposal(@PathVariable Integer proposalId) {
        List<UserVote> votes = votingService.getForVotesByProposal(proposalId);
        List<VoteResponse> responses = votes.stream()
                .map(VoteResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 제안의 반대 투표 조회
     */
    @GetMapping("/proposal/{proposalId}/against")
    public ResponseEntity<List<VoteResponse>> getAgainstVotesByProposal(@PathVariable Integer proposalId) {
        List<UserVote> votes = votingService.getAgainstVotesByProposal(proposalId);
        List<VoteResponse> responses = votes.stream()
                .map(VoteResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 사용자가 특정 제안에 투표했는지 확인
     */
    @GetMapping("/check/{proposalId}/{userGoogleId}")
    public ResponseEntity<VoteCheckResponse> checkUserVote(
            @PathVariable Integer proposalId, 
            @PathVariable String userGoogleId) {
        
        boolean hasVoted = votingService.hasUserVoted(proposalId, userGoogleId);
        boolean canVote = votingService.canUserVote(proposalId, userGoogleId);
        
        VoteCheckResponse response = VoteCheckResponse.builder()
                .hasVoted(hasVoted)
                .canVote(canVote)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 투표 통계
     */
    @GetMapping("/stats/proposal/{proposalId}")
    public ResponseEntity<VoteStats> getVoteStats(@PathVariable Integer proposalId) {
        VoteStats stats = VoteStats.builder()
                .totalVoters(votingService.getVoterCount(proposalId))
                .forVoters(votingService.getForVoterCount(proposalId))
                .againstVoters(votingService.getAgainstVoterCount(proposalId))
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * 사용자 투표 통계
     */
    @GetMapping("/stats/user/{userGoogleId}")
    public ResponseEntity<UserVoteStats> getUserVoteStats(@PathVariable String userGoogleId) {
        UserVoteStats stats = UserVoteStats.builder()
                .totalVotes(votingService.getUserVoteCount(userGoogleId))
                .forVotes(votingService.getUserForVoteCount(userGoogleId))
                .againstVotes(votingService.getUserAgainstVoteCount(userGoogleId))
                .summary(votingService.getUserVotingSummary(userGoogleId))
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Test endpoint to verify paymaster voting integration
     * This endpoint tests the new ZkSyncEraPaymasterService for voting
     */
    @PostMapping("/test-paymaster-vote")
    public ResponseEntity<Map<String, Object>> testPaymasterVote(@Valid @RequestBody VoteRequest request) {
        log.info("Testing paymaster voting integration: proposalId={}, user={}, support={}", 
                request.getProposalId(), request.getUserGoogleId(), request.getSupport());
        
        try {
            // Test the new paymaster service directly
            UserVote userVote = votingService.voteWithSmartContract(
                request.getProposalId(), 
                request.getUserGoogleId(), 
                request.getSupport()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paymaster voting test successful");
            response.put("voteId", userVote.getId());
            response.put("txHash", userVote.getTxHash());
            response.put("votingPower", userVote.getVotingPower());
            response.put("timestamp", userVote.getVotedAt());
            
            log.info("Paymaster voting test successful: voteId={}, txHash={}", 
                    userVote.getId(), userVote.getTxHash());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Paymaster voting test failed: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Paymaster voting test failed");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint for paymaster service
     * Verifies that the zkSync Era paymaster service is available and working
     */
    @GetMapping("/paymaster-health")
    public ResponseEntity<Map<String, Object>> checkPaymasterHealth() {
        log.info("Checking paymaster service health...");
        
        try {
            // Get paymaster service info
            String paymasterInfo = votingService.getPaymasterServiceInfo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "healthy");
            response.put("timestamp", LocalDateTime.now());
            response.put("paymasterInfo", paymasterInfo);
            
            log.info("Paymaster service health check successful");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Paymaster service health check failed: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "unhealthy");
            response.put("timestamp", LocalDateTime.now());
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * 투표 확인 응답 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class VoteCheckResponse {
        private boolean hasVoted;
        private boolean canVote;
    }

    /**
     * 투표 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class VoteStats {
        private long totalVoters;
        private long forVoters;
        private long againstVoters;
    }

    /**
     * 사용자 투표 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class UserVoteStats {
        private long totalVotes;
        private long forVotes;
        private long againstVotes;
        private String summary;
    }
}