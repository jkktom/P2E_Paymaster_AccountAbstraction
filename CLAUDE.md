# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a blockchain technical interview submission for developing a goods exchange service with the following components:
- Web2-based point system with main/sub points architecture
- Ethereum ERC20 governance token with voting functionality
- One-way exchange: points → tokens (10:1 ratio)
- Account Abstraction integration using Alchemy for gasless transactions
- Web application for testing all functionality
- Dual authentication system: Web2 login + Account Abstraction smart wallets

## Repository Structure

```
├── backend/          # Spring Boot API server
├── contracts/        # Hardhat + Solidity smart contracts
├── frontend/         # Next.js TypeScript application
└── docs/            # Documentation and deployment guides
```

## Tech Stack & Architecture

**Backend (Java Spring Boot):**
- Spring Security + OAuth2 Client for Google authentication
- JWT token generation for session management
- Spring Data JPA with H2 (dev) / PostgreSQL (prod)
- Web3j for blockchain interaction
- Alchemy SDK integration for Account Abstraction
- RESTful APIs for point management and token exchange

**Smart Contracts (Solidity):**
- ERC20 governance token with OpenZeppelin voting extensions
- Account Abstraction compatible contracts
- Hardhat for development, testing, and deployment
- Sepolia testnet for development (mainnet-ready configuration)

**Frontend (Next.js/TypeScript):**
- NextAuth.js for Google OAuth integration
- React components with Tailwind CSS
- Alchemy Account Kit for embedded wallet experience
- Axios for Spring API communication
- No MetaMask required - native smart wallet integration

**Account Abstraction (Alchemy):**
- Alchemy Account Kit for smart contract wallet creation
- Alchemy Gas Manager for sponsored transactions
- Alchemy RPC for reliable blockchain interaction
- Gasless token exchanges and governance voting

## Data Models & Architecture

**User Management:**
```
User: {
  id, googleId, email, name, avatar,
  smartWalletAddress, // Alchemy AA wallet
  createdAt
}

MainPointAccount: {
  id, userId, balance, totalEarned,
  pointsToToken, // total converted to governance tokens
  createdAt, updatedAt
}

SubPointAccount: {
  id, userId, balance, totalEarned,
  subToMain, // total converted to main points
  createdAt, updatedAt
}

PointEarnTransaction: {
  id, userId, pointType, // 'MAIN' or 'SUB'
  amount, source, // source of points (e.g., 'TASK_COMPLETION', 'EVENT_REWARD', 'ADMIN_GRANT')
  description, // details about how points were earned
  createdAt
}

ExchangeTransaction: {
  userId, pointsSpent, tokensReceived,
  txHash, status, createdAt
}

ConversionTransaction: {
  userId, subPointsSpent, mainPointsReceived,
  conversionRate, createdAt
}

WalletTransaction: {
  userId, type, amount, gasSponsored,
  txHash, blockNumber, createdAt
}
```

## Key Features

**Enhanced Point System:**
- **Main Points**: Exchangeable with governance tokens (10:1 ratio)
- **Sub Points**: Earned from events, tasks, achievements
- **Conversion System**: Sub points → Main points (configurable rate)
- **Future-Ready**: Extensible for gamification and reward systems

**Account Abstraction Benefits:**
- **Gasless Transactions**: Users exchange points without ETH
- **Embedded Wallets**: No external wallet installation required
- **Seamless UX**: Web2-like experience with Web3 benefits
- **Enterprise Ready**: Simplified onboarding for non-crypto users

**Authentication Flow:**
1. User clicks "Sign in with Google" on frontend
2. NextAuth.js handles OAuth flow with Google
3. Spring backend receives Google user info and creates User record
4. Spring automatically creates Alchemy smart wallet for user
5. JWT token returned for subsequent API calls
6. User immediately ready for point earning and token exchange

**Token Exchange Flow:**
1. User spends main points (10:1 ratio)
2. Spring backend validates and initiates AA transaction
3. Alchemy sponsors gas for token minting/transfer
4. Governance tokens appear in user's smart wallet
5. Transaction recorded in both systems

## Technical Implementation Details

**Alchemy Integration:**
- Account Kit for wallet infrastructure
- Gas Manager policies for sponsored transactions
- RPC endpoints for reliable blockchain connectivity
- Smart wallet factory for user wallet creation

**Security Considerations:**
- Google OAuth 2.0 for secure authentication
- JWT tokens for stateless API authentication
- Smart contract wallet ownership validation
- Rate limiting on point exchanges and conversions
- Transaction monitoring and comprehensive audit logging

**Testing Strategy:**
- Spring Boot unit tests for business logic
- Hardhat tests for smart contracts
- Integration tests for AA wallet operations
- End-to-end tests for complete user flows

## Detailed Documentation

For comprehensive technical details, see:
- **[CLAUDE-API.md](./CLAUDE-API.md)** - Complete API endpoint specifications
- **[CLAUDE-FRONTEND.md](./CLAUDE-FRONTEND.md)** - Frontend application structure and components
- **[CLAUDE-ARCHITECTURE.md](./CLAUDE-ARCHITECTURE.md)** - Technical decisions and architecture analysis

## Development Requirements

- **Target Network**: Ethereum Sepolia (mainnet-ready configuration)
- **Execution Ready**: All code must be runnable for evaluation
- **Comprehensive Testing**: Unit, integration, and E2E test coverage
- **Documentation**: Clear technology choice justifications
- **Submission**: Pull Request format with detailed README