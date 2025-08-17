package com.blooming.blockchain.springbackend.proposal.controller;

import com.blooming.blockchain.springbackend.proposal.dto.VoteRequest;
import com.blooming.blockchain.springbackend.proposal.dto.VoteResponse;
import com.blooming.blockchain.springbackend.proposal.entity.UserVote;
import com.blooming.blockchain.springbackend.proposal.service.VotingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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
     * 특정 제안의 모든 투표 조회
     */
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<List<VoteResponse>> getVotesByProposal(@PathVariable Long proposalId) {
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
    public ResponseEntity<List<VoteResponse>> getForVotesByProposal(@PathVariable Long proposalId) {
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
    public ResponseEntity<List<VoteResponse>> getAgainstVotesByProposal(@PathVariable Long proposalId) {
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
            @PathVariable Long proposalId, 
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
    public ResponseEntity<VoteStats> getVoteStats(@PathVariable Long proposalId) {
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