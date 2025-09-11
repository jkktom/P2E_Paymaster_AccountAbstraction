# Blooming Blockchain Service

A hybrid Web2/Web3 system for point-to-token exchange and governance voting, built on zkSync Era with Account Abstraction (gasless via paymaster).

## Overview
Connects a traditional Web2 point system with a Web3 governance token. Users earn points, convert them, exchange for governance tokens (BLOOM), and participate in proposals and voting.

## Key Features
- **Dual point system**: Sub Points → Main Points at a 10:1 ratio
- **Token exchange**: Main Points → BLOOM governance token (10:1)
- **Governance**: Create proposals and vote (For/Against)
- **Google OAuth**: Simple login; AA smart wallet shown in UI
- **Gasless UX**: Paymaster-backed transactions explained in UI

## Tech Stack

### Backend
- Spring Boot 3.5.x — REST API server
- Spring Security + OAuth2 — Google auth
- Spring Data JPA — Database ORM
- H2 (dev) / PostgreSQL (prod-ready)
- Web3j — Ethereum blockchain interaction

### Frontend
- Next.js 15 (App Router) + TypeScript + Tailwind CSS
- Google OAuth via backend redirects
- English-only UI, `en-US` date formatting

### Blockchain
- zkSync Era Sepolia (testnet)
- Solidity smart contracts
- Hardhat / Foundry (as needed) for development and deployment

## Deployed Smart Contracts

| Contract        | Address                                      | Network            |
|-----------------|----------------------------------------------|--------------------|
| GovernanceToken | `0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e` | zkSync Era Sepolia |
| Paymaster       | `0x10219E515c3955916d79A1aC614B86187f0872BC` | zkSync Era Sepolia |

## Getting Started

### Prerequisites
- Node.js 18+
- Java 21+
- Git

### 1) Clone the repository
```bash
git clone <repository-url>
cd P2E_Paymaster_AccountAbstraction
```

### 2) Run the Backend
```bash
cd spring-backend
./gradlew bootRun
```
- Backend runs at `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

### 3) Run the Frontend
```bash
cd ../next-frontend
npm install
npm run dev
```
- Frontend runs at `http://localhost:3000`

### 4) Try the App
1. Open `http://localhost:3000`
2. Click Google Login
3. Earn points, create a proposal, and vote

## Point System

### Conversion Ratios
- Sub Points → Main Points: 10:1
- Main Points → BLOOM governance token: 10:1

### Flow
1. User earns Sub Points (demo actions)
2. Convert Sub → Main
3. Exchange Main → BLOOM token
4. Vote on proposals using tokens

## Governance

### Create Proposals
- Any authenticated user can create a proposal
- Proposal metadata is stored and surfaced via the UI

### Voting
- For/Against votes
- Voting stats displayed per proposal

## Test Scenarios (Suggested)

### 1) User Registration & Auth
1. Sign in with Google
2. Confirm AA wallet address is displayed in UI
3. Verify user info and initial balances

### 2) Point System
1. Earn Sub Points (demo buttons)
2. Convert Sub → Main (10:1)
3. Exchange Main → BLOOM (10:1)
4. Check point history and balances

### 3) Governance
1. Create a new proposal
2. View active proposals
3. Vote For/Against
4. Review vote results and stats

## API (Selected Endpoints)

### Auth
- `GET /api/auth/user` — Current user
- OAuth login is handled via backend redirect (see frontend button)

### Points
- `GET /api/points/main` — Main point account
- `GET /api/points/sub` — Sub point account
- `POST /api/points/convert` — Convert Sub → Main
- `POST /api/points/exchange` — Exchange Main → Token

### Governance
- `GET /api/proposals` — List proposals
- `POST /api/proposals` — Create proposal
- `POST /api/votes/vote` — Vote on proposal
- `GET /api/votes/stats/proposal/{id}` — Vote stats for proposal

## Technical Notes

### Blockchain Integration
- Governance token deployed on zkSync Era Sepolia
- Hybrid architecture: Web2 database + Web3 integration as needed
- Account Abstraction: gasless UX explained via paymaster in UI

### Security & Auth
- OAuth2 + JWT
- CORS configured for frontend-backend communication
- Input validation on all APIs

### Scalability & Testing
- Modular services by domain (auth, points, proposals, votes)
- Strong typing via TypeScript (frontend) and Java (backend)
- Integration tests for critical flows

## Documentation
Additional docs are available under the `Documentation/` directory in the repository.
