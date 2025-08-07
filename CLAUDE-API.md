# API Documentation

This file contains detailed API specifications for the blockchain goods exchange service.

## Authentication Endpoints

### POST /api/auth/google
- **Body**: `{ googleToken }`
- **Response**: `{ jwtToken, user, smartWalletAddress }`
- **Description**: Authenticates user with Google OAuth, creates account and smart wallet

### POST /api/auth/refresh
- **Body**: `{ refreshToken }`
- **Response**: `{ jwtToken }`
- **Description**: Refreshes expired JWT token

## Point Management Endpoints

### GET /api/points/main
- **Headers**: `Authorization: Bearer {jwt}`
- **Response**: `{ balance, totalEarned, pointsToToken }`
- **Description**: Get user's main point account details

### GET /api/points/sub
- **Headers**: `Authorization: Bearer {jwt}`
- **Response**: `{ balance, totalEarned, subToMain }`
- **Description**: Get user's sub point account details

### POST /api/points/earn
- **Body**: `{ pointType, amount, source, description }`
- **Response**: `{ success, newBalance }`
- **Description**: Award points to user (admin or system triggered)

### POST /api/points/convert-sub-to-main
- **Body**: `{ subPoints }`
- **Response**: `{ mainPointsReceived, conversionRate }`
- **Description**: Convert sub points to main points

## Token Exchange Endpoints

### POST /api/exchange/points-to-tokens
- **Body**: `{ mainPoints }`
- **Response**: `{ txHash, tokensReceived, status }`
- **Description**: Exchange main points for governance tokens (10:1 ratio)

### GET /api/exchange/history
- **Headers**: `Authorization: Bearer {jwt}`
- **Response**: `[{ pointsSpent, tokensReceived, txHash, date }]`
- **Description**: Get user's token exchange history

## Transaction History Endpoints

### GET /api/transactions/points
- **Headers**: `Authorization: Bearer {jwt}`
- **Response**: `[{ type, amount, source, description, date }]`
- **Description**: Get user's point earning transaction history

### GET /api/transactions/wallet
- **Headers**: `Authorization: Bearer {jwt}`
- **Response**: `[{ type, amount, txHash, gasSponsored, date }]`
- **Description**: Get user's blockchain transaction history

## Admin Endpoints (Testing)

### POST /api/admin/grant-points
- **Body**: `{ userId, pointType, amount, description }`
- **Response**: `{ success }`
- **Description**: Grant points to specific user for testing

### GET /api/admin/users
- **Headers**: `Authorization: Bearer {adminJwt}`
- **Response**: `[{ id, name, email, pointBalances }]`
- **Description**: List all users and their point balances

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
- `403`: Forbidden (insufficient permissions)  
- `404`: Not Found
- `429`: Too Many Requests (rate limiting)
- `500`: Internal Server Error