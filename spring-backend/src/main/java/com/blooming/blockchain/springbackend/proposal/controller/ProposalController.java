package com.blooming.blockchain.springbackend.proposal.controller;

import com.blooming.blockchain.springbackend.proposal.dto.CreateProposalRequest;
import com.blooming.blockchain.springbackend.proposal.dto.ProposalResponse;
import com.blooming.blockchain.springbackend.proposal.entity.Proposal;
import com.blooming.blockchain.springbackend.proposal.service.ProposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Proposal REST API Controller
 */
@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@Slf4j
public class ProposalController {

    private final ProposalService proposalService;

    /**
     * 새로운 제안 생성 (스마트 컨트랙트 통합)
     */
    @PostMapping
    public ResponseEntity<?> createProposal(@Valid @RequestBody CreateProposalRequest request, BindingResult bindingResult) {
        log.info("Creating proposal request received: {}", request);
        log.info("Request details - user={}, description length={}, deadline={}", 
                request.getProposerGoogleId(), 
                request.getDescription() != null ? request.getDescription().length() : "null",
                request.getDeadline());

        if (bindingResult.hasErrors()) {
            log.error("Validation errors in proposal creation request:");
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                log.error("Field: {}, Error: {}", error.getField(), error.getDefaultMessage());
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            Proposal proposal = proposalService.createProposalWithSmartContract(
                request.getDescription(),
                request.getProposerGoogleId(),
                request.getDeadline()
            );

            ProposalResponse response = ProposalResponse.fromEntity(proposal);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to create proposal: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Proposal creation failed: " + e.getMessage()));
        }
    }

    /**
     * 모든 제안 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<ProposalResponse>> getAllProposals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Proposal> proposals = proposalService.getAllProposals(pageable);
        Page<ProposalResponse> responses = proposals.map(ProposalResponse::fromEntity);

        return ResponseEntity.ok(responses);
    }

    /**
     * 제안 ID로 조회
     */
    @GetMapping("/{proposalId}")
    public ResponseEntity<ProposalResponse> getProposal(@PathVariable Integer proposalId) {
        return proposalService.getProposalById(proposalId)
                .map(proposal -> ResponseEntity.ok(ProposalResponse.fromEntity(proposal)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 활성 제안 조회
     */
    @GetMapping("/active")
    public ResponseEntity<List<ProposalResponse>> getActiveProposals() {
        List<Proposal> proposals = proposalService.getActiveProposals();
        List<ProposalResponse> responses = proposals.stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 사용자의 제안 조회
     */
    @GetMapping("/user/{userGoogleId}")
    public ResponseEntity<List<ProposalResponse>> getProposalsByUser(@PathVariable String userGoogleId) {
        List<Proposal> proposals = proposalService.getProposalsByUser(userGoogleId);
        List<ProposalResponse> responses = proposals.stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 제안 검색
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProposalResponse>> searchProposals(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Proposal> proposals = proposalService.searchProposals(keyword, pageable);
        Page<ProposalResponse> responses = proposals.map(ProposalResponse::fromEntity);

        return ResponseEntity.ok(responses);
    }

    /**
     * 제안 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ProposalStats> getProposalStats() {
        ProposalStats stats = ProposalStats.builder()
                .totalProposals(proposalService.getTotalProposalCount())
                .activeProposals(proposalService.getActiveProposalCount())
                .executedProposals(proposalService.getExecutedProposalCount())
                .canceledProposals(proposalService.getCanceledProposalCount())
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * 제안 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ProposalStats {
        private long totalProposals;
        private long activeProposals;
        private long executedProposals;
        private long canceledProposals;
    }
}