/**
 * Utility functions for token formatting and conversion
 */

/**
 * Format token balance from wei to human readable format
 * @param tokenBalanceWei Token balance in wei (e.g., 5000000000000000000)
 * @returns Formatted token amount (e.g., "5")
 */
export function formatTokenBalance(tokenBalanceWei: number | string): string {
  if (!tokenBalanceWei) return "0";
  
  const balance = typeof tokenBalanceWei === 'string' 
    ? parseFloat(tokenBalanceWei) 
    : tokenBalanceWei;
  
  // Convert from wei (18 decimals) to tokens
  const tokens = balance / Math.pow(10, 18);
  
  // Format to avoid scientific notation and unnecessary decimals
  if (tokens >= 1) {
    return tokens.toFixed(0); // Show whole numbers for >= 1 token
  } else if (tokens > 0) {
    return tokens.toFixed(4); // Show up to 4 decimals for fractional tokens
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