# 블록체인 거버넌스 토큰 교환 서비스

zkSync Era 기반의 포인트-토큰 교환 및 거버넌스 투표 시스템입니다.
EVM 계열이면서, Native Account Abstraction 을 활용하기에 적용해 보았습니다.

## - 프로젝트 개요

[ ]Web2 포인트 시스템과 Web3 거버넌스 토큰을 연결하는 하이브리드 서비스로, 사용자는 포인트를 통해 거버넌스 토큰을 획득하고 제안에 투표할 수 있습니다.

### 주요 기능
- **포인트 시스템 이원화**: Sub포인트 → Main 포인트 (10:1 비율)
- **토큰 교환**: Main 포인트 → 거버넌스 토큰 (BLOOM 토큰) (10:1 비율)  
- **거버넌스**: 제안 생성 및 투표 시스템
- **Google OAuth**: 간편 로그인 및 자동 지갑 생성.

##  기술 스택

### Backend
- **Spring Boot 3.5.4**: REST API 서버
- **Spring Security + OAuth2**: Google 인증
- **Spring Data JPA**: 데이터베이스 ORM
- **H2 Database**: 개발용 인메모리 데이터베이스
- **Web3j**: 이더리움 블록체인 상호작용

### Frontend  
- **Next.js 15**: React 기반 웹 애플리케이션
- **TypeScript**: 타입 안전성
- **Tailwind CSS**: UI 스타일링
- **NextAuth.js**: 인증 처리

### Blockchain
- **zkSync Era Sepolia**: 테스트넷 배포
- **Solidity**: 스마트 컨트랙트 개발
- **Hardhat**: 컨트랙트 개발 및 배포 도구

##  배포된 스마트 컨트랙트

| 컨트랙트 | 주소 | 네트워크 |
|---------|------|----------|
| GovernanceToken | `0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e` | zkSync Era Sepolia |
| Paymaster | `0x10219E515c3955916d79A1aC614B86187f0872BC` | zkSync Era Sepolia |

##  실행 방법

### 사전 요구사항
- **Node.js 18+**
- **Java 21+**
- **Git**

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd jkktom-blockchain-test
```

### 2. Backend 실행
```bash
cd spring-backend
./gradlew bootRun
```
- 서버가 `http://localhost:8080`에서 실행됩니다
- H2 콘솔: `http://localhost:8080/h2-console`

### 3. Frontend 설정 및 실행
```bash
cd next-frontend


# 의존성 설치 및 실행
npm install
npm run dev
```
- 프론트엔드가 `http://localhost:3000`에서 실행됩니다

### 4. 애플리케이션 접속
1. 브라우저에서 `http://localhost:3000` 접속
2. Google 로그인 클릭
3. 포인트 획득 및 제안 생성/투표 테스트

##  포인트 시스템

### 교환 비율
- **Sub점수 → Main점수**: 10:1 비율
- **Main점수 → 거버넌스 토큰**: 10:1 비율

### 포인트 흐름
1. 사용자가 Sub점수 획득 (이벤트, 작업 완료 등)
2. Sub점수를 Main점수로 변환
3. Main점수를 거버넌스 토큰으로 교환
4. 거버넌스 토큰으로 제안에 투표

## 🗳️ 거버넌스 시스템

### 제안 생성
- 모든 사용자가 제안 생성 가능
- 제안은 블록체인에 실제로 기록됨
- 48-72시간의 투표 기간 설정

### 투표 시스템
- **현재 상태**: 블록체인 연동 없이 Web2 데이터베이스에서 시뮬레이션
- 거버넌스 토큰 보유량에 따른 투표권 계산
- 찬성/반대 투표 및 실시간 집계

##  테스트 시나리오

### 1. 사용자 등록 및 인증
1. Google 로그인 수행
2. 자동 스마트 지갑 생성 확인
3. 사용자 정보 및 포인트 초기화 확인

### 2. 포인트 시스템 테스트
1. Sub점수 획득 (임의 생성)
2. Sub점수 → Main점수 변환 (10:1)
3. Main점수 → 거버넌스 토큰 교환 (10:1)
4. 포인트 히스토리 확인

### 3. 거버넌스 기능 테스트
1. 새로운 제안 생성
2. 제안 목록에서 활성 제안 확인
3. 제안에 대한 투표 수행
4. 투표 결과 및 통계 확인

## 📚 API 문서

### 주요 엔드포인트

#### 인증
- `POST /api/auth/login` - Google OAuth 로그인
- `GET /api/auth/user` - 현재 사용자 정보

#### 포인트
- `GET /api/points/main` - Main점수 조회
- `GET /api/points/sub` - Sub점수 조회
- `POST /api/points/convert` - 포인트 변환
- `POST /api/points/exchange` - 토큰 교환

#### 거버넌스
- `GET /api/proposals` - 제안 목록
- `POST /api/proposals` - 새 제안 생성
- `POST /api/votes` - 투표 수행
- `GET /api/votes/stats/proposal/{id}` - 투표 통계

## 기술적 특징

### 블록체인 통합
- **실제 스마트 컨트랙트**: zkSync Era에 배포된 거버넌스 토큰
- **하이브리드 아키텍처**: Web2 데이터베이스 + Web3 블록체인 연동
- **계정 추상화**: Google 로그인으로 스마트 지갑 자동 생성

### 보안 및 인증
- **OAuth2 + JWT**: 안전한 인증 시스템
- **CORS 설정**: 프론트엔드-백엔드 간 안전한 통신
- **입력 검증**: 모든 API 입력값 검증

### 확장 가능한 설계
- **모듈형 아키텍쳐**: 각 기능별 독립적인 서비스 분리
- **타입 안전성**: TypeScript와 Java 타입 시스템 활용
- **테스트 가능**: 단위 테스트 및 통합 테스트 구조

## root/Docuement 폴더에 추가 설명 문서 정리.

---
