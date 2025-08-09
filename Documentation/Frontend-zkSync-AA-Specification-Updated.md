# Governance Token Frontend Application: zkSync Native AA + Simple POC

## 애플리케이션 개요

### 핵심 목표 (POC 중심)
- **원클릭 온보딩**: Google 로그인 → 자동 지갑 생성 → 바로 사용 가능
- **간단한 포인트 시스템**: 버튼 클릭만으로 포인트 획득 및 변환
- **가스리스 거버넌스**: zkSync Native AA를 활용한 무료 투표 경험
- **직관적인 UI**: 복잡한 블록체인 개념 숨기고 간단한 버튼 중심

## 사용자 여정 (단순화된 POC)

### 1단계: 즉시 시작
```
Google 로그인 버튼 클릭 → 자동 지갑 생성 → 대시보드 이동
```

### 2단계: 포인트 획득 (테스트용)
```
"포인트 받기" 버튼들:
- [서브 포인트 +10 받기] 
- [서브 포인트 +50 받기]
- [메인 포인트 +5 받기]
```

### 3단계: 포인트 변환
```
서브 → 메인: [100 서브포인트를 10 메인포인트로 변환] 버튼
메인 → 토큰: [10 메인포인트를 1 거버넌스토큰으로 교환] 버튼
```

### 4단계: 거버넌스 참여
```
토큰 보유 → 자동 가스리스 → [찬성] [반대] 버튼만 클릭
```

## 간소화된 프론트엔드 구조

### Pages/Routes (최소한)
```
/                    # 랜딩 (Google 로그인)
/dashboard           # 메인 대시보드 (모든 기능 한 페이지)
/admin              # 관리자 테스트 패널
```

### 핵심 컴포넌트 (단순화)

#### 1. 원클릭 온보딩
```jsx
<GoogleLoginButton>
  "Google로 시작하기" 
  // → 자동 지갑 생성 → 바로 대시보드
</GoogleLoginButton>
```

#### 2. 포인트 획득 섹션 (테스트용)
```jsx
<PointEarningButtons>
  <button onClick={() => earnSubPoints(10)}>
    서브포인트 +10 받기 🎁
  </button>
  <button onClick={() => earnSubPoints(50)}>
    서브포인트 +50 받기 🎁
  </button>
  <button onClick={() => earnMainPoints(5)}>
    메인포인트 +5 받기 ⭐
  </button>
</PointEarningButtons>
```

#### 3. 포인트 변환 섹션
```jsx
<PointConversion>
  <div className="conversion-card">
    <h3>서브 → 메인 포인트</h3>
    <p>보유: {subPoints} 서브포인트</p>
    <button onClick={() => convertSubToMain(100)}>
      100 서브포인트 → 10 메인포인트로 변환
    </button>
  </div>
  
  <div className="conversion-card">
    <h3>메인포인트 → 거버넌스 토큰</h3>
    <p>보유: {mainPoints} 메인포인트</p>
    <button onClick={() => exchangeMainToToken(10)}>
      10 메인포인트 → 1 거버넌스토큰으로 교환 (가스리스)
    </button>
  </div>
</PointConversion>
```

#### 4. 간단한 거버넌스
```jsx
<SimpleGovernance>
  <h3>거버넌스 토큰: {tokenBalance}개</h3>
  {tokenBalance > 0 ? (
    <div>
      <p>✅ 가스리스 투표 가능!</p>
      <div className="proposals">
        {proposals.map(proposal => (
          <div key={proposal.id} className="proposal-card">
            <h4>{proposal.description}</h4>
            <div className="vote-buttons">
              <button 
                onClick={() => vote(proposal.id, true)}
                className="vote-for"
              >
                찬성 (무료) ⚡
              </button>
              <button 
                onClick={() => vote(proposal.id, false)}
                className="vote-against"  
              >
                반대 (무료) ⚡
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  ) : (
    <p>토큰을 획득하면 투표할 수 있습니다</p>
  )}
</SimpleGovernance>
```

## 최소 필수 디렉토리 구조

```
src/
├── app/
│   ├── layout.tsx              # AuthProvider + 간단한 레이아웃
│   ├── page.tsx                # 랜딩 페이지 (Google 로그인만)
│   ├── dashboard/
│   │   └── page.tsx            # 모든 기능이 있는 단일 대시보드
│   └── admin/
│       └── page.tsx            # 관리자 테스트 패널
├── components/
│   ├── auth/
│   │   ├── GoogleSignIn.tsx    # Google OAuth 버튼
│   │   └── AuthGuard.tsx       # 로그인 체크
│   ├── points/
│   │   ├── EarnPointsButtons.tsx    # 포인트 받기 버튼들
│   │   ├── PointBalance.tsx         # 포인트 잔액 표시
│   │   └── ConversionButtons.tsx    # 변환 버튼들
│   ├── governance/
│   │   ├── TokenBalance.tsx         # 토큰 잔액 표시  
│   │   ├── ProposalCard.tsx         # 제안 카드
│   │   └── VoteButtons.tsx          # 투표 버튼 (가스리스)
│   └── admin/
│       ├── PointGrant.tsx          # 포인트 지급
│       └── CreateProposal.tsx      # 테스트 제안 생성
├── hooks/
│   ├── useAuth.ts              # Google OAuth + 자동 지갑
│   ├── usePoints.ts            # 포인트 관리
│   ├── useTokens.ts            # 토큰 교환 및 조회
│   ├── useGovernance.ts        # 거버넌스 (가스리스)
│   └── usePaymaster.ts         # 페이마스터 상태
└── lib/
    ├── auth.ts                 # NextAuth 설정
    ├── api.ts                  # 백엔드 API 클라이언트
    ├── zksync.ts              # zkSync 설정
    └── paymaster.ts           # 페이마스터 유틸
```

## 핵심 훅 구현 (단순화)

### useAuth (자동 지갑 생성)
```typescript
const useAuth = () => {
  const { data: session, status } = useSession();
  const [walletAddress, setWalletAddress] = useState<string | null>(null);
  const [isWalletReady, setIsWalletReady] = useState(false);

  useEffect(() => {
    if (session?.user) {
      // 백엔드에서 자동으로 지갑 생성 및 반환
      createOrGetWallet(session.user.email).then(setWalletAddress);
    }
  }, [session]);

  return {
    user: session?.user,
    isAuthenticated: !!session,
    walletAddress,
    isWalletReady: !!walletAddress,
    signIn: () => signIn('google'),
    signOut
  };
};
```

### usePoints (간단한 포인트 관리)
```typescript
const usePoints = () => {
  const [subPoints, setSubPoints] = useState(0);
  const [mainPoints, setMainPoints] = useState(0);
  
  const earnSubPoints = async (amount: number) => {
    // 백엔드 API 호출
    await apiClient.post('/points/earn/sub', { amount });
    setSubPoints(prev => prev + amount);
  };
  
  const earnMainPoints = async (amount: number) => {
    await apiClient.post('/points/earn/main', { amount });
    setMainPoints(prev => prev + amount);
  };
  
  const convertSubToMain = async (subAmount: number) => {
    if (subAmount > subPoints) return;
    
    await apiClient.post('/points/convert', { 
      subPoints: subAmount 
    });
    
    setSubPoints(prev => prev - subAmount);
    setMainPoints(prev => prev + (subAmount / 10)); // 10:1 비율
  };
  
  return {
    subPoints,
    mainPoints,
    earnSubPoints,
    earnMainPoints,
    convertSubToMain
  };
};
```

### useTokens (가스리스 교환)
```typescript  
const useTokens = () => {
  const [tokenBalance, setTokenBalance] = useState(0);
  const { mainPoints } = usePoints();
  
  const exchangeMainToToken = async (mainAmount: number) => {
    if (mainAmount > mainPoints) return;
    
    // 백엔드에서 가스리스 토큰 발행
    await apiClient.post('/tokens/exchange', {
      mainPoints: mainAmount
    });
    
    // 토큰 발행은 paymaster가 처리 (사용자는 기다리기만)
    setTokenBalance(prev => prev + (mainAmount / 10)); // 10:1 비율
  };
  
  return {
    tokenBalance,
    exchangeMainToToken,
    canExchange: mainPoints >= 10
  };
};
```

### useGovernance (가스리스 투표)
```typescript
const useGovernance = () => {
  const [proposals, setProposals] = useState([]);
  const { tokenBalance } = useTokens();
  
  const vote = async (proposalId: number, support: boolean) => {
    if (tokenBalance === 0) return;
    
    // 백엔드에서 paymaster를 통한 가스리스 투표 처리
    await apiClient.post('/governance/vote', {
      proposalId,
      support
    });
    
    // UI 업데이트
    toast.success(`${support ? '찬성' : '반대'} 투표가 완료되었습니다! (가스비 무료)`);
  };
  
  return {
    proposals,
    canVote: tokenBalance > 0,
    vote
  };
};
```

## 대시보드 페이지 구성 (올인원)

```jsx
export default function Dashboard() {
  const { user, walletAddress, isWalletReady } = useAuth();
  const { subPoints, mainPoints, earnSubPoints, earnMainPoints, convertSubToMain } = usePoints();
  const { tokenBalance, exchangeMainToToken } = useTokens();
  const { proposals, canVote, vote } = useGovernance();

  if (!isWalletReady) {
    return <div>지갑 설정 중...</div>;
  }

  return (
    <div className="dashboard">
      <h1>안녕하세요, {user?.name}님!</h1>
      <p>지갑 주소: {walletAddress}</p>
      
      {/* 1. 포인트 받기 섹션 */}
      <section>
        <h2>포인트 받기 (테스트용)</h2>
        <button onClick={() => earnSubPoints(10)}>
          서브포인트 +10 받기 🎁
        </button>
        <button onClick={() => earnSubPoints(50)}>
          서브포인트 +50 받기 🎁  
        </button>
        <button onClick={() => earnMainPoints(5)}>
          메인포인트 +5 받기 ⭐
        </button>
      </section>

      {/* 2. 잔액 표시 */}
      <section>
        <h2>내 잔액</h2>
        <p>서브포인트: {subPoints}</p>
        <p>메인포인트: {mainPoints}</p>
        <p>거버넌스토큰: {tokenBalance}</p>
      </section>

      {/* 3. 변환 섹션 */}
      <section>
        <h2>포인트 변환</h2>
        <button 
          onClick={() => convertSubToMain(100)}
          disabled={subPoints < 100}
        >
          100 서브포인트 → 10 메인포인트로 변환
        </button>
        
        <button
          onClick={() => exchangeMainToToken(10)}
          disabled={mainPoints < 10}
        >
          10 메인포인트 → 1 거버넌스토큰으로 교환 (가스리스)
        </button>
      </section>

      {/* 4. 거버넌스 섹션 */}
      <section>
        <h2>거버넌스 투표</h2>
        {canVote ? (
          <div>
            <p>✅ 가스리스 투표 가능!</p>
            {proposals.map(proposal => (
              <div key={proposal.id} className="proposal">
                <h3>{proposal.description}</h3>
                <button onClick={() => vote(proposal.id, true)}>
                  찬성 (무료) ⚡
                </button>
                <button onClick={() => vote(proposal.id, false)}>
                  반대 (무료) ⚡
                </button>
              </div>
            ))}
          </div>
        ) : (
          <p>거버넌스 토큰을 획득하면 투표할 수 있습니다</p>
        )}
      </section>
    </div>
  );
}
```

## 환경 설정 (단순화)

```env
# Google OAuth
NEXTAUTH_URL=http://localhost:3000
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# 백엔드 API
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

# zkSync 계약 주소
NEXT_PUBLIC_GOVERNANCE_TOKEN_ADDRESS=0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e
NEXT_PUBLIC_PAYMASTER_ADDRESS=[배포 후 설정]
```

## 개발 우선순위

### Phase 1: 기본 플로우
1. ✅ Google 로그인 → 자동 지갑 생성
2. ✅ 대시보드 단일 페이지 구성
3. ✅ 포인트 받기 버튼들
4. ✅ 잔액 표시

### Phase 2: 변환 기능  
1. ✅ 서브 → 메인 포인트 변환
2. ✅ 메인포인트 → 토큰 교환 (가스리스)
3. ✅ 실시간 잔액 업데이트

### Phase 3: 거버넌스
1. ✅ 가스리스 투표 버튼
2. ✅ 투표 자격 체크
3. ✅ 간단한 제안 표시

### Phase 4: 관리자 패널 (테스트용)
1. ✅ 사용자에게 포인트 지급
2. ✅ 테스트 제안 생성
3. ✅ 시스템 상태 모니터링

이 단순화된 구조는 POC에 최적화되어 있으며, 복잡한 블록체인 개념을 숨기고 직관적인 버튼 클릭만으로 모든 기능을 사용할 수 있도록 설계되었습니다.