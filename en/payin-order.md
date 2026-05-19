# Deposit APIs

All endpoints below require signature authentication.

---

### Deposit Integration Flow

1. Call [**Get All Networks**](base-data-api.md#get-all-networks) to retrieve supported chain information.
2. Call [**Get Token Configuration for a Token**](base-data-api.md#get-token-config) to retrieve deposit and withdrawal settings for the token.
3. Deposits support two modes:
   - **Order mode**: Call [**Create Payment Order**](#create-payin-order) to create a pending payment order for the user.
   - **Address mode**: Call [**Get Deposit Address by Sub-User UID and tokenId**](#create-deposit-address) to generate a deposit address for the user.  
   Choose the mode that fits your business logic.
4. The user completes an on-chain deposit.
5. You receive webhook notifications from Paypaz.  
   **Important:** Configure a callback URL and subscribe to the following events:
   - transaction.payinorder.expired: Payment order expired.
   - transaction.payinorder.underpaid: Payment order underpaid.
   - transaction.payinorder.completed: Payment order completed.
   - transaction.deposit.succeeded: Deposit succeeded.

   >  - A deposit success notification is always sent when the on-chain transfer succeeds.  
   >  - If a payment order has not expired, you may also receive `transaction.payinorder.underpaid` or `transaction.payinorder.completed`.  
   >  - After a payment order expires, you receive `transaction.payinorder.expired`.
   
6. If you do not configure a callback URL, you can obtain deposit results as follows:
   - **Order mode**: Poll [**Query Payment Orders (Paginated)**](#query-payin-order-page) or [**Get Payment Order Details**](#query-payin-order-detail) (optional).
   - **Address mode**: Call [**Query Deposit Orders by Sub-User UID, Address, or Order No.**](#query-deposit-order) (optional).

**Notes:**
- A **payment order** is created by the merchant in order mode only.
- A **deposit order** is created when Paypaz receives a successful on-chain transfer notification.



---

### 1.POST Create Payment Order {#create-payin-order}

Creates a payment order for the specified sub-user.

POST /t-api/openapi/v1/op/openapi/createPayInOrder

#### Notes

Staging environment: `tokenId`: USDT, `chainId`: TRON

Use the TRON testnet in UAT. Test token faucet: https://nileex.io/join/getJoinPage to claim USDT test tokens.

> Request Body

```json
{
  "clientSubUserId": "csub_abc123",
  "payOrderNo": "P202601010001",
  "tokenId": "USDT",
  "chainId": "TRON",
  "payAmount": 10
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | ---------------------- | -- | ---- |
| body | body | CreatePayInOrderRequest | No | none |

#### CreatePayInOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | -------------- | ---- | ---- |---------------------------------|
| clientSubUserId | string | true | none | Client sub-user unique identifier |
| payOrderNo | string | true | none | Payment order number (unique, idempotent) |
| tokenId | string | true | none | Token ID |
| chainId | string | true | none | Chain ID |
| payAmount | number | true | none | Payment amount (must be > 0, >= minimum deposit amount, up to 2 decimal places) |

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 123456789,
    "payOrderNo": "P202601010001",
    "orderStatus": 1,
    "orderSource": 1,
    "tokenId": "USDT",
    "chainId": "TRON",
    "walletAddress": "",
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
    "updatedAt": 1700000000000
  }
}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | ---------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | PayInOrderOpenApiVO | false | none | none |

#### PayInOrderOpenApiVO Properties

| Name | Type | Required | Constraint | Description |
| ----------- | -------------- | ----- | ---- | --------------------- |
| id | integer(int64) | false | none | ID |
| payOrderNo | string | false | none | Payment order number |
| orderStatus | integer(int32) | false | none | Order status (1=processing, 2=underpaid, 3=completed, 4=expired) |
| orderSource | integer(int32) | false | none | Order source (1=broker, 2=paypaz) |
| tokenId | string | false | none | tokenId |
| chainId | string | false | none | chainId |
| walletAddress | string | false | none | Wallet address |
| payAmount | string | false | none | Payment amount |
| amount | string | false | none | On-chain amount |
| fee | string | false | none | Fee |
| netAmount | string | false | none | Net amount received (after fees) |
| arriveTime | string | false | none | Arrival time (millisecond timestamp) |
| fromAddress | string | false | none | Source address |
| txId | string | false | none | Transaction ID (blockchain transaction hash) |
| expireSeconds | integer(int32) | false | none | Validity period (seconds) |
| userId | integer(int64) | false | none | User ID (master account) |
| subUserId | integer(int64) | false | none | Sub-user ID |
| createdAt | string | false | none | Created at (millisecond timestamp) |
| updatedAt | string | false | none | Updated at (millisecond timestamp) |

#### Error Codes

- `500105033`: Payment order number already exists
- `500105034`: Payment order amount precision exceeds the limit
- `500105035`: Payment amount is below the minimum deposit amount

---

### 2.GET Get Payment Order Details {#query-payin-order-detail}

Returns payment order details by payment order number.

GET /t-api/openapi/v1/op/openapi/payInOrderInfo

#### Request Parameters

| Name | Location | Type | Required | Description |
| --------- | ----- | ------ | -- | ------ |
| payOrderNo | query | string | Yes | Payment order number |

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 123456789,
    "payOrderNo": "P202601010001",
    "orderStatus": 1,
    "orderSource": 1,
    "tokenId": "USDT",
    "chainId": "TRON",
    "walletAddress": "",
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
    "updatedAt": 1700000000000
  }
}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | ------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | PayInOrderOpenApiVO | false | none | none |

---

### 3.POST Query Payment Orders (Paginated) {#query-payin-order-page}

Returns a paginated list of payment orders matching the given criteria.

POST /t-api/openapi/v1/op/openapi/payInOrders

#### Notes

`startTime` and `endTime` are required. Use 13-digit millisecond timestamps. Maximum query range is 30 days.

> Request Body

```json
{
  "clientSubUserId": "csub_abc123",
  "payOrderNo": "P202601010001",
  "depositOrderId": 0,
  "tokenId": "USDT",
  "chainId": "TRON",
  "walletAddress": "",
  "orderStatus": 1,
  "startTime": 1626307200000,
  "endTime": 1626393600000,
  "pageNo": 1,
  "pageSize": 10
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | ------------------- | -- | ---- |
| body | body | QueryPayInOrderRequest | No | none |

#### QueryPayInOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | -------------- |-------| ---- | ------------- |
| clientSubUserId | string | false | none | Client sub-user unique identifier |
| payOrderNo | string | false | none | Payment order number |
| depositOrderId | integer(int64) | false | none | Deposit record ID |
| tokenId | string | false | none | Token ID |
| chainId | string | false | none | Chain ID |
| walletAddress | string | false | none | Wallet address |
| orderStatus | integer(int32) | false | none | Order status (1=processing, 2=underpaid, 3=completed, 4=expired) |
| startTime | integer(int64) | true | none | Start time (millisecond timestamp) |
| endTime | integer(int64) | true | none | End time (millisecond timestamp) |
| pageNo | integer(int32) | false | none | Page number, starting at 1 |
| pageSize | integer(int32) | false | none | Page size, range 1–100 |

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 123456789,
      "payOrderNo": "P202601010001",
      "orderStatus": 1,
      "orderSource": 1,
      "tokenId": "USDT",
      "chainId": "TRON",
      "walletAddress": "",
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
      "updatedAt": 1700000000000
    }
  ]
}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | ------------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[\[PayInOrderOpenApiVO]] | false | none | none |

---

### 4.POST Get Deposit Address by Sub-User UID and tokenId {#create-deposit-address}

Returns a deposit address for the specified sub-user and token; creates a new address if none exists.

POST /t-api/openapi/v1/op/openapi/depositAddress

#### tokenId Notes

Staging environment: `tokenId`: USDT, `chainId`: TRON

> Request Body

```json
{
  "clientSubUserId": "csub_abc123",
  "tokenId": "USDT",
  "chainId": "TRON"
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | --------------------------- | -- | ---- |
| body | body | \[GetDepositAddressRequest] | No | none |

#### GetDepositAddressRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | ------ | ---- | ---- | --------- |
| clientSubUserId | string | true | none | Client sub-user unique identifier |
| tokenId | string | true | none | Token ID |
| chainId | string | true | none | Chain ID |

> Response Example

> 200 Response

```
{"code":0,"msg":"string","data":{"id":"string","userId":"string","tokenId":"string","chainId":"string","address":"string","createdAt":"string","updatedAt":"string","tag":"string","subUserId":"string"}}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | ---------------------------- | ----- | ---- | ----------------------- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[SubWalletAddressOpenApiVO] | false | none | <p>Enterprise sub-user wallet address entity<br></p> |

#### SubWalletAddressOpenApiVO Properties

| Name | Type | Required | Constraint | Description |
| --------- | ------ | ----- | ---- | ------------------------------------ |
| id | string | false | none | Primary key ID |
| userId | string | false | none | <p>User ID<br>Master account identifier</p> |
| tokenId | string | false | none | <p>Token ID<br>e.g. USDT, USDC</p> |
| chainId | string | false | none | <p>chainId<br>e.g. BNB, TRON</p> |
| address | string | false | none | <p>Wallet address<br>Blockchain address used to receive deposits</p> |
| createdAt | string | false | none | <p>Created at<br>Record creation timestamp (milliseconds)</p> |
| updatedAt | string | false | none | <p>Updated at<br>Last update timestamp (milliseconds)</p> |
| tag | string | false | none | <p>Tag<br>Additional identifier required for some tokens (e.g. XRP, XLM)</p> |
| subUserId | string | false | none | <p>Sub-user ID<br>Enterprise sub-account identifier</p> |

---

### 5.POST Query Deposit Orders by Sub-User UID, Address, or Order No. {#query-deposit-order}

Returns a paginated list of deposit orders. Supports filters by time range, token, wallet address, and more.

POST /t-api/openapi/v1/op/openapi/depositOrders

#### tokenId Notes

Staging environment: `tokenId`: USDT, `chainId`: TRON

> Request Body

```json
 {
  "clientSubUserId": "csub_abc123",
  "walletAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "orderNo": "D202501010001",
  "tokenId": "USDT",
  "chainId": "TRON",
  "startTime": 1626307200000,
  "endTime": 1626393600000,
  "pageNo": 1,
  "pageSize": 10
 }
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | --------------------------- | -- | ---- |
| body | body | \[QueryDepositOrderRequest] | No | none |

#### QueryDepositOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | -------------- | ----- | ---- | ------------ |
| clientSubUserId | string | false | none | Client sub-user unique identifier |
| walletAddress | string | false | none | Wallet address |
| orderNo | string | false | none | Order number |
| tokenId | string | false | none | Token ID |
| chainId | string | false | none | Chain ID |
| startTime | string | false | none | Start time (millisecond timestamp) |
| endTime | string | false | none | End time (millisecond timestamp) |
| pageNo | integer(int32) | false | none | Page number, starting at 1 |
| pageSize | integer(int32) | false | none | Page size, range 1–100 |

> Response Example

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":123456789,"orderNo":"D202501010001","tokenId":"USDT","chainId":"TRON","quantity":0.01,"fee":0,"netAmount":0.01,"fromAddress":"0x1234567890abcdef1234567890abcdef12345678","walletAddress":"0xabcdef1234567890abcdef1234567890abcdef12","txId":"0x9876543210abcdef9876543210abcdef98765432","status":1,"createdAt":1626307200000,"updatedAt":1626307260000,"userId":123456,"subUserId":789012}]}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | --------------------------- | ----- | ---- | ------------------ |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[\[DepositOrderOpenApiVO]] | false | none | \[Deposit order OpenAPI response VO] |
|        |                             |       |      |     |                    |

#### DepositOrderOpenApiVO Properties

| Name | Type | Required | Constraint | Description |
| ------------- | -------------- | ----- | ---- | ---------------- |
| id | string | false | none | Deposit order ID |
| orderNo | string | false | none | Order number |
| tokenId | string | false | none | Token ID |
| chainId | string | false | none | On-chain token ID |
| quantity | string | false | none | Deposit quantity |
| fee | string | false | none | Fee |
| netAmount | string | false | none | Net credited amount |
| fromAddress | string | false | none | Source address |
| walletAddress | string | false | none | Wallet address |
| txId | string | false | none | Transaction ID (blockchain transaction hash) |
| status | integer(int32) | false | none | Deposit status: 1 = completed, credited |
| createdAt | string | false | none | Created at (millisecond timestamp) |
| updatedAt | string | false | none | Updated at (millisecond timestamp) |
| userId | string | false | none | User ID (master account) |
| subUserId | string | false | none | Sub-user ID |
