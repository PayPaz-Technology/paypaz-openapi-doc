# PayPaz Webhook Integration Guide

## 1. Overview

The PayPaz Webhook system delivers real-time notifications for account-related events, such as successful transactions and withdrawal status updates. This guide explains how to configure and integrate PayPaz webhooks.

## 2. Supported Webhook Events

The system supports the following event types:

- `transaction.deposit.succeeded`: Deposit succeeded
- `transaction.withdrawal.succeeded`: Withdrawal succeeded
- `transaction.withdrawal.failed`: Withdrawal failed
- `transaction.payinorder.expired`: Payment order expired
- `transaction.payinorder.underpaid`: Payment order underpaid
- `transaction.payinorder.completed`: Payment order completed

## 3. Webhook Configuration

### 3.1 Configuration Parameters

When configuring a webhook, provide the following:

- **URL**: HTTPS endpoint that receives webhook notifications
- **Event types**: Events to subscribe to
- **Status**: Enable or disable the webhook

### 3.2 Security Configuration

Each webhook configuration can be associated with a key pair used to verify request authenticity:

- **Public key (PubSecretKey)**: Key used to verify signatures
- **Private key (PrivSecretKey)**: Key used to generate signatures (used only within the PayPaz system)

## 4. Webhook Request Format

### 4.1 Request Headers

Each webhook request includes the following headers:

- `Content-Type: application/json`
- `PAYPAZ-WEBHOOK-SIGN`: HMAC-SHA256 signature
- `PAYPAZ-WEBHOOK-TIMESTAMP`: Timestamp (milliseconds)

### 4.2 Signature Verification

Signature generation rules:
1. Concatenate the request body (payload) and timestamp with a pipe (`|`): `payload|timestamp`
2. Sign the string using HMAC-SHA256 and your private key
3. Base64-encode the result

Signature verification example (Java):
```java
private boolean verifySignature(String payload, String timestamp, String signature, String secret) {
    try {
        String message = payload + "|" + timestamp;
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKey);
        byte[] hash = sha256HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
        String computedSignature = Base64.getEncoder().encodeToString(hash);
        return computedSignature.equals(signature);
    } catch (Exception e) {
        return false;
    }
}
```

### 4.3 Request Body Examples

#### Deposit Succeeded Event
```json
{
  "eventType": "transaction.deposit.succeeded",
  "data": {
    "id": "1972615389021605888",
    "fee": "0.00019999",
    "txId": "L5faafcf23774bbe5f0603c93679b545",
    "status": 1,
    "userId": "449267154",
    "orderNo": "6af6340e-553b-428e-9d3b-16398aab5cf0",
    "tokenId": "USDC",
    "quantity": "0.019999",
    "createdAt": "1759143132812",
    "netAmount": "0.01979901",
    "subUserId": "449267154253404897",
    "updatedAt": null,
    "fromAddress": "",
    "chainId": "BNB",
    "walletAddress": "0xc7b530889238fbba211683bb5632b56d60c51270"
  }
}
```
### Deposit Succeeded Event — `data` Properties

|Name|Type|Required|Constraint|Description|
|---|---|---|---|---|
|id|string|false|none|Deposit order ID|
|orderNo|string|false|none|Order number|
|tokenId|string|false|none|Token ID|
|chainId|string|false|none|On-chain token ID|
|quantity|string|false|none|Deposit quantity|
|fee|string|false|none|Fee|
|netAmount|string|false|none|Net credited amount|
|fromAddress|string|false|none|Source address|
|walletAddress|string|false|none|Wallet address|
|txId|string|false|none|Transaction ID (blockchain transaction hash)|
|status|integer(int32)|false|none|Deposit status: 1 = completed, credited|
|createdAt|string|false|none|Created at (millisecond timestamp)|
|updatedAt|string|false|none|Updated at (millisecond timestamp)|
|userId|string|false|none|User ID (master account)|
|subUserId|string|false|none|Sub-user ID|

#### Withdrawal Succeeded Event
```json
{
  "eventType": "transaction.withdrawal.succeeded",
  "data": {
    "id": "1980540667704803328",
    "txId": "Lcb5fd92ed2c7427acfe4b22596d9935",
    "userId": "449267154",
    "address": "0x2c90a96735d851c6728fb6949264b88198b5dc6c",
    "tokenId": "USDC",
    "createdAt": "1761032666380",
    "subUserId": "449267154692704658",
    "updatedAt": "1761032677792",
    "arriveTime": "1761032677796",
    "statusInfo": 2,
    "chainId": "BNB",
    "totalQuantity": "0.0001",
    "transactionId": "c17976f5-e865-4924-8ed8-c47f089f54ee",
    "arriveQuantity": "0.000099",
    "clientWithdrawalId": "1212121212122222222"
  }
}
```
### Withdrawal Succeeded Event — `data` Properties

|Name|Type|Required|Constraint|Description|
|---|---|---|---|---|
|id|string|false|none|Platform-generated ID|
|clientWithdrawalId|string|false|none|Client order ID|
|transactionId|string|false|none|Order ID|
|tokenId|string|false|none|tokenId|
|chainId|string|false|none|Chain token ID|
|userId|string|false|none|none|
|subUserId|string|false|none|Enterprise sub-user ID|
|address|string|false|none|User withdrawal address|
|platformFee|string|false|none|Fee|
|arriveQuantity|string|false|none|Actual withdrawal amount received|
|statusInfo|integer(int32)|false|none|1 = processing, 2 = succeeded, 3 = failed|
|createdAt|string|false|none|Created at|
|updatedAt|string|false|none|Updated at|
|arriveTime|string|false|none|Withdrawal arrival time|
|totalQuantity|string|false|none|Withdrawal amount entered by the user|
|txId|string|false|none|transaction hash|

#### Payment Order Expired Event
```json
{
  "eventType": "transaction.payinorder.expired",
  "data": {
    "id": 123456789,
    "payOrderNo": "P202601010001",
    "orderStatus": 4,
    "orderSource": 1,
    "tokenId": "USDT",
    "chainId": "TRON",
    "walletAddress": "TXxxx...",
    "payAmount": "10",
    "amount": "0",
    "fee": "0",
    "netAmount": "0",
    "arriveTime": 0,
    "fromAddress": null,
    "txId": null,
    "expireSeconds": 3600,
    "userId": 123456,
    "subUserId": 789012,
    "createdAt": 1700000000000,
    "updatedAt": 1700003600000
  }
}
```

#### Payment Order Completed Event
```json
{
  "eventType": "transaction.payinorder.completed",
  "data": {
    "id": 123456789,
    "payOrderNo": "P202601010001",
    "orderStatus": 3,
    "orderSource": 1,
    "tokenId": "USDT",
    "chainId": "TRON",
    "walletAddress": "TXxxx...",
    "payAmount": "10",
    "amount": "10.5",
    "fee": "0.1",
    "netAmount": "10.4",
    "arriveTime": 1700003600000,
    "fromAddress": "TFromxxx...",
    "txId": "0xabc123...",
    "expireSeconds": 3600,
    "userId": 123456,
    "subUserId": 789012,
    "createdAt": 1700000000000,
    "updatedAt": 1700003600000
  }
}
```

#### Payment Order Underpaid Event
```json
{
  "eventType": "transaction.payinorder.underpaid",
  "data": {
    "id": 123456789,
    "payOrderNo": "P202601010001",
    "orderStatus": 2,
    "orderSource": 1,
    "tokenId": "USDT",
    "chainId": "TRON",
    "walletAddress": "TXxxx...",
    "payAmount": "10",
    "amount": "5",
    "fee": "0",
    "netAmount": "0",
    "arriveTime": 0,
    "fromAddress": "TFromxxx...",
    "txId": "0xdef456...",
    "expireSeconds": 3600,
    "userId": 123456,
    "subUserId": 789012,
    "createdAt": 1700000000000,
    "updatedAt": 1700001800000
  }
}
```

### Payment Order Events — `data` Properties

|Name|Type|Required|Constraint|Description|
|---|---|---|---|---|
|id|integer(int64)|false|none|ID|
|payOrderNo|string|false|none|Payment order number|
|orderStatus|integer(int32)|false|none|Order status (1=processing, 2=underpaid, 3=completed, 4=expired)|
|orderSource|integer(int32)|false|none|Order source (1=broker, 2=paypaz)|
|tokenId|string|false|none|tokenId|
|chainId|string|false|none|chainId|
|walletAddress|string|false|none|Wallet address|
|payAmount|string|false|none|Payment amount|
|amount|string|false|none|On-chain amount|
|fee|string|false|none|Fee|
|netAmount|string|false|none|Net amount received (after fees)|
|arriveTime|string|false|none|Arrival time (millisecond timestamp)|
|fromAddress|string|false|none|Source address|
|txId|string|false|none|Transaction ID (blockchain transaction hash)|
|expireSeconds|integer(int32)|false|none|Validity period (seconds)|
|userId|integer(int64)|false|none|User ID (master account)|
|subUserId|integer(int64)|false|none|Sub-user ID|
|createdAt|string|false|none|Created at (millisecond timestamp)|
|updatedAt|string|false|none|Updated at (millisecond timestamp)|

## 5. Retry Mechanism

### 5.1 Retry Policy

- The system automatically retries failed deliveries up to 7 times
- Retry intervals use exponential backoff
- `retryCount` increments after each retry

### 5.2 Response Requirements

**HTTP response body:** Return the string `success` to acknowledge successful receipt. Any other response body indicates failure, and the system will retry according to the retry policy.

## 6. Best Practices

1. **Verify signatures**: Always verify request signatures to ensure requests originate from PayPaz
2. **Idempotency**: Implement idempotent handlers to avoid processing the same event twice
3. **Timeouts**: Respond to webhook requests within 5 seconds when possible
4. **Logging**: Log all received webhook requests and processing results
5. **Error handling**: Implement robust error handling to maintain system stability

## 7. Testing and Debugging

1. Use the staging environment for integration testing
2. Review webhook event logs to monitor delivery status
3. Monitor retry counts and failed events

## 8. Troubleshooting

### Common Issues

1. **Signature verification failed**
    - Verify that keys are configured correctly
    - Confirm your signature algorithm implementation matches the specification

2. **Webhook not received**
    - Check network connectivity and firewall rules
    - Verify that the webhook URL is correct and publicly accessible

3. **Processing timeout**
    - Ensure your service can respond within 5 seconds
    - Consider processing long-running tasks asynchronously

## 9. Support

If you need assistance, contact the PayPaz support team and provide the following information:
- Event ID (`eventLogId`)
- Timestamp
- Relevant error messages
- Your server logs (if applicable)

