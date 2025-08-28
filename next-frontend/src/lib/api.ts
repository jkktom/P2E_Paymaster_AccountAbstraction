// API functions for points operations
// Updated to match Spring Boot backend endpoints

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

// Helper function to get auth headers
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` })
  };
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

// Note: Exchange functionality would be implemented later
// when backend endpoints are available
export const exchangeAPI = {
  // Placeholder for future implementation
  exchangePointsForTokens: async (mainPoints: number): Promise<any> => {
    throw new Error('Exchange functionality not yet implemented in backend');
  },

  // Placeholder for future implementation  
  getExchangeRate: async (): Promise<any> => {
    throw new Error('Exchange rate endpoint not yet implemented in backend');
  }
};
