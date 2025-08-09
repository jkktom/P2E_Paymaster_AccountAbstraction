# 스마트 컨트랙트 배포 가이드

## 배포 개요

**Blooming Blockchain Service** 거버넌스 토큰이 zkSync Sepolia 테스트넷에 성공적으로 배포되었습니다.

### 배포된 컨트랙트 정보

#### GovernanceToken
- **컨트랙트 이름**: Blooming Blockchain Service Token
- **토큰 심볼**: BLOOM
- **네트워크**: zkSync Sepolia Testnet
- **체인 ID**: 300
- **컨트랙트 주소**: `0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e`
- **배포 날짜**: 2024년 8월 9일

#### GovernancePaymaster (NEW!)
- **컨트랙트 이름**: GovernancePaymaster
- **네트워크**: zkSync Sepolia Testnet
- **체인 ID**: 300
- **컨트랙트 주소**: `0x10219E515c3955916d79A1aC614B86187f0872BC`
- **배포자**: `0x5a394A0bb24361da49cE1B10df99CDBDcF7Bb4c1`
- **트랜잭션 해시**: `0x0c17fd1ff136605a61c579e70c4baa084dafcdabd8f8cfa6cae61352e0955a92`
- **배포 날짜**: 2024년 8월 9일
- **연결된 거버넌스 토큰**: `0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e`

### 익스플로러 링크

**GovernanceToken:**
```
https://sepolia.explorer.zksync.io/address/0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e
```

**GovernancePaymaster:**
```
https://sepolia.explorer.zksync.io/address/0x10219E515c3955916d79A1aC614B86187f0872BC
```

## 컨트랙트 기능

### 1. 토큰 발행 기능
- `mintForExchange(address to, uint256 amount, string reason)` - 포인트 교환용 토큰 발행
- `batchMint(address[] recipients, uint256[] amounts, string reason)` - 다수 사용자 토큰 일괄 발행

### 2. 거버넌스 기능
- `createProposal(string description, uint256 deadline)` - 새로운 제안 생성
- `vote(uint256 proposalId, bool support)` - 제안에 투표
- `executeProposal(uint256 proposalId)` - 통과된 제안 실행
- `delegate(address delegatee)` - 투표권 위임

### 3. 관리자 기능
- `pause()` / `unpause()` - 컨트랙트 일시정지/재시작
- `cancelProposal(uint256 proposalId)` - 제안 취소

### 4. 조회 기능
- `getProposal(uint256 proposalId)` - 제안 상세 정보 조회
- `getVotingPower(address account)` - 투표권 조회
- `getVoteInfo(uint256 proposalId, address voter)` - 투표 정보 조회

## 배포 환경 설정

### 필요한 환경 변수
```bash
PRIVATE_KEY=your_metamask_private_key_without_0x
ALCHEMY_API_KEY=v03**********Z1GUby0X
```

### 배포 명령어
```bash
cd solidity-contract
npm run compile
npm run deploy:zksync
```

## 백엔드 통합을 위한 설정

### Spring Boot application.yml 설정
```yaml
app:
  contracts:
    governance-token:
      address: "0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e"
      network: "zkSync-sepolia"
      chain-id: 300
```

### 환경 변수 설정
```bash
GOVERNANCE_TOKEN_ADDRESS=0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e
ZKSYNC_SEPOLIA_RPC_URL=https://sepolia.era.zksync.dev
```

## 프론트엔드 통합을 위한 설정

### Next.js 환경 변수
```bash
NEXT_PUBLIC_GOVERNANCE_TOKEN_ADDRESS=0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e
NEXT_PUBLIC_ZKSYNC_CHAIN_ID=300
NEXT_PUBLIC_ZKSYNC_RPC_URL=https://sepolia.era.zksync.dev
```

## 컨트랙트 검증 및 테스트

### 배포 검증 사항
- ✅ 컨트랙트 정상 배포 완료
- ✅ 토큰 이름과 심볼 확인: "Blooming Blockchain Service Token" (BLOOM)
- ✅ 소유자 권한 확인 (배포자 주소)
- ✅ OpenZeppelin v5 호환성 확인
- ✅ ERC20Votes 투표 기능 활성화 확인

### 테스트 시나리오
1. **토큰 발행 테스트**: 관리자 계정으로 토큰 발행
2. **투표권 위임 테스트**: 사용자가 자신에게 투표권 위임
3. **제안 생성 테스트**: 최소 투표권을 가진 사용자가 제안 생성
4. **투표 테스트**: 제안에 찬성/반대 투표
5. **제안 실행 테스트**: 투표 기간 종료 후 제안 실행

## 보안 고려사항

### 접근 제어
- **onlyOwner**: 토큰 발행, 일시정지, 제안 취소
- **whenNotPaused**: 모든 주요 기능은 일시정지 시 차단
- **nonReentrant**: 재진입 공격 방지

### 투표 보안
- **최소 투표권**: 1 BLOOM (1e18 wei) 이상 보유 시 제안 생성 가능
- **중복 투표 방지**: 한 제안당 한 번만 투표 가능
- **투표 기간 검증**: 마감 기간 이후 투표 불가

## Account Abstraction 통합

zkSync Sepolia는 네이티브 Account Abstraction을 지원하므로:
- **가스리스 거래**: Alchemy Gas Manager 사용 가능
- **스마트 월렛**: 사용자별 스마트 컨트랙트 월렛 생성 가능
- **배치 거래**: 여러 거래를 하나로 묶어 처리 가능

## 향후 개발 계획

### 1단계: 백엔드 통합
- Spring Boot 서비스에서 컨트랙트 연동
- 포인트 교환 API 구현
- 사용자 투표권 관리 시스템

### 2단계: 프론트엔드 개발
- Next.js 기반 웹 애플리케이션
- 지갑 연결 및 토큰 조회 기능
- 거버넌스 참여 인터페이스

### 3단계: 메인넷 배포
- 이더리움 메인넷 또는 zkSync Era 메인넷 배포
- 보안 감사 수행
- 프로덕션 환경 최적화

## 문제 해결

### 일반적인 문제들

**1. 가스 부족 오류**
```bash
Error: insufficient funds for gas * price + value
```
**해결방법**: zkSync Sepolia 테스트 ETH 충전 필요

**2. 네트워크 연결 오류**
```bash
Error: network not supported
```
**해결방법**: Hardhat 설정에서 zkSync Sepolia 네트워크 확인

**3. 개인키 오류**
```bash
Error: invalid private key
```
**해결방법**: .env 파일의 PRIVATE_KEY 형식 확인 (0x 접두사 제거)


**주의사항**: 이 컨트랙트는 테스트넷에 배포되었으므로 실제 가치가 없습니다. 프로덕션 환경에서는 추가적인 보안 검토가 필요합니다.