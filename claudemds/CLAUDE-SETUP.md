# Development Setup Guide

This file provides step-by-step setup instructions for the blockchain goods exchange service development environment.

## IntelliJ IDEA Spring Boot Project Setup

### 1. Create New Spring Boot Project

**File → New → Project → Spring Initializr**

**Project Settings:**
- **Generator**: Initializr Service URL: `https://start.spring.io`
- **Language**: Java
- **Type**: Gradle - Groovy
- **JVM**: 17 (or latest LTS)
- **Group**: `com.blockchain.exchange`
- **Artifact**: `blockchain-exchange-backend`
- **Name**: `blockchain-exchange-backend`
- **Description**: `Blockchain Goods Exchange Service Backend`
- **Package name**: `com.blockchain.exchange`
- **Packaging**: Jar
- **Java Version**: 17

### 2. Spring Boot Dependencies Selection

**Essential Dependencies:**
```
Spring Web
Spring Security
OAuth2 Client
Spring Data JPA
H2 Database (for development)
PostgreSQL Driver (for production)
Spring Boot Actuator
Spring Boot DevTools
Validation
```

**Additional Dependencies to Add Manually:**
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-devtools'
    
    // Database
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'org.postgresql:postgresql'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    implementation 'io.jsonwebtoken:jjwt-impl:0.12.3'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // Web3 Integration
    implementation 'org.web3j:core:4.10.0'
    implementation 'org.web3j:crypto:4.10.0'
    
    // HTTP Client for external APIs
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:junit-jupiter'
}
```

### 3. Project Structure Setup

**Create the following directory structure in IntelliJ:**
```
src/main/java/com/blockchain/exchange/
├── config/
│   ├── SecurityConfig.java
│   ├── Web3Config.java
│   └── AlchemyConfig.java
├── controller/
│   ├── AuthController.java
│   ├── PointController.java
│   ├── ExchangeController.java
│   └── AdminController.java
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── PointType.java
│   ├── MainPointAccount.java
│   └── SubPointAccount.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── PointAccountRepository.java
├── service/
│   ├── AuthService.java
│   ├── PointService.java
│   ├── AlchemyService.java
│   └── UserService.java
├── dto/
│   ├── request/
│   └── response/
├── constant/
│   └── DatabaseConstants.java
└── BlockchainExchangeBackendApplication.java

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── data.sql
```

### 4. Application Configuration

**application.yml:**
```yaml
spring:
  application:
    name: blockchain-exchange-backend
  
  profiles:
    active: dev
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:your-google-client-id}
            client-secret: ${GOOGLE_CLIENT_SECRET:your-google-client-secret}
            scope:
              - email
              - profile

server:
  port: 8080

logging:
  level:
    com.blockchain.exchange: DEBUG
    org.springframework.security: DEBUG

# Custom application properties
app:
  jwt:
    secret: ${JWT_SECRET:your-jwt-secret-key-min-256-bits-long}
    expiration: 86400 # 24 hours
  
  alchemy:
    api-key: ${ALCHEMY_API_KEY:your-alchemy-api-key}
    app-id: ${ALCHEMY_APP_ID:your-alchemy-app-id}
    network: sepolia
    gas-manager-policy-id: ${ALCHEMY_GAS_POLICY_ID:your-gas-policy-id}
  
  cors:
    allowed-origins: ${ALLOWED_ORIGINS:http://localhost:3000}
```

**application-dev.yml:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect

logging:
  level:
    web: DEBUG
```

**application-prod.yml:**
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/exchangedb}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    com.blockchain.exchange: INFO
```

## Next.js TypeScript Frontend Setup

### 1. Create Next.js Project

**Terminal Commands:**
```bash
# Navigate to project root
cd /path/to/your/project

# Create Next.js project with TypeScript
npx create-next-app@latest frontend --typescript --tailwind --eslint --app --src-dir --import-alias "@/*"

# Navigate to frontend directory
cd frontend
```

**Project Creation Options (if prompted):**
- ✅ TypeScript
- ✅ ESLint  
- ✅ Tailwind CSS
- ✅ `src/` directory
- ✅ App Router
- ✅ Import alias (@/*)
- ❌ Turbopack (for now)

### 2. Install Additional Dependencies

```bash
# Authentication
npm install next-auth@beta
npm install @auth/prisma-adapter  # if using Prisma later

# HTTP Client
npm install axios

# UI Components (optional but recommended)
npm install @headlessui/react @heroicons/react

# Form Handling
npm install react-hook-form @hookform/resolvers zod

# State Management (if needed)
npm install zustand

# Alchemy Account Abstraction
npm install @alchemy/aa-core @alchemy/aa-alchemy @alchemy/aa-accounts

# Web3 Utilities
npm install viem@2.x

# Development Dependencies
npm install -D @types/node @types/react @types/react-dom
```

### 3. Frontend Project Structure

**Create this structure in `frontend/src/`:**
```
src/
├── app/
│   ├── api/
│   │   └── auth/
│   │       └── [...nextauth]/
│   │           └── route.ts
│   ├── admin/
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── dashboard/
│   │   └── page.tsx
│   ├── exchange/
│   │   └── page.tsx
│   ├── globals.css
│   ├── layout.tsx
│   └── page.tsx
├── components/
│   ├── ui/
│   │   ├── Button.tsx
│   │   ├── Card.tsx
│   │   └── Modal.tsx
│   ├── auth/
│   │   ├── LoginButton.tsx
│   │   └── AuthProvider.tsx
│   ├── points/
│   │   ├── PointsCard.tsx
│   │   └── ConversionForm.tsx
│   ├── admin/
│   │   ├── UserList.tsx
│   │   └── GrantPointsForm.tsx
│   └── wallet/
│       ├── AlchemyWalletConnect.tsx
│       └── WalletInfo.tsx
├── hooks/
│   ├── useAuth.ts
│   ├── usePoints.ts
│   ├── useWallet.ts
│   └── useApi.ts
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── alchemy.ts
│   └── utils.ts
├── types/
│   ├── auth.ts
│   ├── points.ts
│   └── api.ts
└── constants/
    └── index.ts
```

### 4. Environment Configuration

**Frontend `.env.local`:**
```bash
# NextAuth Configuration
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your-nextauth-secret-key

# Google OAuth
GOOGLE_CLIENT_ID=your-google-client-id.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Backend API
NEXT_PUBLIC_API_URL=http://localhost:8080

# Alchemy Configuration
NEXT_PUBLIC_ALCHEMY_APP_ID=your-alchemy-app-id
NEXT_PUBLIC_ALCHEMY_API_KEY=your-alchemy-public-api-key
```

## Alchemy Account Abstraction Setup

### 1. Alchemy Dashboard Setup

**Step 1: Create Alchemy Account**
- Go to [alchemy.com](https://alchemy.com)
- Sign up / Sign in
- Create new app

**Step 2: App Configuration**
- **App Name**: `Blockchain Exchange`
- **Description**: `Technical interview blockchain exchange service`
- **Chain**: `Ethereum`
- **Network**: `Sepolia` (for development)

**Step 3: Get API Keys**
- Copy **API Key** (for backend)
- Copy **App ID** (for frontend)

### 2. Account Kit Setup

**Step 1: Enable Account Abstraction**
- In Alchemy Dashboard → Apps → Your App
- Navigate to "Account Abstraction" section
- Enable Account Kit
- Note the **Gas Manager Policy ID**

**Step 2: Gas Manager Configuration**
- Create Gas Manager Policy
- **Policy Name**: `Blockchain Exchange Gas Policy`
- **Spend Rules**: 
  - Max spend per user per month: `$10`
  - Max spend per transaction: `$1`
- **Allowed Methods**: All (for development)

### 3. Local Environment Setup

**Backend Environment Variables (.env):**
```bash
# Database
DATABASE_URL=jdbc:h2:mem:testdb

# Google OAuth
GOOGLE_CLIENT_ID=your-google-client-id.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret

# JWT
JWT_SECRET=your-256-bit-secret-key-for-jwt-signing

# Alchemy
ALCHEMY_API_KEY=your-alchemy-api-key
ALCHEMY_APP_ID=your-alchemy-app-id
ALCHEMY_GAS_POLICY_ID=your-gas-manager-policy-id
ALCHEMY_NETWORK=sepolia

# CORS
ALLOWED_ORIGINS=http://localhost:3000
```

**Production Environment Variables:**
```bash
# Database (PostgreSQL in Docker)
DATABASE_URL=jdbc:postgresql://postgres:5432/exchangedb
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-secure-database-password

# Same OAuth and Alchemy keys as local
# Updated origins for production
ALLOWED_ORIGINS=https://your-frontend-domain.vercel.app
```

### 4. Alchemy Integration Code Examples

**Backend Alchemy Service (Java):**
```java
@Service
public class AlchemyService {
    
    @Value("${app.alchemy.api-key}")
    private String alchemyApiKey;
    
    @Value("${app.alchemy.app-id}")
    private String alchemyAppId;
    
    public String createSmartWallet(String userEmail) {
        // Implementation for creating smart contract wallet
        // Return wallet address
    }
    
    public String sendSponsoredTransaction(String walletAddress, String toAddress, BigInteger amount) {
        // Implementation for sponsored transaction
        // Return transaction hash
    }
}
```

**Frontend Alchemy Setup (TypeScript):**
```typescript
// lib/alchemy.ts
import { AlchemyProvider } from '@alchemy/aa-alchemy'
import { sepolia } from 'viem/chains'

const alchemyProvider = new AlchemyProvider({
  apiKey: process.env.NEXT_PUBLIC_ALCHEMY_API_KEY!,
  chain: sepolia,
  opts: {
    feeEstimator: async () => ({
      maxFeePerGas: 0n, // Sponsored by Gas Manager
      maxPriorityFeePerGas: 0n,
    })
  }
})

export { alchemyProvider }
```

## Development Workflow

### 1. Initial Setup Commands

```bash
# Backend (in IntelliJ)
./gradlew bootRun

# Frontend
cd frontend && npm run dev

# Access points
- Backend: http://localhost:8080
- Frontend: http://localhost:3000
- H2 Console: http://localhost:8080/h2-console
```

### 2. IntelliJ Configuration

**Run Configuration:**
- **Main class**: `com.blockchain.exchange.BlockchainExchangeBackendApplication`
- **Environment variables**: Load from `.env` file
- **VM options**: `-Dspring.profiles.active=dev`
- **Working directory**: Project root

**Database Configuration:**
- Add H2 database connection in Database tool
- URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: `password`

### 3. Testing Setup

**Backend Testing:**
```bash
./gradlew test
```

**Frontend Testing:**
```bash
npm test
npm run test:e2e
```

### 4. Docker Development (Optional)

**docker-compose.dev.yml:**
```yaml
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: exchangedb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_dev_data:/var/lib/postgresql/data

volumes:
  postgres_dev_data:
```

## Quick Start Checklist

### Backend Setup:
- [ ] IntelliJ Spring Boot project created with correct dependencies
- [ ] Application properties configured with environment variables
- [ ] Database connection tested (H2 console accessible)
- [ ] Alchemy API keys added to environment
- [ ] Google OAuth credentials configured
- [ ] Application starts without errors

### Frontend Setup:
- [ ] Next.js project created with TypeScript and Tailwind
- [ ] Required dependencies installed
- [ ] Environment variables configured
- [ ] Alchemy Account Kit integrated
- [ ] API client configured for backend communication
- [ ] Application starts and renders

### Integration Setup:
- [ ] Backend API endpoints accessible from frontend
- [ ] Google OAuth flow working
- [ ] Alchemy smart wallet creation functional
- [ ] Database entities created and seeded with demo data
- [ ] Role-based access control working

This setup guide provides everything needed to get the development environment running for your blockchain goods exchange service.