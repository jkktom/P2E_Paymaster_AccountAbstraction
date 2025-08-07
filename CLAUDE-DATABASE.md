# Database Design & JPA Entities

This file documents the final database design using JPA entities with proper ID types and normalization.

## Database Technology Choices

### Development Environment
- **H2 In-Memory Database**: Zero setup, fast startup for development
- **Benefits**: No installation, works anywhere, perfect for testing
- **Configuration**: `spring.datasource.url=jdbc:h2:mem:testdb`
- **Web Console**: Built-in database browser at `/h2-console`

### Production Environment  
- **PostgreSQL**: Production-grade RDBMS on AWS RDS
- **Version**: PostgreSQL 15+
- **Hosting**: AWS RDS Managed Instance

## JPA Entity Design

### User Entity (UUID Primary Key with Role-Based Access Control)
```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_google_id", columnList = "googleId"),
    @Index(name = "idx_users_wallet_address", columnList = "smartWalletAddress"),
    @Index(name = "idx_users_role", columnList = "roleId")
})
public class User {
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(unique = true, nullable = false)
    private String googleId;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String avatar;
    
    @Column(name = "smart_wallet_address", unique = true, nullable = false, length = 42)
    private String smartWalletAddress;
    
    @Column(name = "role_id", nullable = false)
    private Byte roleId = 2; // Default to USER role
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Point Type Table (Normalized)
```java
@Entity
@Table(name = "point_types")
public class PointType {
    @Id
    private Byte id; // 1 = MAIN, 2 = SUB
    
    @Column(unique = true, nullable = false, length = 10)
    private String name; // "MAIN", "SUB"
    
    // Constructor for initialization
    public PointType(Byte id, String name) {
        this.id = id;
        this.name = name;
    }
}
```

### Transaction Status Table (Normalized)
```java
@Entity
@Table(name = "transaction_statuses")
public class TransactionStatus {
    @Id
    private Byte id; // 1 = PENDING, 2 = CONFIRMED, 3 = FAILED
    
    @Column(unique = true, nullable = false, length = 20)
    private String name; // "PENDING", "CONFIRMED", "FAILED"
    
    public TransactionStatus(Byte id, String name) {
        this.id = id;
        this.name = name;
    }
}
```

### Point Source Table (Normalized)
```java
@Entity
@Table(name = "point_sources")
public class PointSource {
    @Id
    private Byte id; // 1 = TASK_COMPLETION, 2 = EVENT_REWARD, 3 = ADMIN_GRANT
    
    @Column(unique = true, nullable = false, length = 50)
    private String name; // "TASK_COMPLETION", "EVENT_REWARD", "ADMIN_GRANT"
    
    public PointSource(Byte id, String name) {
        this.id = id;
        this.name = name;
    }
}
```

### Role Table (User Access Control)
```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    private Byte id; // 1 = ADMIN, 2 = USER
    
    @Column(unique = true, nullable = false, length = 10)
    private String name; // "ADMIN", "USER"
    
    public Role(Byte id, String name) {
        this.id = id;
        this.name = name;
    }
}
```

### Main Point Account Entity
```java
@Entity
@Table(name = "main_point_accounts")
public class MainPointAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_google_id", nullable = false, unique = true)
    private String userGoogleId; // Direct reference to User.googleId
    
    @Column(nullable = false)
    @Min(0)
    private Integer balance = 0;
    
    @Column(nullable = false)
    @Min(0)
    private Integer totalEarned = 0;
    
    @Column(nullable = false)
    @Min(0)
    private Integer pointsToToken = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Sub Point Account Entity
```java
@Entity
@Table(name = "sub_point_accounts")
public class SubPointAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_google_id", nullable = false, unique = true)
    private String userGoogleId; // Direct reference to User.googleId
    
    @Column(nullable = false)
    @Min(0)
    private Integer balance = 0;
    
    @Column(nullable = false)
    @Min(0)
    private Integer totalEarned = 0;
    
    @Column(nullable = false)
    @Min(0)
    private Integer subToMain = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Point Earn Transaction Entity
```java
@Entity
@Table(name = "point_earn_transactions", indexes = {
    @Index(name = "idx_point_earn_user_date", columnList = "userGoogleId, createdAt"),
    @Index(name = "idx_point_earn_type", columnList = "pointTypeId"),
    @Index(name = "idx_point_earn_source", columnList = "sourceId")
})
public class PointEarnTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_google_id", nullable = false)
    private String userGoogleId; // Direct reference to User.googleId
    
    @Column(name = "point_type_id", nullable = false)
    private Byte pointTypeId; // 1 = MAIN, 2 = SUB
    
    @Column(nullable = false)
    @Min(1)
    private Integer amount;
    
    @Column(name = "source_id", nullable = false)
    private Byte sourceId; // 1 = TASK_COMPLETION, 2 = EVENT_REWARD, 3 = ADMIN_GRANT
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### Exchange Transaction Entity
```java
@Entity
@Table(name = "exchange_transactions", indexes = {
    @Index(name = "idx_exchange_user_date", columnList = "userGoogleId, createdAt"),
    @Index(name = "idx_exchange_tx_hash", columnList = "txHash"),
    @Index(name = "idx_exchange_status", columnList = "statusId")
})
public class ExchangeTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_google_id", nullable = false)
    private String userGoogleId; // Direct reference to User.googleId
    
    @Column(nullable = false)
    @Min(10) // Minimum 10 points for 1 token
    private Integer pointsSpent;
    
    @Column(nullable = false)
    @Min(1)
    private Integer tokensReceived;
    
    @Column(unique = true, length = 66)
    private String txHash;
    
    @Column(name = "status_id", nullable = false)
    private Byte statusId = 1; // 1 = PENDING, 2 = CONFIRMED, 3 = FAILED
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime confirmedAt;
    
    // Business rule validation
    @PrePersist
    @PreUpdate
    private void validateExchangeRate() {
        if (pointsSpent != tokensReceived * 10) {
            throw new IllegalStateException("Exchange rate must be 10:1 (points:tokens)");
        }
    }
}
```

### Conversion Transaction Entity (Fixed 10:1 Ratio)
```java
@Entity
@Table(name = "conversion_transactions", indexes = {
    @Index(name = "idx_conversion_user_date", columnList = "userGoogleId, createdAt")
})
public class ConversionTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_google_id", nullable = false)
    private String userGoogleId; // Direct reference to User.googleId
    
    @Column(nullable = false)
    @Min(10) // Minimum 10 sub points for 1 main point
    private Integer subPointsSpent;
    
    @Column(nullable = false)
    @Min(1)
    private Integer mainPointsReceived;
    
    @Column(nullable = false)
    private Byte conversionRate = 10; // Always 10:1 (10 sub points = 1 main point)
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Business rule validation
    @PrePersist
    @PreUpdate
    private void validateConversionRate() {
        if (subPointsSpent != mainPointsReceived * 10) {
            throw new IllegalStateException("Conversion rate must be 10:1 (sub:main)");
        }
    }
}
```

### Wallet Transaction Entity
```java
@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_wallet_tx_user_date", columnList = "userGoogleId, createdAt"),
    @Index(name = "idx_wallet_tx_hash", columnList = "txHash"),
    @Index(name = "idx_wallet_tx_block", columnList = "blockNumber")
})
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_google_id", nullable = false)
    private String userGoogleId; // Direct reference to User.googleId
    
    @Column(nullable = false, length = 20)
    private String transactionType; // TOKEN_EXCHANGE, GOVERNANCE_VOTE
    
    @Column(nullable = false)
    private Integer amount;
    
    @Column(nullable = false)
    private Boolean gasSponsored = true;
    
    @Column(unique = true, length = 66)
    private String txHash;
    
    private Long blockNumber;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

## Data Initialization (Reference Data & Demo Users)

```java
@Component
@Profile("!prod")
public class DataInitializer {
    
    @Autowired
    private PointTypeRepository pointTypeRepository;
    
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    
    @Autowired
    private PointSourceRepository pointSourceRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MainPointAccountRepository mainPointRepository;
    
    @Autowired
    private SubPointAccountRepository subPointRepository;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeReferenceData() {
        // Initialize Roles
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role((byte) 1, "ADMIN"));
            roleRepository.save(new Role((byte) 2, "USER"));
        }
        
        // Initialize Point Types
        if (pointTypeRepository.count() == 0) {
            pointTypeRepository.save(new PointType((byte) 1, "MAIN"));
            pointTypeRepository.save(new PointType((byte) 2, "SUB"));
        }
        
        // Initialize Transaction Statuses
        if (transactionStatusRepository.count() == 0) {
            transactionStatusRepository.save(new TransactionStatus((byte) 1, "PENDING"));
            transactionStatusRepository.save(new TransactionStatus((byte) 2, "CONFIRMED"));
            transactionStatusRepository.save(new TransactionStatus((byte) 3, "FAILED"));
        }
        
        // Initialize Point Sources
        if (pointSourceRepository.count() == 0) {
            pointSourceRepository.save(new PointSource((byte) 1, "TASK_COMPLETION"));
            pointSourceRepository.save(new PointSource((byte) 2, "EVENT_REWARD"));
            pointSourceRepository.save(new PointSource((byte) 3, "ADMIN_GRANT"));
        }
        
        // Initialize Demo Users for Interview Demonstration
        if (userRepository.count() == 0) {
            // Create demo admin user
            User adminUser = new User();
            adminUser.setGoogleId("demo-admin-google-id");
            adminUser.setEmail("admin@demo.com");
            adminUser.setName("Demo Admin");
            adminUser.setSmartWalletAddress("0x1234567890123456789012345678901234567890");
            adminUser.setRoleId((byte) 1); // ADMIN role
            User savedAdmin = userRepository.save(adminUser);
            
            // Create point accounts for admin
            MainPointAccount adminMainPoints = new MainPointAccount();
            adminMainPoints.setUserGoogleId(savedAdmin.getGoogleId());
            adminMainPoints.setBalance(1000);
            adminMainPoints.setTotalEarned(1000);
            mainPointRepository.save(adminMainPoints);
            
            SubPointAccount adminSubPoints = new SubPointAccount();
            adminSubPoints.setUserGoogleId(savedAdmin.getGoogleId());
            adminSubPoints.setBalance(500);
            adminSubPoints.setTotalEarned(500);
            subPointRepository.save(adminSubPoints);
            
            // Create demo regular user
            User regularUser = new User();
            regularUser.setGoogleId("demo-user-google-id");
            regularUser.setEmail("user@demo.com");
            regularUser.setName("Demo User");
            regularUser.setSmartWalletAddress("0x0987654321098765432109876543210987654321");
            regularUser.setRoleId((byte) 2); // USER role (default)
            User savedUser = userRepository.save(regularUser);
            
            // Create point accounts for regular user
            MainPointAccount userMainPoints = new MainPointAccount();
            userMainPoints.setUserGoogleId(savedUser.getGoogleId());
            userMainPoints.setBalance(100);
            userMainPoints.setTotalEarned(100);
            mainPointRepository.save(userMainPoints);
            
            SubPointAccount userSubPoints = new SubPointAccount();
            userSubPoints.setUserGoogleId(savedUser.getGoogleId());
            userSubPoints.setBalance(200);
            userSubPoints.setTotalEarned(200);
            subPointRepository.save(userSubPoints);
        }
    }
}
```

## Constants for Reference IDs

```java
public class DatabaseConstants {
    
    // Roles
    public static final byte ROLE_ADMIN = 1;
    public static final byte ROLE_USER = 2;
    
    // Point Types
    public static final byte MAIN_POINT_TYPE = 1;
    public static final byte SUB_POINT_TYPE = 2;
    
    // Transaction Statuses
    public static final byte STATUS_PENDING = 1;
    public static final byte STATUS_CONFIRMED = 2;
    public static final byte STATUS_FAILED = 3;
    
    // Point Sources
    public static final byte SOURCE_TASK_COMPLETION = 1;
    public static final byte SOURCE_EVENT_REWARD = 2;
    public static final byte SOURCE_ADMIN_GRANT = 3;
    
    // Fixed Conversion Rates
    public static final byte SUB_TO_MAIN_RATE = 10; // 10 sub points = 1 main point
    public static final byte MAIN_TO_TOKEN_RATE = 10; // 10 main points = 1 token
}
```

## Repository Interfaces

```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findBySmartWalletAddress(String address);
    List<User> findByRoleId(Byte roleId);
    boolean existsByGoogleIdAndRoleId(String googleId, Byte roleId);
}

@Repository
public interface RoleRepository extends JpaRepository<Role, Byte> {
    Optional<Role> findByName(String name);
}

@Repository
public interface MainPointAccountRepository extends JpaRepository<MainPointAccount, Integer> {
    Optional<MainPointAccount> findByUserGoogleId(String userGoogleId);
}

@Repository
public interface SubPointAccountRepository extends JpaRepository<SubPointAccount, Integer> {
    Optional<SubPointAccount> findByUserGoogleId(String userGoogleId);
}

@Repository
public interface PointEarnTransactionRepository extends JpaRepository<PointEarnTransaction, Integer> {
    Page<PointEarnTransaction> findByUserGoogleIdOrderByCreatedAtDesc(String userGoogleId, Pageable pageable);
    List<PointEarnTransaction> findByUserGoogleIdAndPointTypeIdOrderByCreatedAtDesc(String userGoogleId, Byte pointTypeId);
}

@Repository
public interface ExchangeTransactionRepository extends JpaRepository<ExchangeTransaction, Integer> {
    Page<ExchangeTransaction> findByUserGoogleIdOrderByCreatedAtDesc(String userGoogleId, Pageable pageable);
    List<ExchangeTransaction> findByStatusId(Byte statusId);
}

@Repository
public interface ConversionTransactionRepository extends JpaRepository<ConversionTransaction, Integer> {
    Page<ConversionTransaction> findByUserGoogleIdOrderByCreatedAtDesc(String userGoogleId, Pageable pageable);
}
```

## Service Layer Example with Role-Based Access Control

```java
@Service
@Transactional
public class PointService {
    
    @Autowired
    private MainPointAccountRepository mainPointRepository;
    
    @Autowired
    private SubPointAccountRepository subPointRepository;
    
    @Autowired
    private PointEarnTransactionRepository earnTransactionRepository;
    
    @Autowired
    private ConversionTransactionRepository conversionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Admin only - grant points to users
    @PreAuthorize("hasRole('ADMIN')")
    public void grantPointsToUser(String targetUserGoogleId, Byte pointTypeId, Integer amount, String description) {
        earnPoints(targetUserGoogleId, pointTypeId, amount, DatabaseConstants.SOURCE_ADMIN_GRANT, description);
    }
    
    // Admin only - promote user to admin
    @PreAuthorize("hasRole('ADMIN')")
    public void promoteUserToAdmin(String targetUserGoogleId) {
        User user = userRepository.findByGoogleId(targetUserGoogleId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setRoleId(DatabaseConstants.ROLE_ADMIN);
        userRepository.save(user);
    }
    
    // Both admin and user can earn points through system actions
    public void earnPoints(String userGoogleId, Byte pointTypeId, Integer amount, Byte sourceId, String description) {
        // Record transaction
        PointEarnTransaction transaction = new PointEarnTransaction();
        transaction.setUserGoogleId(userGoogleId);
        transaction.setPointTypeId(pointTypeId);
        transaction.setAmount(amount);
        transaction.setSourceId(sourceId);
        transaction.setDescription(description);
        earnTransactionRepository.save(transaction);
        
        // Update balance
        if (pointTypeId == DatabaseConstants.MAIN_POINT_TYPE) {
            MainPointAccount account = mainPointRepository.findByUserGoogleId(userGoogleId)
                .orElseThrow(() -> new EntityNotFoundException("Main point account not found"));
            account.setBalance(account.getBalance() + amount);
            account.setTotalEarned(account.getTotalEarned() + amount);
            mainPointRepository.save(account);
        } else {
            SubPointAccount account = subPointRepository.findByUserGoogleId(userGoogleId)
                .orElseThrow(() -> new EntityNotFoundException("Sub point account not found"));
            account.setBalance(account.getBalance() + amount);
            account.setTotalEarned(account.getTotalEarned() + amount);
            subPointRepository.save(account);
        }
    }
    
    public void convertSubToMainPoints(String userGoogleId, Integer subPoints) {
        if (subPoints % DatabaseConstants.SUB_TO_MAIN_RATE != 0) {
            throw new IllegalArgumentException("Sub points must be multiple of " + DatabaseConstants.SUB_TO_MAIN_RATE);
        }
        
        Integer mainPointsToReceive = subPoints / DatabaseConstants.SUB_TO_MAIN_RATE;
        
        // Deduct sub points
        SubPointAccount subAccount = subPointRepository.findByUserGoogleId(userGoogleId)
            .orElseThrow(() -> new EntityNotFoundException("Sub point account not found"));
        if (subAccount.getBalance() < subPoints) {
            throw new IllegalStateException("Insufficient sub points");
        }
        subAccount.setBalance(subAccount.getBalance() - subPoints);
        subAccount.setSubToMain(subAccount.getSubToMain() + subPoints);
        subPointRepository.save(subAccount);
        
        // Add main points
        MainPointAccount mainAccount = mainPointRepository.findByUserGoogleId(userGoogleId)
            .orElseThrow(() -> new EntityNotFoundException("Main point account not found"));
        mainAccount.setBalance(mainAccount.getBalance() + mainPointsToReceive);
        mainAccount.setTotalEarned(mainAccount.getTotalEarned() + mainPointsToReceive);
        mainPointRepository.save(mainAccount);
        
        // Record conversion transaction
        ConversionTransaction conversion = new ConversionTransaction();
        conversion.setUserGoogleId(userGoogleId);
        conversion.setSubPointsSpent(subPoints);
        conversion.setMainPointsReceived(mainPointsToReceive);
        conversionRepository.save(conversion);
    }
}
```

## Database Design Benefits

### Performance Optimizations:
- **UUID Primary Keys**: Globally unique, suitable for distributed systems
- **String Foreign Keys**: Direct Google ID references, no joins needed for user lookup
- **Byte Lookup Tables**: Minimal storage for enum-like data
- **Integer Point Values**: Sufficient for POC, easy arithmetic operations
- **Strategic Indexing**: Optimized for common query patterns

### Business Logic Enforcement:
- **JPA Validation**: @Min annotations prevent negative values
- **Custom Validation**: @PrePersist/@PreUpdate enforce business rules
- **Fixed Rates**: Hardcoded 10:1 ratios for simplicity and consistency
- **Reference Data**: Normalized lookup tables for maintainability

This design provides a clean, performant, and maintainable database structure perfect for your technical interview submission.