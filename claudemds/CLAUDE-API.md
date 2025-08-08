# API Documentation

This file contains detailed API specifications for the blockchain goods exchange service.

## Authentication Endpoints

### POST /api/auth/google
- **Body**: `{ googleToken }`
- **Response**: `{ jwtToken, user: { id, googleId, name, email, role, smartWalletAddress }, refreshToken }`
- **Description**: Authenticates user with Google OAuth, creates account and smart wallet, assigns USER role by default

### POST /api/auth/refresh
- **Body**: `{ refreshToken }`
- **Response**: `{ jwtToken }`
- **Description**: Refreshes expired JWT token

### POST /api/auth/demo-login
- **Body**: `{ userType }` // "admin" or "user"
- **Response**: `{ jwtToken, user, message }`
- **Description**: Demo login for interview presentation (bypasses Google OAuth)

## Point Management Endpoints

### GET /api/points/main
- **Authorization**: USER or ADMIN role required
- **Response**: `{ balance, totalEarned, pointsToToken }`
- **Description**: Get current user's main point account details

### GET /api/points/sub
- **Authorization**: USER or ADMIN role required
- **Response**: `{ balance, totalEarned, subToMain }`
- **Description**: Get current user's sub point account details

### POST /api/points/earn
- **Authorization**: System use only (internal API)
- **Body**: `{ userGoogleId, pointType, amount, source, description }`
- **Response**: `{ success, newBalance }`
- **Description**: Award points to user (triggered by system events, not direct user calls)

### POST /api/points/convert-sub-to-main
- **Authorization**: USER or ADMIN role required
- **Body**: `{ subPoints }`
- **Response**: `{ mainPointsReceived, conversionRate }`
- **Description**: Convert sub points to main points (10:1 ratio)

## Token Exchange Endpoints

### POST /api/exchange/points-to-tokens
- **Authorization**: USER or ADMIN role required
- **Body**: `{ mainPoints }`
- **Response**: `{ txHash, tokensReceived, status }`
- **Description**: Exchange main points for governance tokens (10:1 ratio)

### GET /api/exchange/history
- **Authorization**: USER or ADMIN role required
- **Response**: `[{ pointsSpent, tokensReceived, txHash, status, date }]`
- **Description**: Get current user's token exchange history

## Transaction History Endpoints

### GET /api/transactions/points
- **Authorization**: USER or ADMIN role required
- **Response**: `[{ type, amount, source, description, date }]`
- **Description**: Get current user's point earning transaction history

### GET /api/transactions/wallet
- **Authorization**: USER or ADMIN role required
- **Response**: `[{ type, amount, txHash, gasSponsored, date }]`
- **Description**: Get current user's blockchain transaction history

### GET /api/transactions/conversions
- **Authorization**: USER or ADMIN role required
- **Response**: `[{ subPointsSpent, mainPointsReceived, conversionRate, date }]`
- **Description**: Get current user's point conversion history

## Admin-Only Endpoints (Role-Based Access Control)

### POST /api/admin/grant-points
- **Authorization**: ADMIN role required
- **Body**: `{ userGoogleId, pointType, amount, description }`
- **Response**: `{ success, message }`
- **Description**: Grant points to specific user (admin privilege)

### GET /api/admin/users
- **Authorization**: ADMIN role required
- **Response**: `[{ id, googleId, name, email, role, pointBalances, createdAt }]`
- **Description**: List all users with their roles and point balances

### POST /api/admin/promote-user
- **Authorization**: ADMIN role required
- **Body**: `{ userGoogleId }`
- **Response**: `{ success, message }`
- **Description**: Promote user to admin role (only admins can create admins)

### POST /api/admin/transfer-tokens
- **Authorization**: ADMIN role required
- **Body**: `{ fromUserGoogleId, toUserGoogleId, tokenAmount, reason }`
- **Response**: `{ txHash, success }`
- **Description**: Transfer governance tokens between user wallets (for account consolidation requests)

### GET /api/admin/dashboard-stats
- **Authorization**: ADMIN role required
- **Response**: `{ totalUsers, totalAdmins, totalMainPoints, totalSubPoints, totalTokensIssued }`
- **Description**: System-wide statistics for admin dashboard

## User Endpoints (Both USER and ADMIN roles can access)

### GET /api/user/profile
- **Authorization**: USER or ADMIN role required
- **Response**: `{ id, googleId, name, email, role, smartWalletAddress, createdAt }`
- **Description**: Get current user's profile information

### GET /api/user/role
- **Authorization**: USER or ADMIN role required
- **Response**: `{ role, permissions }`
- **Description**: Get current user's role and permissions for frontend authorization

## Authorization & Security

### JWT Token Structure
```json
{
  "sub": "user-google-id",
  "roleId": 1, // 1 = ADMIN, 2 = USER
  "role": "ADMIN", // or "USER"
  "smartWalletAddress": "0x...",
  "iat": 1640995200,
  "exp": 1641081600
}
```

### Role-Based Access Control
- **ADMIN role (roleId: 1)**: Full access to all endpoints including admin-only operations
- **USER role (roleId: 2)**: Access to user endpoints only, cannot perform admin operations
- **System endpoints**: Internal APIs not exposed to frontend

### Demo Login (Interview Mode)
For demonstration purposes, the application includes demo login endpoints that bypass Google OAuth:
- Demo Admin: `admin@demo.com` with full admin privileges
- Demo User: `user@demo.com` with standard user privileges

## Error Responses

All endpoints return consistent error format:
```json
{
  "error": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

Common HTTP status codes:
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (invalid/missing JWT)
- `403`: Forbidden (insufficient role permissions)  
- `404`: Not Found
- `429`: Too Many Requests (rate limiting)
- `500`: Internal Server Error

### Role-Specific Error Responses
- `403 INSUFFICIENT_ROLE`: User role does not have permission for this operation
- `403 ADMIN_REQUIRED`: Operation requires ADMIN role
- `400 INVALID_ROLE_PROMOTION`: Cannot promote user to admin (only admins can promote)