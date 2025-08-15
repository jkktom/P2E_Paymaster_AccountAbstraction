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
- JWT token generation with smart wallet address integration
- Spring Data JPA with H2 (dev) / PostgreSQL (prod)
- **ZkSyncService**: Automatic smart wallet creation during OAuth signup
- **ZkSyncController**: RESTful APIs for gasless transactions
- **Operational Point System**: Working main/sub points with real-time navbar display

**Smart Contracts (Solidity - zkSync Era):**
- **GovernanceToken**: `0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e` (DEPLOYED)
- **GovernancePaymaster**: `0x10219E515c3955916d79A1aC614B86187f0872BC` (DEPLOYED)
- ERC20 governance token with OpenZeppelin voting extensions
- Native Account Abstraction compatible contracts
- zkSync Era Sepolia testnet (fully operational)

**Frontend (Next.js/TypeScript):**
- NextAuth.js for Google OAuth integration
- React components with Tailwind CSS
- Alchemy Account Kit for embedded wallet experience
- Axios for Spring API communication
- No MetaMask required - native smart wallet integration

**Account Abstraction (zkSync Era + Custom Implementation):**
- **FULLY OPERATIONAL zkSync Account Abstraction system**
- Native smart wallet creation during Google OAuth signup
- Custom GovernancePaymaster for gasless governance transactions
- zkSync Era Sepolia deployment with 0.005 ETH funding
- Gasless voting, proposal creation, and delegation

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
  pointsToToken, // total exchanged to governance tokens
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

**Authentication Flow (OPERATIONAL):**
1. User clicks "Sign in with Google" on frontend
2. NextAuth.js handles OAuth flow with Google
3. Spring backend receives Google user info and creates User record
4. **ZkSyncService automatically creates zkSync smart wallet** for user
5. **JWT token includes smart wallet address** for subsequent API calls
6. User immediately ready for point earning and **gasless governance transactions**

**Token Exchange Flow:**
1. User spends main points (10:1 ratio)
2. Spring backend validates and initiates AA transaction
3. Alchemy sponsors gas for token minting/transfer
4. Governance tokens appear in user's smart wallet
5. Transaction recorded in both systems

## ✅ CURRENT IMPLEMENTATION STATUS

**FULLY OPERATIONAL Account Abstraction System:**
- ✅ **Smart Wallet Creation**: Automatic during Google OAuth signup
- ✅ **Gasless Transactions**: Fully tested governance functions (vote, createProposal, delegate)
- ✅ **Point System**: Working main/sub points with conversion and navbar display
- ✅ **JWT Integration**: Smart wallet addresses included in authentication tokens
- ✅ **zkSync Integration**: Native zkSync Era Sepolia deployment

## Technical Implementation Details

**zkSync Account Abstraction Integration:**
- **ZkSyncService**: Smart wallet creation with secure private key generation
- **GovernancePaymaster**: Custom paymaster contract for gasless governance transactions
- **zkSync Era RPC**: Direct zkSync Era Sepolia blockchain connectivity
- **Paymaster Funding**: 0.005 ETH available for transaction sponsorship

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
- **[Account Abstraction Implementation](./claudemds/CLAUDE-ACCOUNT-ABSTRACTION.md)** - ✅ FULLY OPERATIONAL zkSync AA system documentation
- **[API Documentation](./claudemds/CLAUDE-API.md)** - Current implementation & planned expansion of REST API endpoints
- **[OAuth2 Authentication](./claudemds/CLAUDE-OAUTH2.md)** - Google OAuth2 + JWT authentication architecture analysis
- **[Backend Architecture](./claudemds/CLAUDE-BACKEND.md)** - ✅ OPERATIONAL Spring Boot backend with zkSync integration
- **[Database Design](./claudemds/CLAUDE-DATABASE.md)** - Database architecture & performance-optimized data patterns
- **[Smart Contract Architecture](./claudemds/CLAUDE-CONTRACTS.md)** - ✅ DEPLOYED zkSync contracts & governance system
- **[System Architecture](./claudemds/CLAUDE-ARCHITECTURE.md)** - ✅ OPERATIONAL hybrid backend + blockchain architecture
- **[Frontend Architecture](./claudemds/CLAUDE-FRONTEND.md)** - Frontend application structure and components
- **[Testing Strategy](./claudemds/CLAUDE-TESTING.md)** - Comprehensive testing strategy and approaches
- **[Deployment Guide](./claudemds/CLAUDE-DEPLOYMENT.md)** - Jenkins CI/CD deployment on single AWS Ubuntu instance
- **[Setup Guide](./claudemds/CLAUDE-SETUP.md)** - Development environment setup guide

## Development Requirements

- **Target Network**: zkSync Era Sepolia (fully deployed and operational)
- **Execution Ready**: All code must be runnable for evaluation
- **Comprehensive Testing**: Unit, integration, and E2E test coverage
- **Documentation**: Clear technology choice justifications
- **Submission**: Pull Request format with detailed README