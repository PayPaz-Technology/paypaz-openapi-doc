# Withdrawal APIs

All endpoints below require signature authentication.

---

### Withdrawal Integration Flow
1. Call [**Get All Networks**](base-data-api.md#get-all-networks) to retrieve supported chain information.
2. Call [**Get Token Configuration for a Token**](base-data-api.md#get-token-config) to retrieve deposit and withdrawal settings for the token.
3. Before initiating a withdrawal, call [**Query Assets by tokenId**](#query-user-asset) to check whether the master account balance (sub-account balances are managed by the merchant) meets the withdrawal amount.
4. Call [**Create Withdrawal by UID**](#create-withdraw-order) to create a withdrawal order.
5. You receive webhook notifications from Paypaz.  
   **Important:** Configure a callback URL and subscribe to the following events:
   - transaction.withdrawal.succeeded: Withdrawal succeeded
   - transaction.withdrawal.failed: Withdrawal failed
6. If you do not configure a callback URL, you can obtain withdrawal results via:
   - [**Query Withdrawal Orders by Sub-User UID, Address, or Order No.**](#query-withdraw-order-page)
   - [**Get Withdrawal Order Details**](#query-withdraw-order-detail)

---

### 1.GET Query Assets by tokenId {#query-user-asset}

Returns asset information for a specific token or all tokens.

GET /t-api/openapi/v1/op/openapi/assets

#### tokenId Notes

Staging environment: `tokenId`: USDT, `chainId`: TRON

#### Request Parameters

| Name | Location | Type | Required | Description |
| ------- | ----- | ------ | -- | ---- |
| tokenId | query | string | No | Token ID |

> Response Example

> 200 Response

```
{"code":200,"msg":"success","data":[{"id":"1953034545579294720","accountId":"1951221207996780544","tokenId":"USDT","userId":"468001460","total":"0.070013132010012112","assetTotal":"0.070013132010012112","availableAssetTotal":"0.070013132010012112","marketPrice":"0","locked":"0","available":"0.070013132010012112","indebted":"0","createdAt":"1754474695950","updatedAt":"1754559221966","accountType":1},{"id":"1953010034274480128","accountId":"1951221207996780544","tokenId":"BNB","userId":"468001460","total":"1","assetTotal":"799.31","availableAssetTotal":"798.51069","marketPrice":"799.31","locked":"0.001","available":"0.999","indebted":"0","createdAt":"1754468851999","updatedAt":"1754559160300","accountType":1},{"id":"1952934600546816000","accountId":"1951221207996780544","tokenId":"TBSC_BNB","userId":"468001460","total":"0.272193990000000101","assetTotal":"0.272193990000000101","availableAssetTotal":"0.272098890000000101","marketPrice":"0","locked":"0.0000951","available":"0.272098890000000101","indebted":"0","createdAt":"1754450867197","updatedAt":"1754920359479","accountType":1}]}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | ---------------------- | ----- | ---- | ----- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[\[BalanceOpenApiVO]] | false | none | \[Assets] |

#### BalanceOpenApiVO Properties

| Name | Type | Required | Constraint | Description |
| --------- | ------ | ----- | ---- | ------ |
| id | string | false | none | none |
| accountId | string | false | none | Account ID |
| tokenId | string | false | none | none |
| userId | string | false | none | none |
| total | string | false | none | Total assets |
| locked | string | false | none | Locked assets |
| available | string | false | none | Available assets |
| indebted | string | false | none | Indebted assets |
| createdAt | string | false | none | Created at |
| updatedAt | string | false | none | Updated at |

---

### 2.POST Create Withdrawal by UID {#create-withdraw-order}

Creates a withdrawal order for the specified sub-user. Two-factor authentication (2FA) for the sub-user must be verified by the caller.

POST /t-api/openapi/v1/op/openapi/createWithdrawal

#### tokenId Notes

Staging environment: `tokenId`: USDT, `chainId`: TRON

> Request Body

```json
{
  "clientSubUserId": "csub_abc123",
  "tokenId": "USDT",
  "chainId": "TRON",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "amount": 0.01,
  "clientWithdrawalId": "client12345678901234",
  "twoFactorAuthentication": true
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | -------------------------- | -- | ---- |
| body | body | \[CreateWithdrawalRequest] | No | none |

#### CreateWithdrawalRequest Properties

| Name | Type | Required | Constraint | Description |
| ----------------------- | ------- | ---- | ---- | ----------------------------------------- |
| clientSubUserId | string | true | none | Client sub-user unique identifier |
| tokenId | string | true | none | Token ID |
| chainId | string | true | none | Chain ID |
| address | string | true | none | Withdrawal address |
| amount | string | true | none | Withdrawal amount |
| clientWithdrawalId | string | true | none | Client order ID (for idempotency) |
| twoFactorAuthentication | boolean | true | none | Two-factor authentication (2FA) confirmed |

> Response Example

> 200 Response

```
{"code":0,"msg":"string","data":{"id":"string","clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainTokenId":"string","userId":"string","subUserId":"string","address":"string","arriveQuantity":"string","statusInfo":0,"createdAt":"string","updatedAt":"string","arriveTime":"string","totalQuantity":"string","txId":"string"}}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | --------------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[WithdrawalOrderOpenApiVO] | false | none | none |

#### WithdrawalOrderOpenApiVO Properties

| Name | Type | Required | Constraint | Description |
| ------------------ | -------------- | ----- | ---- | --------------------- |
| id | string | false | none | Platform-generated ID |
| clientWithdrawalId | string | false | none | Client order ID |
| transactionId | string | false | none | Order ID |
| tokenId | string | false | none | tokenId |
| chainId | string | false | none | Chain token ID |
| userId | string | false | none | none |
| subUserId | string | false | none | Enterprise sub-user ID |
| address | string | false | none | User withdrawal address |
| platformFee | string | false | none | Fee |
| arriveQuantity | string | false | none | Actual withdrawal amount received |
| statusInfo | integer(int32) | false | none | 1 = processing, 2 = succeeded, 3 = failed |
| createdAt | string | false | none | Created at |
| updatedAt | string | false | none | Updated at |
| arriveTime | string | false | none | Withdrawal arrival time |
| totalQuantity | string | false | none | Withdrawal amount entered by the user |
| txId | string | false | none | transaction hash |

---

### 3.POST Query Withdrawal Orders by Sub-User UID, Address, or Order No. {#query-withdraw-order-page}

Returns a paginated list of withdrawal orders matching the given criteria.

POST /t-api/openapi/v1/op/openapi/withdrawalOrders

#### Notes

Staging environment: `tokenId`: USDT, `chainId`: TRON

> Request Body

```json
{
  "clientSubUserId": "csub_abc123",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "clientWithdrawalId": "client12345678",
  "tokenId": "USDT",
  "chainId": "TRON",
  "txId": "0xabcd1234...",
  "startTime": 1626307200000,
  "endTime": 1626393600000,
  "pageNo": 1,
  "pageSize": 10
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | --------------------------- | -- | ---- |
| body | body | QueryWithdrawalOrderRequest | No | none |

#### QueryWithdrawalOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| ------------------ | -------------- | ----- | ---- | ------------- |
| clientSubUserId | string | false | none | Client sub-user unique identifier |
| address | string | false | none | Withdrawal address |
| clientWithdrawalId | string | false | none | Client withdrawal order ID |
| tokenId | string | false | none | Token ID |
| chainId | string | false | none | Chain ID |
| txId | string | false | none | Transaction ID (blockchain transaction hash) |
| startTime | string | false | none | Start time (millisecond timestamp) |
| endTime | string | false | none | End time (millisecond timestamp) |
| pageNo | integer(int32) | false | none | Page number, starting at 1 |
| pageSize | integer(int32) | false | none | Page size, range 1–100 |

> Response Example

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":"string","clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainId":"string","userId":"string","subUserId":"string","address":"string","platformFee":"string","arriveQuantity":"string","statusInfo":0,"createdAt":"string","updatedAt":"string","arriveTime":"string","totalQuantity":"string","txId":"string"}]}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | ------------------------------ | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[\[WithdrawalOrderOpenApiVO]] | false | none | none |

#### WithdrawalOrderOpenApiVO Properties

| Name | Type | Required | Constraint | Description |
| ------------------ | -------------- | ----- | ---- | --------------------- |
| id | string | false | none | Platform-generated ID |
| clientWithdrawalId | string | false | none | Client order ID |
| transactionId | string | false | none | Order ID |
| tokenId | string | false | none | tokenId |
| chainId | string | false | none | Chain token ID |
| userId | string | false | none | none |
| subUserId | string | false | none | Enterprise sub-user ID |
| address | string | false | none | User withdrawal address |
| platformFee | string | false | none | Fee |
| arriveQuantity | string | false | none | Actual withdrawal amount received |
| statusInfo | integer(int32) | false | none | 1 = processing, 2 = succeeded, 3 = failed |
| createdAt | string | false | none | Created at |
| updatedAt | string | false | none | Updated at |
| arriveTime | string | false | none | Withdrawal arrival time |
| totalQuantity | string | false | none | Withdrawal amount entered by the user |
| txId | string | false | none | transaction hash |


---

### 4.GET Get Withdrawal Order Details {#query-withdraw-order-detail}

Returns withdrawal order details by client withdrawal order ID.

GET /t-api/openapi/v1/op/openapi/withdrawalOrderInfo

#### tokenId Notes

Staging environment: `tokenId`: USDT, `chainId`: TRON

#### Request Parameters

| Name | Location | Type | Required | Description |
| ------------------ | ----- | ------ | -- | --------- |
| clientWithdrawalId | query | string | Yes | Client withdrawal order ID |

> Response Example

> 200 Response

```
{"code":200,"msg":"success","data":{"id":"1954903600726814720","clientWithdrawalId":"WKxXXnkaD0luIGvnZVrglg7UALaYDPTLiQdEbYvUZjL9qI4ekEqW","transactionId":null,"tokenId":"TBSC_BNB","chainTokenId":"TBSC_BNB","userId":"468001460","subUserId":"468001460825249908","address":"0xa8c5eea944c3af945203e18cf990905519a158ad","arriveQuantity":"0.000000099","statusInfo":1,"createdAt":"1754920313415","updatedAt":"1754920313415","arriveTime":null,"totalQuantity":"0.0000001","txId":null}}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | --------------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[WithdrawalOrderOpenApiVO] | false | none | none |

#### WithdrawalOrderOpenApiVO Properties

| Name | Type | Required | Constraint | Description |
| ------------------ | -------------- | ----- | ---- | --------------------- |
| id | string | false | none | Platform-generated ID |
| clientWithdrawalId | string | false | none | Client order ID |
| transactionId | string | false | none | Order ID |
| tokenId | string | false | none | tokenId |
| chainId | string | false | none | Chain token ID |
| userId | string | false | none | none |
| subUserId | string | false | none | Enterprise sub-user ID |
| address | string | false | none | User withdrawal address |
| platformFee | string | false | none | Fee |
| arriveQuantity | string | false | none | Actual withdrawal amount received |
| statusInfo | integer(int32) | false | none | 1 = processing, 2 = succeeded, 3 = failed |
| createdAt | string | false | none | Created at |
| updatedAt | string | false | none | Updated at |
| arriveTime | string | false | none | Withdrawal arrival time |
| totalQuantity | string | false | none | Withdrawal amount entered by the user |
| txId | string | false | none | transaction hash |
