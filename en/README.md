# Paypaz OpenAPI Integration Guide

This document provides detailed information about the Paypaz Broker OpenAPI. These APIs allow external systems to integrate with the Paypaz Broker platform for managing sub-users, payment orders, assets, deposits, and withdrawals.

---

## Basic Information

### REST API Base URLs

| Environment | Description | Base URL | Egress IP |
|------------|-------------|----------|-----------|
| Staging | For integration testing | [`https://open-api.paypaz.dev`](https://openapi.paypaz.dev) | 16.163.219.116 |
| Production | Live environment | [`https://open-api.paypaz.com`](https://openapi.paypaz.com) | 18.167.66.134 |

---
## API Keys for Authentication
Paypaz generates an API_Key and API_Secret pair in the admin console. Store them securely.
### Common Headers

All endpoints that require signature authentication must include the following HTTP headers:

- `PAYPAZ-ACCESS-KEY`: Your API_Key (string)
- `PAYPAZ-ACCESS-SIGN`: HMAC SHA256 hash of the signing payload, Base64-encoded (see Signing below)
- `PAYPAZ-ACCESS-TIMESTAMP`: Request time (UTC, millisecond timestamp)
- `PAYPAZ-ACCESS-RECV-WINDOW`: Validity window in milliseconds; default `20000`; used to prevent replay attacks; maximum `60000`

All requests should include `Content-Type: application/json` and a valid JSON body when applicable.

### How to Generate the Signature

Steps to generate `PAYPAZ-ACCESS-SIGN`:

1. Build the signing string: `timestamp + method + RECV-WINDOW + requestPath + body`
   - `timestamp`: Same millisecond value as `PAYPAZ-ACCESS-TIMESTAMP`
   - `method`: HTTP method in uppercase (GET/POST)
   - `RECV-WINDOW`: Same value as `PAYPAZ-ACCESS-RECV-WINDOW`
   - `requestPath`: Request path without sorting, e.g. `/t-api/broker-openapi/v1/op/openapi/withdrawalOrderInfo?clientWithdrawalId=d2d640dc-db20-43c3-967a-9aa3b5e55899`
   - `body`: Request body as a string; omit for GET requests with no body; no sorting required

2. Compute HMAC SHA256 using your API Secret Key

3. Base64-encode the result to obtain the final signature

**PAYPAZ-ACCESS-SIGN signing example (JavaScript, GET):**
```javascript
const timestamp = Date.now().toString();
const method = 'GET';
const recvWindow = '10000';
const requestPath = '/t-api/openapi/v1/op/openapi/withdrawalOrderInfo?clientWithdrawalId=d2d640dc-db20-43c3-967a-9aa3b5e55899';
const body = ''; // GET requests usually have no body; for POST, use the JSON string

const signatureString = timestamp + method + recvWindow + requestPath + body;
const signature = CryptoJS.enc.Base64.stringify(
  CryptoJS.HmacSHA256(signatureString, secretKey)
);
```

**PAYPAZ-ACCESS-SIGN signing example (Java, GET):**
```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;
private static String generateHmacSha256Signature(String data, String secret) throws Exception{

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

}
public static void main(String[] args) throws Exception {

   String timestamp = Long.toString(System.currentTimeMillis());
   String method = "GET";
   String recvWindow = "20000";
   String clientWithdrawalId = "WKxXXnkaD0luIGvnZVrglg7UALaYDPTLiQdEbYvUZjL9qI4ekEqW";
   String requestPath = "/t-api/openapi/v1/op/openapi/withdrawalOrderInfo?clientWithdrawalId=" + clientWithdrawalId;
   String bodyString = "";
   
   // Build signature string
   String signatureString = timestamp + method + recvWindow + requestPath + bodyString;
   System.out.println("signatureString: " + signatureString);
   
   // Assume secretKey is defined
   String secretKey = "your_secret_key_here"; // Replace with actual key
   System.out.println("secretKey: " + secretKey);
   
   // Generate signature
   String signature = generateHmacSha256Signature(signatureString, secretKey);
}
```



**PAYPAZ-ACCESS-SIGN signing example (JavaScript, POST):**
```javascript
const timestamp = Date.now().toString();
const method = 'POST';
const recvWindow = '10000';
const requestPath = '/t-api/openapi/v1/op/openapi/createWithdrawal';
const body = {
    "subUid": 123456789,
    "tokenId": "USDT",
    "chainId": "TRON",
    "address": "0x1234567890abcdef1234567890abcdef12345678",
    "amount": 0.01,
    "clientWithdrawalId": "client12345678901234"
};

// Convert body to JSON string for the signature
const bodyString = JSON.stringify(body);

const signatureString = timestamp + method + recvWindow + requestPath + bodyString;
const signature = CryptoJS.enc.Base64.stringify(
  CryptoJS.HmacSHA256(signatureString, secretKey)
);
```

**PAYPAZ-ACCESS-SIGN signing example (Java, POST):**
```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
private static String generateHmacSha256Signature(String data, String secret) throws Exception{
   Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
   SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
   sha256_HMAC.init(secret_key);
    byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
   return Base64.getEncoder().encodeToString(hash);

}
public static void main(String[] args) throws Exception {

       String timestamp = Long.toString(System.currentTimeMillis());
        String method = "POST";
        String recvWindow = "10000";
        String requestPath = "/t-api/openapi/v1/op/openapi/createWithdrawal";
        
        // Build JSON object directly
        JSONObject body = new JSONObject();
        body.put("subUid", 123456789);
        body.put("tokenId", "USDT");
        body.put("chainId", "TRON");
        body.put("address", "0x1234567890abcdef1234567890abcdef12345678");
        body.put("amount", 0.01);
        body.put("clientWithdrawalId", "client12345678901234");
        
        // Get JSON string
        String bodyString = body.toString();
        
        // Build signing string
        String signatureString = timestamp + method + recvWindow + requestPath + bodyString;
        System.out.println("Signature String: " + signatureString);

        // Replace with your actual secret key
        String secretKey = "your_secret_key_here"; 
        
        // Generate signature
        String signature = generateHmacSha256Signature(signatureString, secretKey);
    
}
```


## HTTP Request Examples
**GET example:**
```javascript
GET /t-api/openapi/v1/op/openapi/withdrawalOrderInfo?clientWithdrawalId=d2d640dc-db20-43c3-967a-9aa3b5e5589 HTTP/1.1
Host: open-api.paypaz.com
-H 'PAYPAZ-ACCESS-KEY: XXXXXXXXXX' \
-H 'PAYPAZ-ACCESS-SIGN: xxxxxxxxxxxxxxxxxx' \
-H 'PAYPAZ-ACCESS-TIMESTAMP: 1658384431891' \
-H 'PAYPAZ-ACCESS-RECV-WINDOW: 5000' \
-H 'Content-Type: application/json' 
```

**POST example:**
```javascript
POST /t-api/openapi/v1/op/openapi/createWithdrawal HTTP/1.1
Host: open-api.paypaz.com
-H 'PAYPAZ-ACCESS-KEY: XXXXXXXXXX' \
-H 'PAYPAZ-ACCESS-SIGN: xxxxxxxxxxxxxxxxxx' \
-H 'PAYPAZ-ACCESS-TIMESTAMP: 1658384431891' \
-H 'PAYPAZ-ACCESS-RECV-WINDOW: 5000' \
-H 'Content-Type: application/json' \
-d '{
    "subUid": 123456789,
    "tokenId": "TBSC_BNB",
    "address": "0x1234567890abcdef1234567890abcdef12345678",
    "amount": 0.01,
    "clientWithdrawalId": "client12345678901234"
}'
```

## Response Format

All API responses follow a standard envelope:

```json
{
  "code": 200,       // Status code; 200 means success
  "msg": "success",  // Message
  "data": {}         // Response payload; structure varies by endpoint
}
```

### HTTP Status Codes

| HTTP Status Code | Description |
|------------------|-------------|
| 200 | Request succeeded |
| 400 | Invalid request parameters |
| 404 | Resource not found |
| 500 | Internal server error |

### Business Error Codes

When processing fails, `code` contains a specific error code and `msg` contains a description.

| Error Code | Description |
|------------|-------------|
| 429100000 | Rate limit exceeded |
| 500105001 | Required authentication information is missing |
| 500105002 | Invalid API Key |
| 500105003 | Signature verification failed |
| 500105004 | Request timestamp has expired |
| 500105005 | Invalid timestamp format |
| 500105006 | Failed to generate signature |
| 500105007 | Sub-user does not exist |
| 500105008 | Query parameters cannot all be empty |
| 500105009 | Scope permissions are not configured correctly |
| 500105010 | No permission to access this API |
| 500105011 | IP is not on the whitelist |
| 500105012 | Withdrawal token does not exist |
| 500105013 | Invalid withdrawal address |
| 500105014 | Withdrawal token configuration does not exist |
| 500105015 | Withdrawals are not allowed |
| 500105016 | Withdrawal amount is below the minimum |
| 500105017 | User is not allowed to withdraw |
| 500105018 | Balance does not exist |
| 500105019 | Insufficient balance |
| 500105020 | Daily withdrawal limit exceeded |
| 500105021 | Withdrawal amount is too small |
| 500105022 | Withdrawal failed |
| 500105023 | Withdrawal address is not on the whitelist |
| 500105024 | OpenAPI system error |
| 500105025 | Withdrawal order ID already exists |
| 500105026 | Deposit token does not exist |
| 500105027 | Deposit token configuration does not exist |
| 500105028 | Deposits are not allowed |
| 500105029 | Failed to create balance record |
| 500105030 | Failed to create wallet address |
| 500105031 | Withdrawal amount precision exceeds the limit |
| 500105033 | Payment order number already exists |
| 500105034 | Payment order amount precision exceeds the limit |
| 500105035 | Payment amount is below the minimum deposit amount |
| 500105036 | Start time and end time cannot be empty |
| 500105037 | Start time cannot be greater than end time |
| 500105038 | Time range cannot exceed 30 days |
| 500105039 | Invalid time format; use a 13-digit millisecond timestamp |

## Notes

1. All timestamps are milliseconds since the Unix epoch.
2. Pagination starts at 1 (the first page is `1`).
3. Maximum page size is 100 items.
4. For withdrawals, ensure `clientWithdrawalId` is unique for idempotency.
5. Sub-user operations require that the sub-user belongs to the current API user.

## API Permissions

The following scopes are used:
- `deposit`: Required for deposit and payment order operations
- `withdraw`: Required for withdrawal operations

## Rate Limits

The following endpoints are rate-limited per user ID:

| Endpoint Path | Method | Window | Max Requests | Description |
|---------------|--------|--------|--------------|-------------|
| `/t-api/openapi/v1/op/openapi/depositAddress` | POST | 60s | 120 | Get or create deposit address |
| `/t-api/openapi/v1/op/openapi/createWithdrawal` | POST | 60s | 120 | Create withdrawal order |
| `/t-api/openapi/v1/op/openapi/createPayInOrder` | POST | 60s | 120 | Create payment order |

**Rate limit rules:**
- Limit type: per user ID
- Time window: 60 seconds
- Maximum requests: 120
- Exceeding the limit returns a rate limit error response

**Notes:**
- Counters are tracked independently per user ID
- Implement retries with backoff when you receive a rate limit error
