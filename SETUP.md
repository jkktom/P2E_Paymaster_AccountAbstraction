# Google OAuth + API Setup Guide

## 🚀 Quick Start

### 1. Backend Setup (Spring Boot)

#### Environment Variables
Copy `spring-backend/env.example` to `spring-backend/.env` and fill in:

```bash
# Required for Google OAuth
GOOGLE_CLIENT_ID=your-actual-google-client-id
GOOGLE_CLIENT_SECRET=your-actual-google-client-secret

# JWT Secret (already set in application.yml)
JWT_SECRET=K8mP#2vN9qR$5wX@7yZ!4hF&6jL*8nQ%3sT^1uV+0cB

# CORS (for frontend communication)
ALLOWED_ORIGINS=http://localhost:3000
```

#### Get Google OAuth Credentials
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select a project
3. Enable Google+ API
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client IDs"
5. Choose "Web application"
6. Add authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google`
   - `http://localhost:3000/auth/callback`

#### Start Backend
```bash
cd spring-backend
./gradlew bootRun
```

### 2. Frontend Setup (Next.js)

#### Environment Variables
Copy `next-frontend/env.example` to `next-frontend/.env.local` and fill in:

```bash
# API URL
NEXT_PUBLIC_API_URL=http://localhost:8080

# Google OAuth (same as backend)
NEXT_PUBLIC_GOOGLE_CLIENT_ID=your-actual-google-client-id
```

#### Start Frontend
```bash
cd next-frontend
npm install
npm run dev
```

## 🔧 API Endpoints

### Authentication
- `POST /auth/google` - Google OAuth login
- `GET /auth/user` - Get current user profile
- `POST /auth/validate` - Validate JWT token
- `POST /auth/refresh` - Refresh JWT token
- `POST /auth/logout` - Logout

### Points Management
- `GET /api/points/main` - Get main points balance
- `GET /api/points/sub` - Get sub points balance
- `POST /api/points/convert-sub-to-main` - Convert sub to main points

### User Management
- `GET /api/user/profile` - Get user profile
- `GET /api/user/role` - Get user role

## 🔐 How It Works

1. **User clicks Google Sign-In** on frontend
2. **Google returns ID token** to frontend
3. **Frontend sends token** to backend `/auth/google`
4. **Backend validates** Google token and creates JWT
5. **Backend returns JWT** to frontend
6. **Frontend stores JWT** and uses for API calls
7. **Backend validates JWT** on protected endpoints

## 🧪 Testing

### Test Backend
```bash
cd spring-backend
./gradlew test
```

### Test Frontend
```bash
cd next-frontend
npm run test
```

### Manual Testing
1. Start both backend and frontend
2. Open `http://localhost:3000`
3. Click Google Sign-In
4. Complete OAuth flow
5. Verify JWT token is stored
6. Test protected endpoints

## 🚨 Common Issues

### CORS Errors
- Ensure `ALLOWED_ORIGINS` includes your frontend URL
- Check that frontend is making requests to correct backend URL

### Google OAuth Errors
- Verify `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are correct
- Check redirect URIs in Google Cloud Console
- Ensure Google+ API is enabled

### JWT Errors
- Verify `JWT_SECRET` is set and matches between backend and frontend
- Check token expiration settings

## 📱 Next Steps

1. **Complete Google OAuth setup** with real credentials
2. **Test authentication flow** end-to-end
3. **Implement protected routes** in frontend
4. **Add error handling** and loading states
5. **Deploy to staging/production** with proper environment variables
