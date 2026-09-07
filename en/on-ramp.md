# On-Ramp APIs

All endpoints below require signature authentication. For common headers and the signature algorithm, see the [**Authentication API**](README.md). The merchant is identified by the API Key, so there is no need to pass a merchant ID in the body.

---

### On-Ramp Integration Flow

1. KYC is bound to the end user (a fixed `clientSubUserId` within a merchant), not to an individual order.
2. New users: call [**Sync KYC Profile**](#sync-kyc-profile) first to submit the profile to PayPaz, then call [**Create On-Ramp Order**](#create-onramp-order).
   - With a Sumsub `shareToken`, the merchant's existing KYC data is reused; without it, the user fills in the profile on the hosted page.
3. Existing users (`kycStatus=APPROVED`): you may skip the sync step and create the order directly. It usually returns `CREATED` (already quoted).
4. Direct the user to the `embedUrl` returned by order creation (the hosted order page). Any remaining KYC is completed there, and the user then selects a payment method and pays on the same page.
5. Retrieve order results in either of these ways:
   - Single order: [**Get On-Ramp Order Details**](#query-onramp-order-detail).
   - Order list: [**Query On-Ramp Orders**](#query-onramp-order-list).

```
New user:
  POST /onramp/kyc/initiate   (reuses Sumsub when shareToken is present, otherwise the user fills it in)
       ↓
  POST /onramp/orders         → status=KYC_REQUIRED, returns embedUrl
       ↓
  Open embedUrl               → complete KYC → activate quote → pay

Existing user (kycStatus=APPROVED):
  Skip initiate
       ↓
  POST /onramp/orders         → usually CREATED (already quoted)
       ↓
  Open embedUrl               → pay
```

**Notes:**
- Order creation returns both the **order `status`** and the **KYC `kycStatus`**. They mean different things, see [**Order and KYC Status**](#status).
- The KYC profile can be synced before or after order creation. Syncing first is recommended so the profile is pre-filled when the hosted page opens.
- `kycCheckoutUrl` is a KYC-only page with no order attached. **Do not use it to test payments** — always direct users to `embedUrl`.
- Quoting, document/liveness checks, and payment widgets are called internally by the hosted page. You do not need to integrate those endpoints.
- On-Ramp webhooks are not yet available to merchants. For now, poll the query endpoints.

---

### 1.POST Sync KYC Profile {#sync-kyc-profile}

Submits and archives the end user's KYC profile by `clientSubUserId`. It does not depend on an existing order.

POST /t-api/openapi/v1/op/openapi/onramp/kyc/initiate

#### Notes

**Do not pass `mode`.** A non-empty `shareToken` means Sumsub reuse; without it, the user fills in the profile on the hosted page (documents and liveness are handled there).

Without a `shareToken`, any missing fields are completed by the user on `embedUrl`. With a `shareToken`, the full standard profile must be provided in this request, and documents are reused from Sumsub. The `profile` field names are camelCase; legacy snake_case names are still accepted.

> Request Body (user-provided, no shareToken)

```json
{
  "clientSubUserId": "csub_abc123",
  "email": "user@example.com",
  "userIp": "18.136.0.1",
  "profile": {
    "email": "user@example.com",
    "firstName": "Taro",
    "lastName": "Tanaka",
    "birthdate": "1990-04-12",
    "countryIso2Code": "JP",
    "state": "Tokyo",
    "city": "Tokyo",
    "address": "1-1 Chiyoda",
    "zipCode": "100-0001",
    "citizenshipIso2Codes": "JP",
    "placeOfBirth": "JP"
  }
}
```

> Request Body (Sumsub reuse, with shareToken)

```json
{
  "clientSubUserId": "csub_abc123",
  "email": "user@example.com",
  "shareToken": "_act-sbx-jwt-...",
  "userIp": "18.136.0.1",
  "profile": {
    "email": "user@example.com",
    "firstName": "Taro",
    "lastName": "Tanaka",
    "birthdate": "1990-04-12",
    "countryIso2Code": "JP",
    "state": "Tokyo",
    "city": "Tokyo",
    "address": "1-1 Chiyoda",
    "zipCode": "100-0001",
    "citizenshipIso2Codes": "JP",
    "placeOfBirth": "JP"
  }
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | ------------------------ | -- | ---- |
| body | body | OnRampKycInitiateRequest | No | none |

#### OnRampKycInitiateRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | ------ | ----- | ---- | ---------------------------------------- |
| clientSubUserId | string | true | none | End-user unique identifier, must stay the same for the same user |
| email | string | true | none | End customer email |
| shareToken | string | false | none | Non-empty enables Sumsub reuse; empty means the user fills in the profile on the hosted page. Single-use, do not persist |
| userIp | string | false | none | End-user IP, recommended |
| profile | object | false | none | KYC profile, camelCase field names, see the table below |
| mode | string | false | none | Deprecated. Inferred from whether `shareToken` is empty; ignored if passed |

#### profile Properties

| Name | Type | Required | Constraint | Description |
| -------------------- | ------ | ----- | ---- | --------------------------- |
| firstName | string | false | none | Given name, recommended |
| lastName | string | false | none | Family name, recommended |
| birthdate | string | false | none | Date of birth in YYYY-MM-DD, recommended |
| countryIso2Code | string | false | none | Country of residence in ISO2, must match the address; required when `shareToken` is present |
| state | string | false | none | State or province, recommended |
| city | string | false | none | City, recommended |
| address | string | false | none | Street address, recommended |
| zipCode | string | false | none | Postal code, recommended |
| citizenshipIso2Codes | string | false | none | Citizenship in ISO2, recommended |
| placeOfBirth | string | false | none | Place of birth in ISO2, recommended |

#### How to Obtain a shareToken {#share-token}

Enable **Share applicants data** in your own Sumsub dashboard, then call Sumsub `POST /resources/accessTokens/shareToken` with your App Token. `forClientId` is provided by PayPaz — do not use your own clientId. The token is single-use with a short TTL: pass it to this endpoint immediately and do not persist it.

Both sandbox and production use `https://api.sumsub.com`; the environment is determined by the App Token prefix (usually `sbx:` for sandbox). The applicant must already be approved on your Sumsub side, and the verification level must include document verification (not liveness only).

> Sumsub Request Body

```json
{
  "applicantId": "6a41dab9f1fa3809f65ceae6",
  "forClientId": "<Sumsub clientId provided by PayPaz>",
  "ttlInSecs": 600
}
```

| Name | Type | Required | Constraint | Description |
| ----------- | ------ | ----- | ---- | ----------------------------------------- |
| applicantId | string | true | none | Applicant ID on your Sumsub side (from the dashboard or the Applicant API) |
| forClientId | string | true | none | Recipient Sumsub clientId provided by PayPaz; do not use your own clientId |
| ttlInSecs | number | false | none | Token lifetime in seconds, 600 (10 minutes) recommended; regenerate after expiry |

> curl example (sign timestamp+METHOD+path+body with HMAC-SHA256 hex per Sumsub rules)

```bash
TS=$(date +%s)
BODY='{"applicantId":"YOUR_APPLICANT_ID","forClientId":"PAYPAZ_CLIENT_ID","ttlInSecs":600}'
SUMSUB_PATH='/resources/accessTokens/shareToken'
SIG=$(python3 -c "import hmac,hashlib,os,sys
secret=os.environ['SUMSUB_SECRET_KEY']
ts,method,path,body=sys.argv[1:]
print(hmac.new(secret.encode(), (ts+method+path+body).encode(), hashlib.sha256).hexdigest())" \
  "$TS" POST "$SUMSUB_PATH" "$BODY")

curl -s -X POST "https://api.sumsub.com$SUMSUB_PATH" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-App-Token: $SUMSUB_APP_TOKEN" \
  -H "X-App-Access-Ts: $TS" \
  -H "X-App-Access-Sig: $SIG" \
  -d "$BODY"
```

> Sumsub Success Response (use `token` as the `shareToken` for this endpoint)

```json
{
  "token": "_act-sbx-jwt-eyJhbGciOiJub25lIn0...."
}
```

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "status": "KYC_REQUIRED",
    "email": "user@example.com",
    "pageId": "....",
    "kycCheckoutUrl": "https://pay.example.com/onramp/kyc?pageId=....",
    "kycStatus": "STARTED",
    "kycMode": "sdk_v2"
  }
}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | --------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | OnRampOrderOpenApiVO | false | none | none |

This endpoint does not return an order `embedUrl`. Once you receive `kycStatus=STARTED`, proceed to [**Create On-Ramp Order**](#create-onramp-order).

#### Error Codes

- `500105001`: Missing required authentication information (the `profile` has no country of residence while `shareToken` is present)
- `500105007`: Sub-user does not exist
- `500105008`: Query parameters cannot all be empty (`clientSubUserId` is empty)

---

### 2.POST Create On-Ramp Order {#create-onramp-order}

Creates an On-Ramp order by net crypto amount. The fiat currency and payment method are selected by the user on the hosted page.

POST /t-api/openapi/v1/op/openapi/onramp/orders

#### Notes

The order is always persisted regardless of KYC state: when KYC is incomplete, `status=KYC_REQUIRED`; when KYC has passed, PayPaz tries to activate the quote and the status is usually `CREATED`.

`brokerOrderRef` is the merchant order number and must be unique per merchant. Duplicate submissions return the same order.

> Request Body

```json
{
  "brokerOrderRef": "ONRAMP-20260825-0001",
  "clientSubUserId": "csub_abc123",
  "netCryptoAmount": "1000",
  "cryptoCurrency": "USDT",
  "email": "user@example.com",
  "userIp": "18.136.0.1",
  "network": "TRON",
  "tokenId": "USDT",
  "chainId": "TRON",
  "returnUrl": "https://merchant.example.com/onramp/result"
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | ------------------------ | -- | ---- |
| body | body | CreateOnRampOrderRequest | No | none |

#### CreateOnRampOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | ------ | ----- | ---- | ----------------------------------- |
| brokerOrderRef | string | true | none | Merchant order number, unique per merchant |
| clientSubUserId | string | true | none | End-user unique identifier, must match the one used when syncing the KYC profile |
| netCryptoAmount | string | true | none | Net crypto amount, must be at least 0.000001 |
| email | string | true | none | End customer email |
| cryptoCurrency | string | false | none | Crypto currency, defaults to USDT |
| fiatCurrency | string | false | none | Fiat currency, optional; the user can still change it on the hosted page |
| tokenId | string | false | none | PayPaz deposit token, defaults to USDT |
| chainId | string | false | none | PayPaz deposit chain, defaults to TRON |
| network | string | false | none | Network name on the Legend side, defaults to the same value as `chainId` |
| userIp | string | false | none | End-user IP, recommended |
| returnUrl | string | false | none | Redirect URL for the "Return to merchant" action on the result page. Must be https, up to 2048 characters. Payment notifications do not use this field |

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "paypazOrderId": "PZO2091798976151027712",
    "brokerOrderRef": "ONRAMP-20260825-0001",
    "clientSubUserId": "csub_abc123",
    "status": "KYC_REQUIRED",
    "cryptoCurrency": "USDT",
    "cryptoAmountNet": "1000",
    "fiatCurrency": "USD",
    "fiatAmountCharge": null,
    "fiatAmountPaid": null,
    "network": "TRON",
    "embedUrl": "https://pay.example.com/onramp?pageId=....",
    "kycCheckoutUrl": "https://pay.example.com/onramp/kyc?pageId=....",
    "pageId": "....",
    "email": "user@example.com",
    "returnUrl": "https://merchant.example.com/onramp/result",
    "kycStatus": "STARTED",
    "kycMode": "sdk_v2",
    "expireAt": null,
    "createdAt": "1787558714317"
  }
}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | --------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | OnRampOrderOpenApiVO | false | none | none |

#### OnRampOrderOpenApiVO Properties

| Name | Type | Required | Constraint | Description |
| ---------------- | ------ | ----- | ---- | ------------------------------- |
| paypazOrderId | string | false | none | PayPaz order number |
| brokerOrderRef | string | false | none | Merchant order number |
| clientSubUserId | string | false | none | End-user unique identifier |
| status | string | false | none | Order status, see [**Order and KYC Status**](#status) |
| cryptoCurrency | string | false | none | Crypto currency |
| cryptoAmountNet | string | false | none | Net crypto amount |
| fiatCurrency | string | false | none | Fiat currency |
| fiatAmountCharge | string | false | none | Fiat amount charged; null before the order is activated |
| fiatAmountPaid | string | false | none | Fiat amount actually paid; empty until the trade is settled |
| network | string | false | none | Network name |
| walletAddress | string | false | none | PayPaz deposit address; empty before the order is activated |
| embedUrl | string | false | none | **Hosted order page, direct users to this URL** (KYC + payment) |
| kycCheckoutUrl | string | false | none | KYC-only page, do not use it for payments |
| pageId | string | false | none | Hosted page token |
| legendUid | string | false | none | User identifier on the channel side |
| email | string | false | none | End customer email |
| returnUrl | string | false | none | Redirect URL for the "Return to merchant" action on the result page |
| kycStatus | string | false | none | KYC status of this user, see [**Order and KYC Status**](#status) |
| kycMode | string | false | none | KYC mode, such as `sdk_v2` or `sumsub_share` |
| paymentMethod | string | false | none | Payment method, written back after the user selects it on the hosted page |
| txHash | string | false | none | Crypto transfer transaction hash |
| expireAt | string | false | none | Quote expiry time (millisecond timestamp); may be null before the order is activated |
| createdAt | string | false | none | Created at (millisecond timestamp) |
| completedAt | string | false | none | Completed at (millisecond timestamp) |

- When `status=KYC_REQUIRED`, the order has been created successfully. Just open `embedUrl`.
- If the KYC profile has not been synced, the hosted page still guides the user through KYC, but syncing first is recommended.
- After KYC passes, the user continues to payment on the same page. Do not create the order again.

#### Error Codes

- `500105007`: Sub-user does not exist
- `500105008`: Query parameters cannot all be empty (`clientSubUserId` is empty)
- `500105026`: Deposit token not found (invalid `tokenId` / `chainId` combination)
- `500105028`: Deposit not allowed (deposits are disabled for this token)
- `500105041`: On-Ramp pair unavailable
- `500105042`: On-Ramp amount below the minimum
- `500105043`: On-Ramp chain not supported
- `500105044`: On-Ramp chain not enabled

---

### 3.GET Get On-Ramp Order Details {#query-onramp-order-detail}

Returns On-Ramp order details by merchant order number.

GET /t-api/openapi/v1/op/openapi/onramp/orders/info

#### Request Parameters

| Name | Location | Type | Required | Description |
| -------------- | ----- | ------ | -- | ----- |
| brokerOrderRef | query | string | Yes | Merchant order number |

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "paypazOrderId": "PZO2091798976151027712",
    "brokerOrderRef": "ONRAMP-20260825-0001",
    "clientSubUserId": "csub_abc123",
    "status": "COMPLETED",
    "cryptoCurrency": "USDT",
    "cryptoAmountNet": "1000",
    "fiatCurrency": "USD",
    "fiatAmountCharge": "1084.9",
    "fiatAmountPaid": "1084.9",
    "network": "TRON",
    "walletAddress": "TXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    "embedUrl": "https://pay.example.com/onramp?pageId=....",
    "kycStatus": "APPROVED",
    "kycMode": "sdk_v2",
    "paymentMethod": "card",
    "txHash": "0x9876543210abcdef9876543210abcdef98765432",
    "expireAt": "1787560514317",
    "createdAt": "1787558714317",
    "completedAt": "1787559014317"
  }
}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | --------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | OnRampOrderOpenApiVO | false | none | none |

#### Error Codes

- `500105008`: Query parameters cannot all be empty (`brokerOrderRef` is empty)
- `500105045`: On-Ramp order not found

---

### 4.POST Query On-Ramp Orders {#query-onramp-order-list}

Returns a paginated list of On-Ramp orders for the merchant that owns the current API Key, sorted by `createdAt` descending. Requires the `onRamp` permission.

POST /t-api/openapi/v1/op/openapi/onramp/orders/query

#### Notes

`startTime` and `endTime` must be provided together or omitted together, as 13-digit millisecond values, with a maximum query range of 30 days. When both are omitted, the current UTC day is used.

List items use the same fields as `OnRampOrderOpenApiVO`, but **KYC is not queried separately**, so `kycStatus` and `kycMode` may be empty.

> Request Body

```json
{
  "clientSubUserId": "csub_abc123",
  "brokerOrderRef": "ONRAMP-20260825-0001",
  "paypazOrderId": "PZO2091798976151027712",
  "status": "COMPLETED",
  "startTime": 1721606400000,
  "endTime": 1724198400000,
  "pageNo": 1,
  "pageSize": 20
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | ----------------------- | -- | ---- |
| body | body | QueryOnRampOrderRequest | No | none |

#### QueryOnRampOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | -------------- | ----- | ---- | --------------------------------- |
| clientSubUserId | string | false | none | End-user unique identifier. Omit to query all orders of the merchant; an error is returned if the sub-user does not exist |
| brokerOrderRef | string | false | none | Merchant order number (exact match) |
| paypazOrderId | string | false | none | PayPaz order number (exact match) |
| status | string | false | none | Order status, such as `CREATED` or `COMPLETED` |
| startTime | integer(int64) | false | none | Start time (13-digit millisecond timestamp), must be sent together with `endTime` |
| endTime | integer(int64) | false | none | End time (13-digit millisecond timestamp), must be sent together with `startTime` |
| pageNo | integer(int32) | false | none | Page number, starts at 1, defaults to 1 |
| pageSize | integer(int32) | false | none | Page size, 1-100, defaults to 20 |

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1,
    "size": 1,
    "list": [
      {
        "paypazOrderId": "PZO2091798976151027712",
        "brokerOrderRef": "ONRAMP-20260825-0001",
        "clientSubUserId": "csub_abc123",
        "status": "CREATED",
        "cryptoCurrency": "USDT",
        "cryptoAmountNet": "1000",
        "fiatCurrency": "USD",
        "fiatAmountCharge": "1084.9",
        "fiatAmountPaid": null,
        "network": "TRON",
        "embedUrl": "https://pay.example.com/onramp?pageId=....",
        "createdAt": "1787558714317"
      }
    ]
  }
}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | --------------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[\[OnRampOrderOpenApiVO]] | false | none | Paginated result |

#### Error Codes

- `500105007`: Sub-user does not exist
- `500105036`: Start time and end time cannot be empty (only one was provided)
- `500105037`: Start time cannot be later than end time
- `500105038`: The interval between start time and end time cannot exceed 30 days
- `500105039`: Invalid time format, provide a valid 13-digit millisecond timestamp

---

### Order and KYC Status {#status}

#### Order status

| status | Description |
| ------------------- | ----------------- |
| KYC_REQUIRED | KYC incomplete, not quoted and no deposit address yet |
| ACTIVATING | KYC passed, creating the deposit address and fetching the quote |
| CREATED | Activated, ready for checkout |
| AWAITING_PAYMENT | The user has entered the payment flow |
| PAYMENT_PROCESSING | Payment accepted |
| FIAT_RECEIVED | Fiat received |
| CRYPTO_TRANSFERRING | Crypto transfer in progress |
| PENDING_VERIFICATION | Additional compliance verification |
| COMPLETED | Completed |
| FAILED | Failed |
| EXPIRED | Expired |

Typical transitions: `KYC_REQUIRED` → (KYC passed) → `CREATED` → `AWAITING_PAYMENT` → `PAYMENT_PROCESSING` → `CRYPTO_TRANSFERRING` → `COMPLETED`

#### kycStatus

| kycStatus | Description |
| ----------- | ----------------- |
| NOT_STARTED | KYC profile not synced yet |
| STARTED | KYC profile synced |
| PENDING | Under review |
| APPROVED | Approved; order activation is attempted at creation time |
| REJECTED | Rejected |
