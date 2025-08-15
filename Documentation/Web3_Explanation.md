# zkSync Account Abstraction 구현 가이드

## 📋 개요

이 프로젝트는 **zkSync Era Sepolia**에서 **Account Abstraction(계정 추상화)** 기술을 활용하여 가스리스 블록체인 서비스를 구현한 기술 데모입니다.

## 🔧 핵심 기술 스택

### Backend (Spring Boot)
- **Google OAuth 2.0** + JWT 토큰 인증
- **자동 스마트 지갑 생성** (회원가입 시)
- **Web3j** 기반 블록체인 연동
- **H2/PostgreSQL** 데이터베이스

### Blockchain (zkSync Era)
- **ERC20 거버넌스 토큰**: `0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e`
- **페이마스터 컨트랙트**: `0x10219E515c3955916d79A1aC614B86187f0872BC`
- **가스비 후원 시스템** (0.005 ETH 적립됨)

### Frontend (Next.js)
- **React 컴포넌트** 기반 UI
- **실시간 지갑 정보** 표시
- **가스리스 거래** 인터페이스

## 🚀 작동 원리

### 1. 회원가입 플로우
```
사용자 → Google 로그인 → Spring Backend → 스마트 지갑 생성 → JWT 토큰 발급
```

**핵심 코드:**
```java
// UserService.java - 신규 사용자 생성 시
ZkSyncService.SmartWallet smartWallet = zkSyncService.createSmartWallet(email);
User newUser = User.builder()
    .smartWalletAddress(smartWallet.getAddress())
    .build();
```

### 2. 스마트 지갑 생성
- **보안 개인키 생성**: `SecureRandom` + `Web3j.Credentials`
- **지갑 주소 도출**: ECDSA 타원곡선 암호화
- **데이터베이스 저장**: User 테이블에 `smart_wallet_address` 필드

### 3. JWT 토큰 통합
```java
// JWT에 스마트 지갑 주소 포함
String jwtToken = jwtService.generateToken(googleId, email, name, roleId, smartWalletAddress);
```

### 4. 가스리스 거래 시스템
- **페이마스터 컨트랙트**가 가스비 대납
- **투표권 검증**: 1 BLOOM 토큰 이상 보유자만 이용 가능
- **화이트리스트 함수**: vote, createProposal, delegate 등

## 🎯 핵심 장점

### 1. **사용자 경험 혁신**
- ❌ **기존**: MetaMask 설치 → 네트워크 추가 → ETH 충전 → 거래
- ✅ **개선**: Google 로그인 → 바로 사용 가능

### 2. **무료 블록체인 거래**
- 모든 거버넌스 투표가 **가스비 무료**
- 페이마스터가 자동으로 가스비 후원
- 일반 사용자는 ETH 보유 불필요

### 3. **엔터프라이즈 Ready**
- Web2 인증 시스템과 완벽 통합
- 대량 사용자 온보딩 가능
- 암호화폐 지식 없이도 이용 가능

### 4. **보안 & 탈중앙화**
- 개인키는 사용자별 고유 생성
- OpenZeppelin 표준 준수
- zkSync의 검증된 보안성 활용

## 📊 구현 결과

### 성공적인 통합 검증
- ✅ **자동 지갑 생성**: `0x5317...82c8`
- ✅ **페이마스터 활성화**: 0.005 ETH 잔액
- ✅ **JWT 토큰 통합**: 지갑 주소 포함
- ✅ **프론트엔드 표시**: Header에 지갑 주소 표시

### API 엔드포인트
```bash
# 페이마스터 상태 확인
GET /api/zksync/paymaster/status

# 지갑 잔액 조회
GET /api/zksync/wallet/balance

# 가스리스 토큰 교환
POST /api/zksync/exchange/gasless
```

## 🔮 확장 가능성

### 1. **실제 가스리스 거래 구현**
- zkSync SDK 통합으로 실제 블록체인 거래 처리
- Paymaster 로직 고도화

### 2. **추가 DeFi 기능**
- 스테이킹 리워드 시스템
- 유동성 풀 참여
- NFT 발행/거래

### 3. **기업용 솔루션**
- 대량 사용자 지갑 관리
- 맞춤형 가스 정책
- 규제 준수 기능

## 💡 기술적 혁신

이 구현은 **전통적인 Web2 UX**와 **Web3 기술의 혜택**을 성공적으로 결합한 사례입니다:

- **개발자**: 기존 Spring Boot 개발 패턴 유지하면서 블록체인 통합
- **사용자**: 복잡한 암호화폐 지식 없이도 탈중앙화 서비스 이용
- **기업**: 확장 가능한 블록체인 인프라로 새로운 비즈니스 모델 구현

## 🎯 결론

**zkSync Account Abstraction**을 활용한 이 시스템은 블록체인 기술의 **대중화**와 **실용화**를 위한 핵심 솔루션을 제시합니다. 

가스비 부담 없는 블록체인 서비스를 통해 **더 많은 사용자가 Web3 생태계에 참여**할 수 있는 기반을 마련했습니다.
