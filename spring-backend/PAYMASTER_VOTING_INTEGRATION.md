# zkSync Era Paymaster Voting Integration

## Overview

This document explains how the voting system has been updated to use the zkSync Era paymaster service for gasless transactions.

## What Changed

### 1. Updated SmartContractProposalService
- **Proposal Creation**: Now uses `ZkSyncEraPaymasterService.sendGaslessTransaction()`
- **Voting**: Now uses `ZkSyncEraPaymasterService.sendGaslessTransaction()`
- **Gas Fees**: Paid by the paymaster contract owner (`0x5a394A0bb24361da49cE1B10df99CDBDcF7Bb4c1`)

### 2. New Dependencies
- Added `ZkSyncEraPaymasterService` as a dependency
- Removed old `RawTransactionManager` approach
- Unified gasless transaction handling

## How It Works

### Proposal Creation
1. User creates proposal → Backend calls smart contract
2. Paymaster intercepts transaction → Pays gas fees
3. Proposal created on-chain → Permanent record

### Voting
1. User votes → Backend calls smart contract
2. Paymaster intercepts transaction → Pays gas fees  
3. Vote recorded on-chain → Permanent record

## Testing Endpoints

### 1. Health Check
```bash
GET /api/votes/paymaster-health
```
Verifies paymaster service is working correctly.

### 2. Test Paymaster Voting
```bash
POST /api/votes/test-paymaster-vote
{
  "proposalId": 1,
  "userGoogleId": "your-google-id",
  "support": true
}
```
Tests the new paymaster voting integration.

### 3. Regular Voting
```bash
POST /api/votes/vote
{
  "proposalId": 1,
  "userGoogleId": "your-google-id", 
  "support": true
}
```
Uses the updated paymaster service for gasless voting.

## Configuration

### Required Environment Variables
```bash
ZKSYNC_RPC_URL=https://sepolia.era.zksync.dev
PAYMASTER_ADDRESS=0x10219E515c3955916d79A1aC614B86187f0872BC
GOVERNANCE_TOKEN_ADDRESS=0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e
ZKSYNC_OWNER_PRIVATE_KEY=your-owner-private-key
```

## Benefits

✅ **Gasless Experience**: Users don't pay gas fees  
✅ **Unified System**: Same paymaster for proposals and voting  
✅ **Cost Control**: You manage all gas expenses  
✅ **On-chain Transparency**: All votes permanently recorded  
✅ **Better UX**: Seamless voting without wallet setup  

## Verification

### On zkSync Era Explorer
- Check transaction history for your GovernanceToken contract
- Look for `vote()` function calls
- Verify `VoteCast` events are emitted
- Confirm gas fees are paid by paymaster owner

### In Your Database
- Votes should be recorded with transaction hashes
- Voting power should be updated
- Proposal vote counts should increment

## Troubleshooting

### Common Issues
1. **Paymaster not working**: Check configuration and RPC connection
2. **Transaction failures**: Verify user has sufficient voting power
3. **Gas estimation errors**: Check contract state and parameters

### Debug Steps
1. Check `/api/votes/paymaster-health` endpoint
2. Verify transaction on zkSync Era explorer
3. Check application logs for detailed error messages
4. Confirm paymaster contract has sufficient ETH balance

## Next Steps

1. **Test the integration** with the new endpoints
2. **Verify on-chain voting** works correctly
3. **Monitor gas costs** and paymaster balance
4. **Update frontend** to use new voting flow
5. **Deploy to production** when testing is complete
