# Architecture & Technical Decisions

This file documents the technical architecture choices and their justifications.

## Technology Stack Analysis

### Java Spring Boot vs Alternatives

#### Advantages over Express/Node.js:
- **Concurrency**: Better handling of concurrent blockchain operations through thread pools
- **Type Safety**: Compile-time error detection vs runtime errors in JavaScript
- **Ecosystem Maturity**: Spring Security, JPA, and Web3j provide production-ready patterns
- **Enterprise Patterns**: Built-in transaction management, dependency injection, and AOP
- **Testing Framework**: Comprehensive test annotations and mocking capabilities

#### Advantages over Django/Python:
- **Performance**: Superior performance for I/O intensive blockchain operations
- **Blockchain Libraries**: Web3j is more mature than web3.py for enterprise applications  
- **Static Typing**: Better IDE support and refactoring safety
- **JVM Ecosystem**: Access to extensive Java library ecosystem

#### Trade-offs:
- **Development Speed**: Slower initial setup compared to Express/Django
- **Learning Curve**: More complex for developers unfamiliar with Spring ecosystem
- **Resource Usage**: Higher memory footprint than Node.js applications

### Next.js TypeScript vs Alternatives

#### Advantages over React SPA:
- **SEO Optimization**: Server-side rendering for better search visibility
- **Performance**: Static generation and automatic code splitting
- **Developer Experience**: Built-in TypeScript support and hot reloading
- **Production Ready**: Optimized builds and deployment patterns

#### Advantages over Vue/Nuxt:
- **Ecosystem Size**: Larger component library and tool ecosystem
- **Web3 Integration**: Better Web3 library support and documentation
- **Market Adoption**: Higher industry adoption for enterprise applications

## Account Abstraction with Alchemy

### Integration Benefits:
- **Managed Infrastructure**: Alchemy handles smart wallet deployment and management
- **Gas Sponsorship**: Built-in gas policies eliminate user transaction costs
- **Developer Experience**: Account Kit provides seamless wallet integration
- **Security**: Battle-tested smart contracts and security practices
- **Scalability**: Enterprise-grade infrastructure for high transaction volumes

### Why Alchemy:
- **Market Leader**: Proven track record with major Web3 applications
- **Documentation**: Comprehensive guides and developer resources
- **Support**: Enterprise support and SLA guarantees
- **Integration**: Native TypeScript/Java SDK support

## Database Design Decisions

### Separated Point Tables Strategy

#### Why Separate MainPointAccount and SubPointAccount:
- **Scalability**: Independent scaling of different point types
- **Flexibility**: Different business logic for each point type
- **Reporting**: Easier analytics and tracking per point type
- **Future Extensions**: Room for additional point categories

#### PointEarnTransaction Design:
- **Audit Trail**: Complete history of all point earnings
- **Debugging**: Easier troubleshooting of point discrepancies  
- **Analytics**: Source tracking for engagement analysis
- **Compliance**: Transparent record keeping for potential regulations

### Google OAuth User Model

#### Simplified User Entity Benefits:
- **Reduced Complexity**: No password management or email verification
- **Better Security**: Delegated authentication to Google's infrastructure
- **User Convenience**: Single sign-on experience
- **Privacy Compliance**: Leverages Google's GDPR compliance

## Security Architecture

### Authentication & Authorization

#### JWT Token Strategy:
- **Stateless**: No server-side session storage required
- **Scalable**: Easy horizontal scaling without session affinity
- **Flexible**: Custom claims for user permissions and wallet address
- **Secure**: Short expiration with refresh token rotation

#### API Security Patterns:
- **Rate Limiting**: Prevent abuse of point earning and exchange endpoints
- **Input Validation**: Comprehensive validation using Spring Boot annotations  
- **SQL Injection Prevention**: JPA parameterized queries by default
- **CORS Configuration**: Restricted cross-origin requests

### Blockchain Security

#### Smart Contract Security:
- **OpenZeppelin**: Use of audited, battle-tested contract libraries
- **Access Control**: Role-based permissions for contract functions
- **Reentrancy Guards**: Protection against common attack vectors
- **Upgrade Patterns**: Proxy contracts for future improvements

## Interview Positioning

### Enterprise Readiness:
- **Production Patterns**: Demonstrates understanding of scalable architecture
- **Security First**: Shows security-conscious development approach  
- **Modern Stack**: Exhibits knowledge of current industry standards
- **Full-Stack Competence**: Proves ability to work across technology stack

### Innovation Showcase:
- **Account Abstraction**: Demonstrates cutting-edge Web3 UX knowledge
- **User Experience**: Shows understanding of mainstream adoption barriers
- **Integration Skills**: Exhibits ability to combine multiple complex systems
- **Business Acumen**: Understands the importance of user onboarding friction