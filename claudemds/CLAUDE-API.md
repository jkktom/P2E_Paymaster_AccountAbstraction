# API Documentation - Current Implementation & Planned Expansion

This document provides comprehensive API specifications for the blockchain goods exchange service, covering both implemented endpoints and planned functionality.

## 🟢 Implemented Authentication Endpoints

### GET /oauth2/authorization/google
- **Method**: Browser redirect
- **Description**: Initiates Google OAuth2 authentication flow
- **Implementation**: Spring Security OAuth2 client configuration
- **Response**: Redirects to Google OAuth consent screen

### GET /api/auth/login/google  
- **Authorization**: None (public)
- **Response**: `{ success, loginUrl, message }`
- **Description**: Returns Google OAuth2 login URL for frontend integration
- **Implementation Status**: ✅ Complete

### POST /api/auth/google
- **Authorization**: None (public)
- **Response**: `{ success, loginUrl, message }`
- **Description**: Alternative endpoint returning Google OAuth2 login URL
- **Implementation Status**: ✅ Complete

### GET /api/auth/user
- **Authorization**: Bearer JWT token required
- **Response**: `{ success, user: { googleId, email, name, avatar, roleId, createdAt }, balance: { mainPoint, subPoint, tokenBalance, updatedAt } }`
- **Description**: Get current authenticated user profile with balance information
- **Implementation Status**: ✅ Complete

### POST /api/auth/validate
- **Authorization**: None (public)
- **Body**: `{ token: "jwt-token" }`
- **Response**: `{ success, valid, user, shouldRefresh }`
- **Description**: Validate JWT token and return user information
- **Implementation Status**: ✅ Complete

### POST /api/auth/refresh
- **Authorization**: Bearer JWT token required
- **Response**: `{ success, token, expiresIn, message }`
- **Description**: Refresh expired JWT token using existing token
- **Implementation Status**: ✅ Complete

### POST /api/auth/logout
- **Authorization**: Bearer JWT token required
- **Response**: `{ success, message }`
- **Description**: Logout endpoint (client-side token removal)
- **Implementation Status**: ✅ Complete

## OAuth2 Success Flow

### POST /login/oauth2/code/google
- **Method**: Spring Security internal redirect
- **Description**: OAuth2 callback endpoint handled by OAuth2AuthenticationSuccessHandler
- **Flow**: Redirects to frontend with JWT token and user information
- **Frontend Callback**: `{frontend-url}/auth/callback?token={jwt}&user={name}`
- **Implementation Status**: ✅ Complete

## 🟡 Planned Point Management Endpoints

### GET /api/points/balance
- **Authorization**: Bearer JWT token required
- **Response**: `{ success, mainPoint, subPoint, tokenBalance, updatedAt }`
- **Description**: Get current user's complete point and token balance
- **Service Layer**: ✅ UserPointTokenService implemented
- **Controller**: 🚧 Needs implementation

### POST /api/points/earn/main
- **Authorization**: Bearer JWT token required (ADMIN only)
- **Body**: `{ userGoogleId, amount, sourceId, description }`
- **Response**: `{ success, transaction, newBalance }`
- **Description**: Award main points to user (admin operation)
- **Service Layer**: ✅ PointTransactionService.earnMainPoints() implemented
- **Controller**: 🚧 Needs implementation

### POST /api/points/earn/sub
- **Authorization**: Bearer JWT token required (ADMIN only)
- **Body**: `{ userGoogleId, amount, sourceId, description }`
- **Response**: `{ success, transaction, newBalance }`
- **Description**: Award sub points to user (admin operation)
- **Service Layer**: ✅ PointTransactionService.earnSubPoints() implemented
- **Controller**: 🚧 Needs implementation

### POST /api/points/convert
- **Authorization**: Bearer JWT token required
- **Body**: `{ subPointsToConvert }`
- **Response**: `{ success, transaction, mainPointsReceived, conversionRate }`
- **Description**: Convert sub points to main points using current conversion ratio
- **Service Layer**: ✅ PointTransactionService.convertSubToMainPoints() implemented  
- **Controller**: 🚧 Needs implementation

## 🟡 Planned Token Exchange Endpoints

### POST /api/exchange/tokens
- **Authorization**: Bearer JWT token required
- **Body**: `{ mainPointsToExchange }`
- **Response**: `{ success, transaction, tokensReceived, txHash, exchangeRate }`
- **Description**: Exchange main points for governance tokens using current exchange ratio
- **Service Layer**: ✅ PointTransactionService.exchangeMainPointsToTokens() implemented
- **Controller**: 🚧 Needs implementation
- **Blockchain**: 🚧 Needs smart contract integration

### GET /api/exchange/history
- **Authorization**: Bearer JWT token required
- **Query Params**: `?page=0&size=20`
- **Response**: `{ success, transactions: [{ pointsSpent, tokensReceived, txHash, status, createdAt }], page, totalElements }`
- **Description**: Get current user's token exchange transaction history
- **Service Layer**: 🚧 Needs TokenTransaction repository queries
- **Controller**: 🚧 Needs implementation

## 🟡 Planned Transaction History Endpoints

### GET /api/transactions/points
- **Authorization**: Bearer JWT token required
- **Query Params**: `?page=0&size=20&pointTypeId=1&sourceId=2&startDate=2024-01-01&endDate=2024-12-31`
- **Response**: `{ success, transactions, page, totalElements }`
- **Description**: Get current user's point transaction history with filtering
- **Service Layer**: ✅ PointTransactionService with comprehensive query methods implemented
- **Controller**: 🚧 Needs implementation

### GET /api/transactions/recent/earning
- **Authorization**: Bearer JWT token required
- **Query Params**: `?limit=10`
- **Response**: `{ success, transactions }`
- **Description**: Get user's recent point earning transactions
- **Service Layer**: ✅ PointTransactionService.getRecentEarningTransactions() implemented
- **Controller**: 🚧 Needs implementation

### GET /api/transactions/recent/spending
- **Authorization**: Bearer JWT token required
- **Query Params**: `?limit=10`
- **Response**: `{ success, transactions }`
- **Description**: Get user's recent point spending transactions
- **Service Layer**: ✅ PointTransactionService.getRecentSpendingTransactions() implemented
- **Controller**: 🚧 Needs implementation

### GET /api/transactions/statistics
- **Authorization**: Bearer JWT token required
- **Response**: `{ success, statistics: { totalMainEarned, totalSubEarned, totalMainExchanged, totalSubConverted, totalTransactions } }`
- **Description**: Get user's comprehensive point transaction statistics
- **Service Layer**: ✅ PointTransactionService.getUserStatistics() implemented
- **Controller**: 🚧 Needs implementation

## 🟡 Planned Admin-Only Endpoints (Role-Based Access Control)

### POST /api/admin/points/grant
- **Authorization**: Bearer JWT token required (ADMIN role only)
- **Body**: `{ userGoogleId, pointType: "MAIN" | "SUB", amount, sourceId, description }`
- **Response**: `{ success, transaction, message }`
- **Description**: Grant points to specific user (admin privilege)
- **Service Layer**: ✅ PointTransactionService.earnMainPoints() and earnSubPoints() support admin operations
- **Controller**: 🚧 Needs implementation with role validation

### GET /api/admin/users
- **Authorization**: Bearer JWT token required (ADMIN role only)
- **Query Params**: `?page=0&size=50&roleId=2`
- **Response**: `{ success, users: [{ googleId, name, email, roleId, createdAt, balance }], page, totalElements }`
- **Description**: List all users with their roles and point balances
- **Service Layer**: 🚧 Needs UserService extension for admin queries
- **Controller**: 🚧 Needs implementation

### POST /api/admin/users/role
- **Authorization**: Bearer JWT token required (ADMIN role only)
- **Body**: `{ userGoogleId, roleId }`
- **Response**: `{ success, message }`
- **Description**: Update user role (only admins can change roles)
- **Service Layer**: 🚧 Needs UserService extension for role management
- **Controller**: 🚧 Needs implementation

### GET /api/admin/statistics
- **Authorization**: Bearer JWT token required (ADMIN role only)
- **Response**: `{ success, stats: { totalUsers, totalAdmins, totalMainPoints, totalSubPoints, totalTokensIssued, recentTransactions } }`
- **Description**: System-wide statistics for admin dashboard
- **Service Layer**: 🚧 Needs comprehensive statistics service
- **Controller**: 🚧 Needs implementation

## 🟡 Planned User Profile Endpoints

### GET /api/user/profile
- **Authorization**: Bearer JWT token required
- **Response**: `{ success, profile: { googleId, name, email, avatar, roleId, createdAt } }`
- **Description**: Get current user's profile information
- **Implementation**: ✅ Available in /api/auth/user endpoint
- **Dedicated Endpoint**: 🚧 Needs separate controller implementation

### PUT /api/user/profile
- **Authorization**: Bearer JWT token required
- **Body**: `{ name }`
- **Response**: `{ success, updatedProfile, message }`
- **Description**: Update current user's profile information
- **Service Layer**: 🚧 Needs UserService extension for profile updates
- **Controller**: 🚧 Needs implementation

## Authorization & Security Implementation

### JWT Token Structure (Current Implementation)
```json
{
  "sub": "user-google-id",
  "email": "user@gmail.com",
  "name": "User Name",
  "iat": 1640995200,
  "exp": 1641081600
}
```

### Role-Based Access Control Architecture
**Current Implementation**: Role information stored in User entity, JWT contains basic user info
**Planned Enhancement**: Include roleId in JWT claims for efficient role-based authorization

- **ADMIN role (roleId: 1)**: Full system access including user management and point granting
- **USER role (roleId: 2)**: Standard user operations including point conversions and token exchanges
- **Role Validation**: Implemented in UserService, needs controller-level enforcement

### Authentication Flow (Implemented)
1. **Frontend initiates**: Call `/api/auth/login/google` to get OAuth2 URL
2. **User redirects**: Browser redirects to Google OAuth2 consent screen
3. **OAuth2 callback**: Google redirects to `/login/oauth2/code/google`
4. **Backend processing**: OAuth2AuthenticationSuccessHandler processes user info
5. **JWT generation**: Backend generates JWT token with user information
6. **Frontend callback**: User redirected to frontend with token and user info
7. **Token validation**: Frontend validates token via `/api/auth/validate`
8. **API access**: Include JWT in Authorization header for protected endpoints

## Error Response Format (Implemented)

All endpoints return consistent error response structure:
```json
{
  "success": false,
  "message": "Human readable error message",
  "error": "ERROR_CODE"
}
```

Success response structure:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* response data */ }
}
```

### HTTP Status Codes & Error Handling
- **200**: Success with data response
- **400**: Bad Request (validation errors, missing required fields)
- **401**: Unauthorized (invalid/missing JWT token)
- **403**: Forbidden (insufficient role permissions)
- **404**: Not Found (user or resource not found)
- **500**: Internal Server Error (system errors)

### Implemented Error Scenarios
- **JWT Validation**: Token expiration, invalid signature, missing token
- **User Not Found**: GoogleId not found in database
- **Authentication Flow**: OAuth2 processing failures
- **Input Validation**: Missing or invalid request parameters

## Implementation Status Summary

### ✅ Completed Components
- **Authentication System**: Complete OAuth2 + JWT implementation
- **User Management**: User creation, profile management, role system
- **Service Layer**: Comprehensive business logic for points and transactions
- **Database Layer**: Complete entity model with optimized queries
- **Security Configuration**: CORS, JWT filters, OAuth2 integration

### 🚧 Next Implementation Phase
- **Controller Layer**: Points, Exchange, Admin, and Transaction controllers
- **Role-Based Authorization**: JWT enhancement with roleId claims
- **Smart Contract Integration**: Token exchange blockchain operations
- **Frontend API Integration**: Complete API consumption implementation

The backend architecture provides a solid foundation with all business logic implemented in service layers, requiring only controller implementation to expose full API functionality.