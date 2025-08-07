# Frontend Application Documentation

This file contains detailed frontend specifications for the Next.js TypeScript application.

## Application Structure

### Pages/Routes
```
/                    # Landing page with Google login
/dashboard           # Main user dashboard  
/points              # Point management & history
/exchange            # Token exchange interface
/governance          # Voting interface (future feature)
/admin               # Admin panel for testing
```

### Key Components

#### AuthProvider
- NextAuth.js integration for Google OAuth
- JWT token management
- User session state
- Automatic token refresh

#### PointsCard
- Display main/sub point balances
- Real-time balance updates
- Conversion rate display
- Interactive conversion buttons

#### ExchangeForm  
- Points to token exchange interface
- Real-time conversion preview (10:1 ratio)
- Transaction confirmation modal
- Gas sponsorship indication

#### TransactionHistory
- Tabbed interface (Points/Exchange/Wallet)
- Infinite scroll for large datasets
- Search and filter capabilities
- Export functionality

#### AlchemyWalletConnect
- Embedded smart wallet display
- Wallet address copying
- Token balance display
- Transaction status indicator

#### GovernanceVoting
- Future governance token voting interface
- Proposal list and details
- Voting power display
- Vote casting functionality

#### AdminPanel
- User management interface
- Point granting form
- System statistics dashboard
- Testing utilities

### Custom Hooks

#### useAuth()
```typescript
const {
  user,           // Current user object
  isLoading,      // Authentication loading state
  signIn,         // Google sign in function
  signOut,        // Sign out function
  token           // Current JWT token
} = useAuth();
```

#### usePoints()
```typescript
const {
  mainPoints,     // Main point balance object
  subPoints,      // Sub point balance object
  loading,        // Loading state
  earnPoints,     // Function to earn points
  convertPoints,  // Function to convert sub to main
  refresh         // Function to refresh balances
} = usePoints();
```

#### useWallet()
```typescript
const {
  address,        // Smart wallet address
  balance,        // Token balance
  isConnected,    // Connection status
  transactions,   // Recent transactions
  sendTransaction // Function to send transaction
} = useWallet();
```

#### useExchange()
```typescript
const {
  exchangeRate,   // Current exchange rate (10:1)
  exchangeTokens, // Function to exchange points
  history,        // Exchange history
  loading,        // Transaction loading state
  estimateGas     // Function to estimate gas (always 0 due to AA)
} = useExchange();
```

## UI/UX Design Principles

### Design System
- **Colors**: Modern blue/green palette for trust
- **Typography**: Clean, readable fonts (Inter/Poppins)
- **Layout**: Card-based design with clear hierarchy  
- **Responsive**: Mobile-first approach

### User Experience Flow
1. **Landing**: Clean Google login button
2. **Onboarding**: Brief explanation of points/tokens  
3. **Dashboard**: Overview of all balances and actions
4. **Actions**: Clear CTAs for point earning and exchange
5. **History**: Comprehensive transaction tracking

### Accessibility
- ARIA labels on all interactive elements
- Keyboard navigation support
- High contrast mode compatibility
- Screen reader optimization

## State Management Strategy

### React Context + Custom Hooks
- Minimal state management overhead
- Type-safe state access
- Efficient re-rendering optimization
- Easy testing and debugging

### Data Fetching
- SWR for efficient API data fetching
- Automatic background revalidation
- Optimistic UI updates
- Error boundary implementation

## Performance Optimizations

### Code Splitting
- Route-based splitting for faster initial load
- Component lazy loading for heavy features
- Dynamic imports for admin features

### Caching Strategy
- API response caching with SWR
- Image optimization with Next.js
- Static generation for landing pages
- Service worker for offline capability

## Testing Strategy

### Component Testing
- Jest + React Testing Library
- Component behavior verification
- User interaction simulation
- Accessibility testing

### Integration Testing
- API integration mocks
- Authentication flow testing
- Complete user journey validation

### E2E Testing
- Playwright for cross-browser testing
- Critical user path verification
- Mobile responsive testing