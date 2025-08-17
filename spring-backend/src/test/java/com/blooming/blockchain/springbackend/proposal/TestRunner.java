package com.blooming.blockchain.springbackend.proposal;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * 프로포절 투표 테스트 실행을 위한 간단한 러너
 * 
 * 사용법:
 * 1. IDE에서 이 클래스의 main 메서드 실행
 * 2. 또는 명령줄에서: ./gradlew test --tests ProposalVotingIntegrationTest
 * 3. 또는 전체 테스트: ./gradlew test
 */
public class TestRunner {
    
    public static void main(String[] args) {
        // JUnit 5 테스트 실행
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClass(ProposalVotingIntegrationTest.class))
            .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        
        TestExecutionSummary summary = listener.getSummary();
        
        System.out.println("\n=== 테스트 실행 결과 ===");
        System.out.printf("전체 테스트: %d개\n", summary.getTestsFoundCount());
        System.out.printf("성공: %d개\n", summary.getTestsSucceededCount());
        System.out.printf("실패: %d개\n", summary.getTestsFailedCount());
        System.out.printf("건너뜀: %d개\n", summary.getTestsSkippedCount());
        
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n실패한 테스트:");
            summary.getFailures().forEach(failure -> {
                System.out.printf("- %s: %s\n", 
                    failure.getTestIdentifier().getDisplayName(),
                    failure.getException().getMessage());
            });
        }
        
        System.out.println("\n=== 테스트 완료 ===");
    }
}