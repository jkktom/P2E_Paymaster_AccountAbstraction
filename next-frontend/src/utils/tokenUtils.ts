/**
 * Utility functions for token formatting and conversion
 */

/**
 * Format token balance from integer token values to human readable format
 * @param tokenBalance Token balance as integer (e.g., 12)
 * @returns Formatted token amount (e.g., "12")
 */
export function formatTokenBalance(tokenBalance: number | string): string {
  if (!tokenBalance) return "0";
  
  const balance = typeof tokenBalance === 'string' 
    ? parseFloat(tokenBalance) 
    : tokenBalance;
  
  // Since backend now sends integer token values directly, no conversion needed
  // Format to avoid unnecessary decimals
  if (balance >= 1) {
    return balance.toFixed(0); // Show whole numbers for >= 1 token
  } else if (balance > 0) {
    return balance.toFixed(4); // Show up to 4 decimals for fractional tokens
  } else {
    return "0";
  }
}

/**
 * Convert token amount to wei
 * @param tokenAmount Token amount (e.g., 5)
 * @returns Wei amount (e.g., "5000000000000000000")
 */
export function tokenToWei(tokenAmount: number): string {
  return (tokenAmount * Math.pow(10, 18)).toString();
}

/**
 * Convert wei to token amount
 * @param weiAmount Wei amount (e.g., "5000000000000000000")
 * @returns Token amount (e.g., 5)
 */
export function weiToToken(weiAmount: string | number): number {
  const wei = typeof weiAmount === 'string' ? parseFloat(weiAmount) : weiAmount;
  return wei / Math.pow(10, 18);
}