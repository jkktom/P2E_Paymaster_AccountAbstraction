package com.blooming.blockchain.springbackend.proposal;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Proposal API 통합 테스트 실행을 위한 러너
 * 
 * 사용법:
 * 1. IDE에서 이 클래스의 main 메서드 실행
 * 2. 또는 명령줄에서: ./gradlew test --tests ProposalApiIntegrationTest
 * 3. 또는 전체 테스트: ./gradlew test
 * 
 * 주의사항:
 * - Spring Boot 애플리케이션이 실행 중이어야 합니다
 * - 환경변수에 올바른 private key가 설정되어 있어야 합니다
 * - zkSync Era Sepolia 네트워크에 접근 가능해야 합니다
 */
public class ProposalApiTestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Proposal API 통합 테스트 실행 ===");
        System.out.println("스마트 컨트랙트 통합 기능을 테스트합니다.");
        System.out.println("");
        
        // JUnit 5 테스트 실행
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClass(ProposalApiIntegrationTest.class))
            .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        
        TestExecutionSummary summary = listener.getSummary();
        
        System.out.println("\\n=== 테스트 실행 결과 ===");
        System.out.printf("전체 테스트: %d개\\n", summary.getTestsFoundCount());
        System.out.printf("성공: %d개\\n", summary.getTestsSucceededCount());
        System.out.printf("실패: %d개\\n", summary.getTestsFailedCount());
        System.out.printf("건너뜀: %d개\\n", summary.getTestsSkippedCount());
        
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\\n실패한 테스트:");
            summary.getFailures().forEach(failure -> {
                System.out.printf("- %s: %s\\n", 
                    failure.getTestIdentifier().getDisplayName(),
                    failure.getException().getMessage());
            });
        }
        
        System.out.println("\\n=== 테스트 완료 ===");
        System.out.println("상세한 로그는 애플리케이션 콘솔에서 확인하세요.");
        System.out.println("스마트 컨트랙트 트랜잭션 정보도 포함되어 있습니다.");
    }
}