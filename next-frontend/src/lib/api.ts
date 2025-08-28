// API functions for points operations
// Updated to match Spring Boot backend endpoints

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// Helper function to get auth headers
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` })
  };
};

// Helper function to get stored JWT token
export const getStoredToken = (): string | null => {
  if (typeof window === 'undefined') {
    return null; // Server-side rendering
  }
  return localStorage.getItem('jwtToken');
};

export const pointsAPI = {
  // Get main points balance
  getMainPoints: async (): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/points/main`, {
      method: 'GET',
      headers: getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // Get sub points balance
  getSubPoints: async (): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/points/sub`, {
      method: 'GET',
      headers: getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // Get complete balance info
  getBalance: async (): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/points/balance`, {
      method: 'GET',
      headers: getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // Convert sub points to main points
  convertSubToMain: async (subPoints: number): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/points/convert-sub-to-main`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ subPoints })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // Earn demo points (for testing purposes) - calls admin endpoint
  earnDemoPoints: async (pointType: 'MAIN' | 'SUB', amount: number): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/admin/grant-points`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ 
        pointType, 
        amount,
        description: `Demo ${pointType} points earned via frontend`
      })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }
};

export const authAPI = {
  // Get current user info
  getCurrentUser: async (): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/auth/user`, {
      method: 'GET',
      headers: getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },


  // Google OAuth login
  googleLogin: async (googleToken: string): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/auth/google`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ token: googleToken })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // Demo login for testing
  demoLogin: async (userType: 'admin' | 'user'): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/auth/demo`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ userType })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // Sign out (clear token)
  signOut: (): void => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('jwtToken');
      localStorage.removeItem('token');
    }
  },

  // Validate token
  validateToken: async (token: string): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/auth/validate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ token })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // Refresh token
  refreshToken: async (): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }
};

// Token exchange functionality - working with zkSync backend
export const exchangeAPI = {
  // Exchange main points for governance tokens (10:1 fixed rate)
  pointsToTokens: async (mainPoints: number): Promise<any> => {
    const response = await fetch(`${API_BASE_URL}/api/zksync/exchange/points-to-tokens`, {
      method: 'POST',
      headers: getAuthHeaders()
      // No body needed - backend uses fixed 10:1 exchange rate
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // Legacy function name for backward compatibility
  exchangePointsForTokens: async (mainPoints: number): Promise<any> => {
    return exchangeAPI.pointsToTokens(mainPoints);
  },

  // Get exchange rate (currently fixed at 10:1)
  getExchangeRate: async (): Promise<any> => {
    return {
      success: true,
      mainPointsPerToken: 10,
      rate: '10:1',
      description: 'Fixed exchange rate: 10 main points = 1 governance token'
    };
  }
};
