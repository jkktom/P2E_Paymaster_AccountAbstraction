# Testing Strategy

This file documents the comprehensive testing approach for the blockchain goods exchange service.

## Testing Pyramid Overview

### Unit Tests (Foundation)
- **Spring Boot**: Service layer business logic
- **Smart Contracts**: Solidity function testing with Hardhat
- **Frontend**: Component behavior with Jest/React Testing Library
- **Coverage Target**: 80%+ code coverage

### Integration Tests (Core)
- **API Integration**: Full request/response cycles
- **Database Integration**: JPA entity relationships and transactions
- **Alchemy Integration**: Account Abstraction wallet operations
- **Blockchain Integration**: Smart contract interactions via Web3j

### End-to-End Tests (Critical Paths)
- **User Journey**: Complete authentication to token exchange flow
- **Cross-Browser**: Chrome, Firefox, Safari compatibility
- **Mobile Responsive**: Mobile device user experience

## Backend Testing (Spring Boot)

### Unit Tests
```java
@SpringBootTest
class PointServiceTest {
    @Test
    void shouldEarnMainPoints() { /* ... */ }
    
    @Test  
    void shouldConvertSubToMainPoints() { /* ... */ }
    
    @Test
    void shouldExchangePointsForTokens() { /* ... */ }
}

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Test
    void shouldAuthenticateWithGoogle() { /* ... */ }
}
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
class PointIntegrationTest {
    @Test
    void shouldHandleCompletePointEarningFlow() { /* ... */ }
}

@DataJpaTest
class UserRepositoryTest {
    @Test
    void shouldFindUserByGoogleId() { /* ... */ }
}
```

### Mock Strategy
- **Alchemy SDK**: Mock wallet creation and transaction submission
- **Web3j**: Mock blockchain interactions for unit tests
- **Google OAuth**: Mock authentication responses
- **External APIs**: WireMock for service dependencies

## Smart Contract Testing (Hardhat)

### Unit Tests
```javascript
describe("GovernanceToken", function() {
  it("Should mint tokens correctly", async function() {
    // Test token minting functionality
  });
  
  it("Should support voting functionality", async function() {
    // Test governance voting features
  });
  
  it("Should prevent unauthorized access", async function() {
    // Test access control mechanisms
  });
});
```

### Integration Tests
```javascript
describe("Token Exchange Integration", function() {
  it("Should handle point-to-token exchange", async function() {
    // Test complete exchange workflow
  });
});
```

### Test Networks
- **Local**: Hardhat local blockchain for fast testing
- **Sepolia**: Integration testing on actual testnet
- **Fork Testing**: Mainnet fork for realistic environment testing

## Frontend Testing (Next.js)

### Component Unit Tests
```typescript
describe('PointsCard', () => {
  it('displays main and sub point balances', () => {
    // Test component rendering with mock data
  });
  
  it('handles conversion button clicks', () => {
    // Test user interactions
  });
});

describe('ExchangeForm', () => {
  it('validates point amounts', () => {
    // Test form validation logic
  });
  
  it('submits exchange requests', () => {
    // Test form submission
  });
});
```

### Custom Hook Tests
```typescript
describe('usePoints', () => {
  it('fetches point balances on mount', () => {
    // Test hook behavior
  });
  
  it('updates balances after transactions', () => {
    // Test state management
  });
});
```

### Integration Tests
```typescript
describe('Authentication Flow', () => {
  it('redirects to dashboard after Google login', () => {
    // Test complete auth workflow
  });
});
```

## End-to-End Testing (Playwright)

### Critical User Journeys
```typescript
test('Complete User Journey', async ({ page }) => {
  // 1. Land on homepage
  await page.goto('/');
  
  // 2. Sign in with Google (mocked)
  await page.click('[data-testid="google-login"]');
  
  // 3. View dashboard with initial points
  await expect(page.locator('[data-testid="main-points"]')).toBeVisible();
  
  // 4. Exchange points for tokens
  await page.click('[data-testid="exchange-button"]');
  await page.fill('[data-testid="points-input"]', '100');
  await page.click('[data-testid="confirm-exchange"]');
  
  // 5. Verify transaction success
  await expect(page.locator('[data-testid="transaction-success"]')).toBeVisible();
});
```

### Cross-Browser Testing
- **Browsers**: Chrome, Firefox, Safari, Edge
- **Devices**: Desktop, tablet, mobile viewports
- **Features**: Authentication, wallet connection, transactions

## Testing Infrastructure

### Test Databases
- **H2 In-Memory**: Fast unit and integration tests
- **PostgreSQL Testcontainers**: Realistic database testing
- **Data Seeding**: Consistent test data setup

### Mock Services
```java
@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public AlchemyService mockAlchemyService() {
        return Mockito.mock(AlchemyService.class);
    }
}
```

### CI/CD Integration
- **GitHub Actions**: Automated test execution on pull requests
- **Test Reports**: Coverage reports and test results
- **Quality Gates**: Minimum coverage thresholds

## Performance Testing

### Load Testing
- **API Endpoints**: Concurrent user simulation
- **Database Performance**: Query optimization validation
- **Blockchain Interactions**: Transaction throughput testing

### Security Testing
- **Authentication**: Token validation and expiration
- **Authorization**: Role-based access control
- **Input Validation**: SQL injection and XSS prevention
- **Rate Limiting**: API abuse prevention

## Test Data Management

### Test Fixtures
```java
@TestComponent
public class TestDataBuilder {
    public User createTestUser() {
        return User.builder()
            .googleId("test-google-id")
            .email("test@example.com")
            .name("Test User")
            .build();
    }
}
```

### Database Seeding
- **Realistic Data**: Representative user and transaction data
- **Edge Cases**: Boundary conditions and error scenarios
- **Performance Data**: Large datasets for performance testing

## Testing Best Practices

### Naming Conventions
- **Unit Tests**: `shouldDoSomethingWhenCondition()`
- **Integration Tests**: `shouldHandleCompleteWorkflow()`
- **E2E Tests**: `completeUserJourney()`

### Test Organization
- **AAA Pattern**: Arrange, Act, Assert
- **Single Responsibility**: One assertion per test when possible
- **Independent Tests**: No test dependencies
- **Fast Feedback**: Quick-running unit tests first

### Continuous Testing
- **Pre-commit Hooks**: Run unit tests before commits
- **Pull Request Checks**: Full test suite execution
- **Deployment Gates**: Tests must pass before deployment
- **Production Monitoring**: Health checks and synthetic transactions

## Testing Schedule

### Development Phase
- **Unit Tests**: Written alongside code development
- **Integration Tests**: After feature completion
- **E2E Tests**: Before feature sign-off

### Pre-Submission
- **Full Test Suite**: All tests passing
- **Coverage Report**: Generate and review coverage
- **Performance Baseline**: Establish performance benchmarks
- **Security Scan**: Static analysis and dependency check