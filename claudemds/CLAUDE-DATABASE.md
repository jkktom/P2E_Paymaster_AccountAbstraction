# Database Design - Architecture Overview

This document explains the database architecture and design decisions for the blockchain goods exchange service.

## System Architecture Philosophy

**Core Design Principle**: Simple, performant, maintainable data layer supporting hybrid Web2/Web3 operations.

### Key Architectural Decisions

1. **String-based Relations**: Uses `googleId` strings instead of JPA foreign keys for better performance
2. **Reference Tables**: Expandable lookup tables (Role, PointEarnSpendSource, TransactionStatus) for business flexibility
3. **Current Balance Focus**: UserPointToken stores only current balances; historical data comes from transaction logs
4. **Granular Transaction Logging**: Separate amount fields per operation type for detailed audit trails

## Entity Architecture

### User Management Layer

#### User Entity
**Location**: `user/entity/User.java`

**Purpose**: Core user profile from Google OAuth authentication

**Key Design Decisions:**
- **UUID Primary Key**: Better for distributed systems than auto-increment integers
- **Direct Role Reference**: `roleId` byte field instead of JPA relationship for performance  
- **Google OAuth Integration**: `googleId` as unique business identifier
- **Indexed Access**: Database index on `googleId` for fast lookups

#### Role Entity (Expandable Reference)
**Location**: `user/entity/Role.java`

**Purpose**: Expandable role system supporting future business growth

**Business Flexibility**: 
- Current: `{1: "ADMIN", 2: "USER"}`
- Future: Can add `{3: "MODERATOR", 4: "PREMIUM"}` without schema changes
- Database-driven role management without code deployment

### Points & Token Balance Layer

#### UserPointToken Entity (Current Balances Only)
**Location**: `userdetail/entity/UserPointToken.java`

**Purpose**: Simple current balance tracking for user's points and tokens

**Key Design Decisions:**
- **No Historical Tracking**: Only current balances; history comes from PointTransaction queries
- **Direct Google ID Reference**: Uses `userGoogleId` string field, not User entity relationship  
- **Simple Balance Fields**: `mainPoint`, `subPoint`, `tokenBalance` - no cumulative totals
- **Atomic Operations**: Business methods handle conversions and exchanges safely
- **Validation Built-in**: Prevents negative balances and invalid operations

**Why This Design:**
- **Performance**: No JPA relationship overhead
- **Data Integrity**: Single source of truth for historical data (transaction tables)
- **Simplicity**: Easy to understand and maintain
- **Scalability**: No complex aggregate calculations at balance level

### Transaction Logging Layer

#### PointTransaction Entity (Comprehensive Audit Trail)
**Location**: `pointtransaction/entity/PointTransaction.java`

**Purpose**: Complete audit trail for all point operations with granular amount tracking

**Key Design Features:**
- **Granular Amount Fields**: Separate fields for each operation type (`mainEarnAmount`, `subConvertedAmount`, etc.)
- **Source Classification**: 12 predefined source types covering all business scenarios
- **Transaction Lifecycle**: PENDING → CONFIRMED/FAILED status tracking
- **Configurable Ratios**: Static configuration for conversion and exchange rates
- **Database Optimization**: Strategic indexes on user/date/type/source for fast queries

**Why Granular Amount Tracking:**
- **Detailed Reporting**: Can answer "how many sub points were converted?" vs "how many main points were earned?"
- **Business Intelligence**: Separate metrics for earning vs spending vs converting
- **Audit Compliance**: Clear separation of different transaction types
- **Query Performance**: Direct aggregation without complex calculations

#### TokenTransaction Entity (Blockchain Integration)  
**Location**: `pointtransaction/entity/TokenTransaction.java`

**Purpose**: Track blockchain transactions for point-to-token exchanges

**Integration Features:**
- **zkSync Transaction Tracking**: Stores blockchain transaction hashes
- **Exchange Rate Validation**: Enforces 10:1 ratio at database level
- **Gas Sponsorship Tracking**: Records whether transaction was sponsored (always true for zkSync AA)
- **Balance Application Status**: Tracks whether blockchain transaction was applied to UserPointToken balance

### Reference Data Layer

#### PointEarnSpendSource Entity (Transaction Classification)
**Location**: `global/entity/PointEarnSpendSource.java`

**Purpose**: Predefined transaction source classification system

**Business Model:**
- **12 Source Types**: Covers all earning and spending scenarios
- **MAIN Sources (1-6)**: Task completion, events, admin grants, exchanges
- **SUB Sources (7-12)**: Task completion, events, admin grants, conversions
- **Helper Methods**: Business logic for source type detection and validation

**Architectural Benefit**: Extensible classification system without code changes

#### TransactionStatus Entity (Status Management)
**Location**: `global/entity/TransactionStatus.java`

**Purpose**: Transaction lifecycle status tracking

**Status Model:**
- **PENDING (1)**: Transaction initiated
- **CONFIRMED (2)**: Successfully processed
- **FAILED (3)**: Transaction failed or rolled back

**Usage**: Both PointTransaction and TokenTransaction reference this for consistent status management

## Architectural Design Patterns

### 1. Performance-First Data Relations
**googleId String References**: Instead of traditional JPA foreign keys, uses Google OAuth ID strings
- **Benefits**: Eliminates JPA relationship overhead, faster queries, better caching
- **Trade-off**: Manual relationship management vs automatic ORM benefits
- **Why Chosen**: Google OAuth provides stable, unique identifiers perfect for this pattern

### 2. Current State + Transaction Log Architecture  
**Separation of Concerns**: UserPointToken stores only current balances; PointTransaction stores complete history
- **Benefits**: Fast balance queries, complete audit trail, simplified balance updates
- **Pattern**: Event Sourcing lite - current state derived from transaction sequence
- **Scalability**: Historical queries can be moved to separate read replicas

### 3. Reference Data Strategy
**Byte ID Lookups**: PointEarnSpendSource and TransactionStatus use 1-byte primary keys
- **Storage Efficiency**: 1 byte vs 4-8 bytes for traditional integer keys
- **Business Flexibility**: New sources/statuses added via database, not code deployment  
- **Query Performance**: Small lookup tables with predictable access patterns

### 4. Transaction Lifecycle Management
**Three-Phase Commit Pattern**: PENDING → Apply Changes → CONFIRMED/FAILED
- **Data Integrity**: Rollback capability if balance updates fail
- **Audit Trail**: Complete visibility into transaction success/failure
- **Business Logic**: Handles complex scenarios like insufficient balance gracefully

## Technical Implementation Insights

### Database Environment Strategy
**Development**: H2 in-memory database with console access for rapid prototyping
**Production**: PostgreSQL with connection pooling and transaction isolation

**Key Configuration Files:**
- `application-dev.yml` - H2 setup with console access at `/h2-console`
- `application-prod.yml` - PostgreSQL with environment variable configuration

### Repository Layer Architecture
**Location**: Each entity package contains its repository interface

**Design Philosophy**: 
- **Method Naming Conventions**: Spring Data JPA query derivation from method names
- **Custom Queries**: `@Query` annotations for complex aggregations and business logic
- **Pagination Support**: All list operations support `Pageable` parameters for performance
- **Statistical Queries**: Database-level aggregations for reporting and analytics

**Key Repository Features:**
- **UserRepository**: GoogleId-based lookups, role filtering
- **UserPointTokenRepository**: Balance retrieval by googleId
- **PointTransactionRepository**: Comprehensive transaction history with filtering, aggregations, and business intelligence queries

### Query Optimization Strategy
**Strategic Indexing**: Database indexes on frequently accessed columns
- User: `googleId` for OAuth lookups  
- PointTransaction: `(userGoogleId, createdAt)`, `pointTypeId`, `sourceId`
- TokenTransaction: `(userGoogleId, createdAt)`, `txHash`, `transactionStatusId`

**Aggregation Performance**: Custom `@Query` annotations push calculations to database level instead of application memory

## Service Layer Integration

### Business Logic Architecture
**Location**: Each domain package contains its service classes

**Service Design Patterns:**
- **UserService**: OAuth user lifecycle, role management, profile updates
- **UserPointTokenService**: Current balance operations, atomic conversions/exchanges
- **PointTransactionService**: Transaction lifecycle management, historical queries, business intelligence

### Transaction Coordination Pattern
**Three-Layer Architecture**: Controller → Service → Repository

**PointTransactionService Integration:**
1. **Transaction Creation**: Creates PENDING PointTransaction record
2. **Balance Application**: Calls UserPointTokenService for atomic balance updates  
3. **Status Confirmation**: Updates transaction to CONFIRMED/FAILED based on success
4. **Audit Completion**: Comprehensive logging with error handling and rollback

## Strategic Design Decisions

### 1. Simplified Balance Model
**Single UserPointToken Entity**: Instead of separate account entities per point type
- **Reasoning**: Reduces join complexity, enables atomic cross-point-type operations
- **Trade-off**: Less normalized but significantly better performance
- **Result**: Simple balance queries, atomic conversions, easier service logic

### 2. Historical Data Strategy  
**Transaction Log as Source of Truth**: Current balances + complete transaction history
- **Pattern**: Event Sourcing principles without full CQRS complexity
- **Benefits**: Complete audit trail, point-in-time balance calculation, business intelligence
- **Scalability**: Historical queries can be optimized separately from balance queries

### 3. String-based Relationships
**GoogleId References**: Avoids traditional JPA entity relationships
- **Performance**: Eliminates ORM overhead for high-frequency operations
- **Simplicity**: Direct string matching in queries vs complex join operations
- **OAuth Alignment**: Uses Google's stable identifier as natural foreign key

### 4. Reference Data Flexibility
**Database-Driven Configuration**: PointEarnSpendSource, TransactionStatus, Role as entities
- **Business Agility**: New sources/statuses/roles without code deployment
- **Consistency**: Centralized lookup data with referential integrity
- **Evolution**: System can grow with business requirements without schema migrations

## Implementation Status & Future Evolution

### Current State
**Robust Foundation**: Complete entity layer with sophisticated transaction management, OAuth integration, and performance-optimized queries ready for production scale.

**Key Capabilities**: 
- User authentication and role management
- Comprehensive point system with audit trails  
- Transaction lifecycle management with rollback capabilities
- Business intelligence queries for reporting and analytics

### Next Development Phases
**Blockchain Integration**: TokenTransaction entity ready for zkSync smart contract coordination
**API Layer**: Service layer complete and ready for REST controller implementation  
**Production Scaling**: PostgreSQL migration with connection pooling and transaction isolation

### Architectural Benefits Achieved
**Performance**: Optimized for high-frequency balance operations with minimal database overhead
**Auditability**: Complete transaction history with granular amount tracking for compliance
**Scalability**: Event sourcing patterns support read replicas and analytical workloads
**Flexibility**: Reference data strategy enables business rule evolution without deployment cycles

This database architecture provides a solid foundation for Web2/Web3 hybrid operations with excellent performance, maintainability, and business agility characteristics.