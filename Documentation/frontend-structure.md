# Governance Token Frontend Application Documentation

이 문서는 거버넌스 토큰 시스템을 위한 Next.js TypeScript 애플리케이션의 상세 프론트엔드 사양을 담고 있습니다.

## 애플리케이션 개요

### 핵심 목표
- **가스리스 거버넌스**: zkSync Native AA를 활용한 무료 투표 경험
- **간편한 온보딩**: Google OAuth를 통한 원클릭 회원가입 및 자동 지갑 생성
- **포인트-토큰 교환**: 기존 포인트 시스템과 거버넌스 토큰 간의 원활한 연결
- **투명한 거버넌스**: 모든 제안과 투표 과정의 투명한 공개

## 애플리케이션 구조

### Pages/Routes
```
/                    # 랜딩 페이지 (Google 로그인)
/dashboard           # 메인 대시보드
/governance          # 거버넌스 메인 페이지
/governance/proposals # 제안 목록
/governance/proposal/[id] # 제안 상세 페이지
/governance/create   # 새 제안 생성
/governance/history  # 투표 히스토리
/tokens              # 토큰 관리 (교환, 위임)
/tokens/exchange     # 포인트-토큰 교환
/tokens/delegate     # 투표권 위임
/profile             # 사용자 프로필
/admin               # 관리자 패널 (테스트용)
```

### 핵심 컴포넌트

#### AuthProvider
- NextAuth.js와 Google OAuth 통합
- JWT 토큰 관리 및 자동 갱신
- 사용자 세션 상태 관리
- 자동 지갑 생성 플로우

#### WalletProvider
- zkSync 지갑 연결 및 상태 관리
- 토큰 잔액 실시간 업데이트
- 가스리스 트랜잭션 처리
- 트랜잭션 상태 추적

#### GovernanceProvider
- 제안 데이터 관리
- 투표 상태 실시간 업데이트
- 투표권 계산 및 표시
- 제안 생성/투표 플로우

#### ProposalCard
- 제안 요약 정보 표시
- 투표 진행 상황 시각화
- 투표 마감 시간 카운트다운
- 빠른 투표 액션 버튼

#### VotingInterface
- 찬성/반대 투표 버튼
- 투표권 수량 표시
- 가스리스 투표 안내
- 투표 확인 모달

#### ProposalCreator
- 제안 작성 폼
- 마감시간 설정 인터페이스
- 제안 미리보기
- 최소 투표권 검증

#### TokenExchange
- 포인트-토큰 교환 인터페이스
- 실시간 교환 비율 표시
- 트랜잭션 확인 모달
- 가스비 대납 안내

#### VotingPowerDisplay
- 현재 투표권 표시
- 위임된 투표권 정보
- 위임 관리 인터페이스
- 투표권 히스토리

#### TransactionHistory
- 탭 인터페이스 (토큰/투표/제안)
- 무한 스크롤 지원
- 검색 및 필터 기능
- 트랜잭션 상세 정보

### 커스텀 훅

#### useAuth()
```typescript
const {
  user,           // 현재 사용자 객체 (Google 정보 + 지갑 주소)
  isLoading,      // 인증 로딩 상태
  signIn,         // Google 로그인 함수
  signOut,        // 로그아웃 함수
  walletAddress,  // 자동 생성된 지갑 주소
  isWalletReady   // 지갑 준비 상태
} = useAuth();
```

#### useGovernance()
```typescript
const {
  proposals,      // 제안 목록
  activeProposals, // 진행 중인 제안들
  votingPower,    // 현재 투표권
  loading,        // 로딩 상태
  createProposal, // 제안 생성 함수
  vote,           // 투표 함수 (가스리스)
  getProposal,    // 특정 제안 조회
  refresh         // 데이터 새로고침
} = useGovernance();
```

#### useTokens()
```typescript
const {
  balance,        // 토큰 잔액
  pointBalance,   // 포인트 잔액
  exchangeRate,   // 교환 비율
  loading,        // 로딩 상태
  exchange,       // 포인트-토큰 교환 함수
  delegate,       // 투표권 위임 함수
  history,        // 토큰 거래 히스토리
  refresh         // 잔액 새로고침
} = useTokens();
```

#### useVoting()
```typescript
const {
  votingHistory,  // 투표 히스토리
  hasVoted,       // 특정 제안 투표 여부 확인
  voteWeight,     // 투표 시점의 권한 수
  delegatedTo,    // 위임 대상
  loading,        // 투표 처리 중 상태
  castVote,       // 가스리스 투표 실행
  checkEligibility // 투표 자격 확인
} = useVoting();
```

#### usePaymaster()
```typescript
const {
  isEnabled,      // Paymaster 활성화 상태
  balance,        // Paymaster 잔액
  estimateGas,    // 가스 추정 (항상 0)
  sponsorTransaction, // 가스 대납 트랜잭션
  transactionStatus   // 트랜잭션 상태
} = usePaymaster();
```

## UI/UX 디자인 원칙

### 디자인 시스템
- **컬러**: 신뢰감을 주는 블루/그린 팔레트
- **타이포그래피**: 읽기 쉬운 폰트 (Inter/Poppins)
- **레이아웃**: 카드 기반 디자인, 명확한 계층 구조
- **반응형**: 모바일 우선 접근법

### 사용자 경험 플로우
1. **랜딩**: 깔끔한 Google 로그인 버튼
2. **온보딩**: 거버넌스 시스템 간단 설명
3. **대시보드**: 모든 잔액과 액션의 개요
4. **거버넌스**: 직관적인 제안 탐색 및 투표
5. **토큰 관리**: 명확한 교환 및 위임 프로세스

### 가스리스 UX 강조
- **투표 버튼**: "무료 투표" 배지 표시
- **트랜잭션 안내**: "가스비 없음" 명시
- **로딩 상태**: "백엔드에서 처리 중..." 메시지
- **완료 알림**: "가스리스 투표 완료!" 확인

## 상태 관리 전략

### React Context + Zustand
- 경량 상태 관리
- 타입 안전한 상태 접근
- 효율적인 리렌더링 최적화
- 테스트 및 디버깅 용이성

### 데이터 페칭
- TanStack Query (React Query)를 사용한 효율적인 API 데이터 페칭
- 자동 백그라운드 재검증
- 낙관적 UI 업데이트
- 에러 바운더리 구현

## 성능 최적화

### 코드 분할
- 라우트 기반 분할로 빠른 초기 로드
- 거버넌스 기능의 지연 로딩
- 관리자 기능의 동적 임포트

### 캐싱 전략
- React Query를 통한 API 응답 캐싱
- Next.js 이미지 최적화
- 랜딩 페이지의 정적 생성
- 오프라인 기능을 위한 서비스 워커

## 보안 고려사항

### 클라이언트 사이드 보안
- 민감한 정보 로컬 저장 금지
- JWT 토큰 안전한 처리
- XSS 방지를 위한 입력 검증
- CSRF 토큰 검증

### 지갑 보안
- Private Key는 백엔드에서만 관리
- 트랜잭션 서명은 백엔드에서 처리
- 사용자는 의도만 전달 (투표 의사)

## 테스트 전략

### 컴포넌트 테스팅
- Jest + React Testing Library
- 컴포넌트 동작 검증
- 사용자 상호작용 시뮬레이션
- 접근성 테스팅

### 통합 테스팅
- API 통합 목 처리
- 인증 플로우 테스팅
- 거버넌스 플로우 검증

### E2E 테스팅
- Playwright를 사용한 크로스 브라우저 테스팅
- 핵심 사용자 경로 검증
- 모바일 반응형 테스팅

## 배포 및 모니터링

### 배포 전략
- Vercel을 통한 자동 배포
- 환경별 설정 관리
