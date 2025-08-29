# zkSync Sepolia Transaction Reading Guide

This document shows how to read transaction details from zkSync Sepolia blockchain using curl commands.

## Configuration
- **RPC Endpoint**: `https://sepolia.era.zksync.dev`
- **Chain ID**: 300
- **Governance Token Contract**: `0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e`

## 1. Get Transaction Details

```bash
curl -X POST https://sepolia.era.zksync.dev \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "eth_getTransactionByHash",
    "params": ["TX_HASH_HERE"],
    "id": 1
  }'
```

**Example Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "hash": "0xb41dd4d8f3cff5535007ccb8ccaf5f95a075a052ae70256ded9a10a67919df2f",
    "from": "0x5a394a0bb24361da49ce1b10df99cdbdcf7bb4c1",
    "to": "0x21341e1672ee0a7ddadb5d7bff72f93c8e81ef3e",
    "input": "0x35facf78...",
    "blockNumber": "0x56fb06"
  }
}
```

## 2. Get Transaction Receipt (Success/Failure Status)

```bash
curl -X POST https://sepolia.era.zksync.dev \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "eth_getTransactionReceipt",
    "params": ["TX_HASH_HERE"],
    "id": 1
  }'
```

**Key Fields:**
- `status`: "0x1" = Success, "0x0" = Failed
- `logs`: Contains emitted events
- `gasUsed`: Gas consumed

## 3. Decode Proposal Creation Transaction

For our governance contract, proposal creation transactions have:

### Function Signature
- **createProposal**: `0x35facf78`

### Input Data Structure
```
0x35facf78  // Function selector (4 bytes)
0000000000000000000000000000000000000000000000000000000000000040  // Description offset
0000000000000000000000000000000000000000000000000000000068bb4f14  // Deadline timestamp
0000000000000000000000000000000000000000000000000000000000000009  // Description length
7765656b20746573740000000000000000000000000000000000000000000000  // Description (hex)
```

### Decoding Commands

**Extract deadline timestamp:**
```bash
echo "0x68bb4f14" | python3 -c "import sys; print(int(sys.stdin.read().strip(), 16))"
# Output: 1757105940
```

**Convert timestamp to date:**
```bash
date -r 1757105940
TZ='Asia/Seoul' date -r 1757105940
```

**Extract description:**
```bash
echo "7765656b2074657374" | xxd -r -p
# Output: week test
```

## 4. Decode Events from Receipt

### Proposal Created Event
- **Event Signature**: `0x98120a6aaa04295520ab4e01c6c1235dd316e822cc9ff31db7b3f197366d18bd`
- **Topic 2**: Proposal ID (hex to decimal)

**Example:**
```bash
echo "0x000000000000000000000000000000000000000000000000000000000000002c" | python3 -c "import sys; print(int(sys.stdin.read().strip(), 16))"
# Output: 44 (Proposal ID)
```

## 5. Example: Complete Transaction Analysis

**Transaction Hash**: `0xb41dd4d8f3cff5535007ccb8ccaf5f95a075a052ae70256ded9a10a67919df2f`

### Results:
- **Status**: ✅ SUCCESS
- **Function**: createProposal
- **Proposal ID**: 44
- **Description**: "week test"
- **Deadline**: 1757105940 → Sep 6, 2025, 05:59:00 KST
- **Gas Used**: 0x35f31 (221,041 gas)

## 6. Useful Helper Functions

### Convert hex timestamp to readable date
```bash
convert_timestamp() {
    local hex_timestamp=$1
    local decimal=$(python3 -c "print(int('$hex_timestamp', 16))")
    echo "Decimal: $decimal"
    echo "UTC: $(date -r $decimal)"
    echo "KST: $(TZ='Asia/Seoul' date -r $decimal)"
}

# Usage: convert_timestamp "0x68bb4f14"
```

### Extract description from hex
```bash
decode_hex_string() {
    local hex_string=$1
    echo "$hex_string" | xxd -r -p
}

# Usage: decode_hex_string "7765656b2074657374"
```

## 7. Contract Addresses Reference

- **Governance Token**: `0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e`
- **Paymaster**: `0x10219E515c3955916d79A1aC614B86187f0872BC`

## 8. Common Use Cases

1. **Verify proposal creation**: Check transaction status and decode parameters
2. **Debug timezone issues**: Compare blockchain timestamp with database time
3. **Monitor gas usage**: Track transaction costs
4. **Audit proposal details**: Verify description and deadline accuracy

This method works for any zkSync Sepolia transaction and provides complete blockchain verification of our governance system.