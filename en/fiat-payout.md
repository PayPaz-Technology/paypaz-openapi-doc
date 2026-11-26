# Fiat Pay-out APIs

All of the following APIs require signature authentication. See [**Authentication API**](README.md) for the common headers and the signing algorithm. The merchant is identified by the API key; do not pass a merchant ID in the body.

The API key must have **Fiat Pay-out** enabled (scope key `fiatPayOut`). Otherwise all three APIs return `500105010`, no order is created and no data is returned. Webhooks are delivered according to the merchant's webhook configuration and do not depend on the API key's scope.

---

### Integration Flow {#flow}

1. The merchant server calls [**Create Fiat Pay-out Order**](#create-fiat-payout-order). Paypaz freezes the merchant's USDT at the locked rate and returns the order snapshot, including `orderNo` and the hosted page URL `hostedUrl`.
2. The merchant gives `hostedUrl` to the recipient of that order. The recipient uses the hosted page to follow progress, confirm receipt or report a problem.
3. The collection partner accepts and pays, and Paypaz sends [**Webhooks**](#webhook). The merchant can also follow up with [**Get Fiat Pay-out Order Details**](#query-fiat-payout-order) or [**Query Fiat Pay-out Orders**](#query-fiat-payout-order-list).
4. Once the funds arrive, the merchant confirms receipt in the merchant portal under "Fiat Pay-in/Pay-out › Fiat Pay-out Records". Disputes and supplementary evidence are also handled in the merchant portal; these actions have no OpenAPI yet.

```
POST createFiatPayOutOrder → USDT frozen, orderNo and hostedUrl returned
  ↓
Give hostedUrl to the recipient → the recipient follows progress, confirms receipt or reports a problem
  ↓
A partner accepts → Webhook accepted
  ↓
The partner pays → Webhook proof_submitted
  ↓
The merchant confirms receipt in the merchant portal → Webhook completed
  (on a timeout or dispute → Webhook appealed, then completed / failed after the ruling)
  ↓
If a callback is missing → GET fiatPayOutOrderInfo to reconcile
```

**Order lifecycle:**
- **Pending acceptance**: if no partner accepts within 60 minutes of creation, the order becomes `rejected` (`rejectReason = accept_timeout`) and the frozen USDT is released in full. A partner rejection also leads to `rejected` (`partner_rejected`).
- **Pending payment**: if the partner submits no valid payment within 30 minutes of accepting, the order enters an appeal (`appealSource = timeout`) and `paymentTimeout` becomes `true`.
- **Pending confirmation**: entered once the partner submits a payment that matches the order amount. If the merchant does not confirm within 30 minutes, the order enters an appeal (`appealSource = confirm_timeout`) and `confirmationTimeout` becomes `true`.
- **Completed**: reached in only two ways: the merchant confirms receipt in the merchant portal, or the platform rules that the full amount arrived. A recipient confirming on the hosted page only records `customerConfirmedAt` and sends an event; it does not complete the order.
- **Failed**: the platform rules a full release, `failReason = platform_release`.

**Conventions:**
- Every response is `{ "code": 200, "msg": "success", "data": { } }`; `code = 200` means success. Business rejections still return HTTP 200 and are distinguished by `code` in the body.
- `msg` depends on the language; for `zh-CN` the success message is「成功」. Decide on `code` only; messages are for display.
- When the first language in `Accept-Language` is `zh-CN`, `msg` is Chinese; otherwise it is English.
- Amounts are decimal strings. Times are ISO 8601 with a `+08:00` offset, to the second, e.g. `2026-11-13T15:08:00+08:00`.
- Snapshot fields for steps that have not happened yet are returned as `null`, not omitted.

---

### 1.POST Create Fiat Pay-out Order {#create-fiat-payout-order}

Server-to-server call that creates a CNY fiat pay-out.

POST /t-api/openapi/v1/op/openapi/createFiatPayOutOrder

#### Notes

- **Rate limit**: create, details and list are counted separately. Each API allows at most 120 calls per merchant per 60 seconds; beyond that it returns `429100000` with HTTP status 200.
- **Recipient account**: must be the recipient's own account, with the account name matching the KYC name. Only CNY is supported, and the amount is a whole-number string within the range.
- **Freezing**: on success the merchant's available USDT drops by `totalDebitAmount` immediately, booked per network as shown in `freezeSplit`.
- **Hosted page**:
  - Give `hostedUrl` to the recipient of this order. The link is signed and can be opened until 30 days after the order reaches a terminal state.
  - With a `returnUrl`, the hosted page shows a "Return to merchant" button; without one it does not.
- **Idempotency**: `merchantOrderNo` is the idempotency key per merchant, see [**Idempotency**](#idempotency).

> Request Body

```json
{
  "merchantOrderNo": "B20261113000123",
  "userId": "U10023841",
  "kyc": {
    "name": "Zhang San",
    "idType": "ID_CARD",
    "idNo": "110101199001011234"
  },
  "fiatCurrency": "CNY",
  "fiatAmount": "100000",
  "recipient": {
    "accountName": "Zhang San",
    "accountNo": "6222021234567892223",
    "bankCode": "ICBC"
  },
  "returnUrl": "https://broker.example.com/payout/result"
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | ---------------------------- | -- | ---- |
| body | body | CreateFiatPayOutOrderRequest | Yes | none |

#### CreateFiatPayOutOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | ------ | ----- | -------------------- | ------------------------------------------------------------------------------------------- |
| merchantOrderNo | string | true | 1–64 chars | Merchant order number; idempotency key, unique per merchant, no leading or trailing whitespace |
| userId | string | true | 1–64 chars | Merchant-side customer ID; stored on the order and returned in responses and webhooks |
| kyc | object | true | none | Recipient KYC, see [**kyc Properties**](#kyc) |
| fiatCurrency | string | true | `CNY` only | Case-sensitive |
| fiatAmount | string | true | Positive whole-number string | No decimal point, thousands separators or leading zeros. Inclusive range in [**Amounts and Validation**](#rules), `1000`–`100000` by default |
| recipient | object | true | none | Recipient account, see [**recipient Properties**](#recipient) |
| returnUrl | string | false | `https://`, up to 512 | "Return to merchant" URL on the hosted page. Omit it to hide the button. When passed it must start with lowercase `https://` and contain no whitespace; an empty string is rejected (`500105102`) |

#### kyc Properties {#kyc}

| Name | Type | Required | Constraint | Description |
| ------ | ------ | ---- | ---------------------- | ----------------------------------------------------------------------------------------- |
| name | string | true | 1–100 chars | Recipient name; must be the same person as `recipient.accountName` |
| idType | string | true | `ID_CARD` / `PASSPORT` | `ID_CARD` PRC resident identity card / `PASSPORT` passport; case-sensitive |
| idNo | string | true | 1–64 chars | Before storage, full-width characters are converted to half-width, leading and trailing whitespace is trimmed and letters are upper-cased; no whitespace inside. An ID card is 17 digits plus a final digit or `X`, format only; a passport is 5–20 characters of `A–Z` and digits |

#### recipient Properties {#recipient}

| Name | Type | Required | Constraint | Description |
| ----------- | ------ | ---- | --------- | --------------------------------------------------------------------- |
| accountName | string | true | 1–100 chars | Compared with `kyc.name` after Unicode NFKC normalization, ignoring case and leading/trailing whitespace, with runs of whitespace treated as one space |
| accountNo | string | true | 12–32 digits | The recipient's own bank card number, without spaces |
| bankCode | string | true | 1–16 chars | See [**Bank codes**](#banks); case-sensitive |

- Missing fields and wrong lengths, enums, card numbers, URL formats, or an amount that is not a positive whole-number string are rejected directly by the OpenAPI with `500100000`; `msg` is an English field description, e.g. `fiatAmount must be a positive integer string` or `recipient.accountNo must be 12-32 digits without spaces`.
- ID number format, an empty `returnUrl`, name mismatch, amount range and channel checks return `500105xxx`, see [**Error codes**](#error-codes).

> Response Example

> 200 Response

The rate and amounts in this example only illustrate the format; rely on the actual response.

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "orderId": "1987654321098765400",
    "orderNo": "PZPO20261113-00001",
    "merchantOrderNo": "B20261113000123",
    "userId": "U10023841",
    "status": "pending_accept",
    "statusVersion": 1,
    "executionStatus": "not_started",
    "fiatCurrency": "CNY",
    "fiatAmount": "100000",
    "settlementCurrency": "USDT",
    "merchantRate": "7.1000",
    "merchantAmount": "14084.51",
    "feeAmount": "14.09",
    "totalDebitAmount": "14098.60",
    "freezeSplit": [
      { "network": "TRON", "amount": "14098.60" }
    ],
    "bankCode": "ICBC",
    "accountNo": "6222****2223",
    "createdAt": "2026-11-13T15:08:00+08:00",
    "acceptDeadlineAt": "2026-11-13T16:08:00+08:00",
    "paymentDeadlineAt": null,
    "confirmationDeadlineAt": null,
    "serverTime": "2026-11-13T15:08:00+08:00",
    "paymentTimeout": false,
    "confirmationTimeout": false,
    "customerConfirmedAt": null,
    "confirmationSource": null,
    "completedAt": null,
    "failedAt": null,
    "rejectedAt": null,
    "rejectReason": null,
    "failReason": null,
    "hostedUrl": "https://pay.paypaz.com/fiat-pay-out?token=...",
    "appealId": null,
    "appealSource": null,
    "appealReason": null,
    "appealReasonCode": null,
    "appealRecords": [],
    "payments": []
  }
}
```

#### Response Schema

Status code **200**

| Name | Type | Required | Constraint | Description |
| ------ | ------------------ | ----- | ---- | ------------------------------------------ |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | FiatPayOutOrderVO | false | none | Order snapshot, see [**FiatPayOutOrderVO Properties**](#fiat-payout-order-vo) |

#### FiatPayOutOrderVO Properties {#fiat-payout-order-vo}

The successful create response, and every order returned by the details and list APIs, is this snapshot. On successful creation the status is `pending_accept` and the USDT is already frozen.

| Name | Type | Description |
| ---------------------- | ------------------------ | -------------------------------------------------------------------------------- |
| orderId | string | Internal platform ID as a decimal string. Identify orders by `orderNo` |
| orderNo | string | Paypaz order number: `PZPO` + Shanghai date + `-` + a daily sequence of at least 5 digits, e.g. `PZPO20261113-00001` |
| merchantOrderNo | string | Merchant order number |
| userId | string | Merchant-side customer ID |
| status | string | Order status, see [**Order status**](#status). Replays and queries return the current status |
| statusVersion | integer | Status version, `1` on creation. Increments when the order status or the appeal content changes; use it to discard stale notifications |
| executionStatus | string | Execution progress: `not_started` / `accepted` / `paid` |
| fiatCurrency | string | `CNY` |
| fiatAmount | string | CNY whole-number string, same as the request |
| settlementCurrency | string | Always `USDT` |
| merchantRate | string | Merchant settlement rate Rm, 4 decimals, meaning 1 USDT = Rm CNY |
| merchantAmount | string | Payment amount X in USDT, 2 decimals |
| feeAmount | string | Fee F in USDT, 2 decimals; `"0.00"` when there is no fee |
| totalDebitAmount | string | USDT frozen for this order, equal to `merchantAmount + feeAmount`, 2 decimals |
| freezeSplit | array[FreezeSplitItem] | Freeze breakdown; the `amount` values add up to `totalDebitAmount` |
| bankCode | string | Recipient bank code |
| accountNo | string | Masked card number: first 4 + `****` + last 4 |
| createdAt | string | Creation time |
| acceptDeadlineAt | string | Acceptance deadline, 60 minutes after creation. If nobody accepts by then, the order is rejected and the funds released |
| paymentDeadlineAt | string \| null | Payment deadline, set once a partner accepts: 30 minutes after acceptance |
| confirmationDeadlineAt | string \| null | Confirmation deadline, set once the partner submits a valid payment: 30 minutes after submission |
| serverTime | string | Server time when this snapshot was produced; use it for countdowns |
| paymentTimeout | boolean | `true` once a payment timeout moved the order into an appeal |
| confirmationTimeout | boolean | `true` once a confirmation timeout moved the order into an appeal |
| customerConfirmedAt | string \| null | When the recipient confirmed receipt on the hosted page |
| confirmationSource | string \| null | How the order completed: `merchant_portal` confirmed in the merchant portal / `platform_ruling` platform ruling. `merchant_api` is reserved and does not occur today |
| completedAt | string \| null | Completion time |
| failedAt | string \| null | Failure time |
| rejectedAt | string \| null | Rejection time |
| rejectReason | string \| null | Rejection reason: `partner_rejected` / `accept_timeout` |
| failReason | string \| null | Failure reason: `platform_release` |
| hostedUrl | string | Hosted page URL with path `/fiat-pay-out?token=`. The token is signed and must not be modified |
| appealId | string \| null | Appeal ID |
| appealSource | string \| null | Appeal source, see [**Status and enums**](#status) |
| appealReason | string \| null | Appeal description as written by the party that raised it; empty for system-raised appeals |
| appealReasonCode | string \| null | Appeal reason code, see [**Status and enums**](#status) |
| appealRecords | array[AppealRecordItem] | Appeal records; an empty array on creation |
| payments | array[PaymentItem] | Payment records; an empty array on creation |

The response contains no partner rate, markup, spread or partner details.

#### FreezeSplitItem Properties

| Name | Type | Description |
| ------- | ------ | -------------------------------------- |
| network | string | Network, a chain identifier such as `TRON`, `ETH` or `BSC_BNB` |
| amount | string | USDT frozen on that network, 2 decimals |

#### PaymentItem Properties

| Name | Type | Description |
| ---------------- | ------------- | ------------------------------------------------------------------ |
| paymentId | string | Payment record ID |
| kind | string | `normal` payment / `late` late-reported payment |
| bankTxnNo | string | Bank transaction number |
| bankStatus | string | `processing` / `succeeded` / `returned` / `failed` |
| paidAmount | string | Amount paid in CNY, 2 decimals |
| paidAt | string | Payment time |
| payerBankCode | string | Payer bank code |
| payerAccountTail | string | Last 4 digits of the payer account |
| proofFileIds | array[string] | Payment proof file IDs. There is no download API yet |
| submittedAt | string | Submission time |

#### AppealRecordItem Properties

| Name | Type | Description |
| ---------- | ------------- | --------------------------------------------------------------------------------------- |
| recordId | string | Record ID |
| recordType | integer | `1` opened / `2` evidence / `3` late payment / `4` dispute appended / `5` customer feedback appended / `6` ruling |
| party | string | Submitted by: `platform` / `merchant` / `partner` (collection partner) / `customer` (recipient) |
| reasonCode | string | Reason code: same as `appealReasonCode` for the opening record, `not_received` / `amount_mismatch` for appended disputes, empty otherwise |
| content | string | Description. Empty for ruling records |
| fileIds | array[string] | Attachment file IDs. There is no download API yet |
| createdAt | string | Submission time |

#### Error Codes

- `500105010`: the API key does not have Fiat Pay-out
- `500100000`: invalid parameter format
- `429100000`: rate limit exceeded
- `500105101`–`500105116`: business rejections, see [**Error codes**](#error-codes)

---

### 2.GET Get Fiat Pay-out Order Details {#query-fiat-payout-order}

Gets one order by order number or merchant order number; only orders of the merchant that owns the API key are returned.

GET /t-api/openapi/v1/op/openapi/fiatPayOutOrderInfo

#### Request Parameters

| Name | Location | Type | Required | Description |
| --------------- | ----- | ------ | --- | ----------------------------------- |
| orderNo | query | string | One of two | Paypaz order number; pass only one of this and `merchantOrderNo` |
| merchantOrderNo | query | string | One of two | Merchant order number used at creation |

```
GET /t-api/openapi/v1/op/openapi/fiatPayOutOrderInfo?orderNo=PZPO20261113-00001
GET /t-api/openapi/v1/op/openapi/fiatPayOutOrderInfo?merchantOrderNo=B20261113000123
```

- The signed `requestPath` must include the query string exactly as sent; a GET has no body, so the body part of the signature is an empty string.
- Passing both, neither, or only blanks returns `500105102` with field `orderNo`.
- An order that does not exist and an order of another merchant both return `500105115`; the two cases are not distinguished. A rejected creation creates no order, so querying its `merchantOrderNo` also returns `500105115`.

On success `data` is the [**order snapshot**](#fiat-payout-order-vo), including the current `hostedUrl`, payments and appeal records.

#### Error Codes

- `500105102`: not exactly one order number
- `500105115`: order not found or not accessible
- `500105010`: the API key does not have Fiat Pay-out

---

### 3.POST Query Fiat Pay-out Orders {#query-fiat-payout-order-list}

Pages through the current merchant's pay-out orders. The request body may be omitted or sent as `{}`. Disabling the pay-out configuration does not affect querying past orders. Results are sorted by `createdAt` descending, then by `orderNo` descending.

POST /t-api/openapi/v1/op/openapi/fiatPayOutOrders

> Request Body

```json
{
  "page": 1,
  "pageSize": 20,
  "createdFrom": "2026-11-01T00:00:00+08:00",
  "createdTo": "2026-11-18T00:00:00+08:00",
  "status": ["pending_accept", "pending_payment"]
}
```

#### QueryFiatPayOutOrdersRequest Properties

| Name | Type | Required | Constraint | Description |
| ----------- | -------------- | ----- | -------------- | -------------------------------------------------------- |
| page | integer(int32) | false | ≥ 1 | Starts at 1; defaults to `1` |
| pageSize | integer(int32) | false | 1–100 | Defaults to `20` |
| createdFrom | string | false | ISO 8601 with offset | Lower bound on creation time, inclusive, e.g. `2026-11-01T00:00:00+08:00`; UTC times ending in `Z` are accepted too |
| createdTo | string | false | ISO 8601 with offset | Upper bound on creation time, exclusive. When both are passed it must be later than `createdFrom` |
| status | array[string] | false | See order status | Multiple values allowed, case-sensitive. Omit for all statuses |

- Time filters compare at millisecond precision while snapshot times are shown to the second; keep this in mind when building ranges from second boundaries.
- A page beyond the last one returns an empty `items` array with the same `total`.
- An invalid page, page size, time or status returns `500105102`; see [**Reasons for 500105102**](#invalid-reasons).

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": "1",
    "page": 1,
    "pageSize": 20,
    "items": []
  }
}
```

#### Response Schema

| Name | Type | Description |
| ------------ | ------------------------ | --------------------------------------------------- |
| » total | string | Total number of matching orders, as a decimal string |
| » page | integer(int32) | Page number actually used |
| » pageSize | integer(int32) | Page size actually used |
| » items | array[FiatPayOutOrderVO] | Orders on this page, each an [**order snapshot**](#fiat-payout-order-vo); `[]` when there are none |

---

### 4.Idempotency {#idempotency}

The idempotency key is "this merchant + `merchantOrderNo`". The compared content is `userId`, the normalized name and ID number, `idType`, `fiatCurrency`, `fiatAmount`, the normalized account name, the card number, `bankCode` and `returnUrl`.

| Submitted again | Result |
| --------------------------------------- | ------------------------------------------------------------------------------------------ |
| Same parameters, first attempt created an order | `code = 200` with the order's current snapshot. The status may have moved past `pending_accept`; no second order is created and nothing is frozen again |
| Same parameters, first attempt was rejected (`500105101`–`500105109`) | The same error code as the first time. `msg` is rendered in this request's language, and `data` of `500105104` reflects the currently configured range. To order again, use a new `merchantOrderNo` |
| Same `merchantOrderNo`, different parameters | `500105110` |
| Another request with the same `merchantOrderNo` is in progress | `500105111` if it is still running after 3 seconds, regardless of whether the parameters match. Resubmit the original request later |
| Result could not be confirmed | `500105116`. Retry the original request unchanged; if the order was in fact created, the retry returns its snapshot |

- The replay check runs before the business checks. Omitting `returnUrl` and sending an empty string compare as equal: after a successful creation without `returnUrl`, a replay with `""` returns `200` rather than `500105102`.
- A rejected creation creates no order, so the query APIs cannot find it.

---

### 5.Webhook {#webhook}

Configure callback URLs and subscribe to events under "Developer › Webhooks" in the merchant portal; signing and retries are the same as for other businesses, see [**Webhook Guide**](WEBHOOK_GUIDE.md). Every enabled callback URL receives its own delivery; a URL with an event subscription only receives the subscribed events.

The body is `{ "eventType": "...", "data": { } }`. Fields without a value are omitted from `data`. Amounts, rates and times use the same format as the order snapshot.

**Common fields:**

| Name | Type | Description |
| --------------- | ------- | ------------------------------------------- |
| eventId | string | Event ID (UUID), unique per event and unchanged across retries; use it for deduplication |
| occurredAt | string | When the event happened, ISO 8601 (`+08:00`) |
| orderNo | string | Paypaz order number |
| merchantOrderNo | string | Merchant order number |
| userId | string | Merchant-side customer ID |
| status | string | Order status after the event |
| statusVersion | integer | Status version after the event, consistent with `statusVersion` in the snapshot |
| fiatCurrency | string | `CNY` |
| fiatAmount | string | CNY whole-number string |

**Events:**

| eventType | Trigger | Status after | Extra fields |
| -------------------------------------------- | ----------------------------------------- | ---------------------- | --------------------------------------------------------------------------------------------------------- |
| `transaction.fiat.payout.accepted` | A partner accepted the order | `pending_payment` | `acceptedAt`, `paymentDeadlineAt` (30 minutes after acceptance) |
| `transaction.fiat.payout.proof_submitted` | The partner submitted a payment matching the order amount | `pending_confirmation` | `proofSubmittedAt`, `confirmationDeadlineAt` (30 minutes after submission), `proofFileIds` |
| `transaction.fiat.payout.customer_confirmed` | The recipient confirmed receipt on the hosted page | Unchanged | `customerConfirmedAt` |
| `transaction.fiat.payout.appealed` | The order entered an appeal for the first time | `appealed` | `appealId`, `appealSource`, `appealReason`, `appealReasonCode`, `appealedAt` |
| `transaction.fiat.payout.completed` | The merchant confirmed receipt in the merchant portal, or the platform ruled that the full amount arrived | `completed` | `settlementCurrency`, `merchantRate`, `merchantAmount`, `feeAmount`, `totalDebitAmount`, `confirmationSource`, `completedAt` |
| `transaction.fiat.payout.failed` | The platform ruled a release | `failed` | `failReason`, `failedAt`, `settlementCurrency`, `releasedAmount`, `freezeSplit` |
| `transaction.fiat.payout.rejected` | The partner rejected the order, or nobody accepted it in time | `rejected` | `rejectReason`, `rejectedAt`, `settlementCurrency`, `releasedAmount`, `freezeSplit` |

- **Creation**: a successful creation sends no event; rely on the create response.
- **Mismatched payment**: if the partner's payment does not match the order amount, the order goes straight into an appeal (`appealSource = partner`) and `appealed` is sent instead of `proof_submitted`.
- **During an appeal**: further disputes, evidence or payments on an order that is already in an appeal do not send `appealed` again.
- **Released amount**: `releasedAmount` is the USDT released, equal to the `totalDebitAmount` frozen at creation; `freezeSplit` breaks it down by network.
- **Ordering**: callbacks may arrive out of order. Use `statusVersion` to discard notifications older than your local state, and call the query API when in doubt.

```json
{
  "eventType": "transaction.fiat.payout.completed",
  "data": {
    "eventId": "3f0c9a52-7d1e-4b8a-9c61-2e5f4d7a8b90",
    "occurredAt": "2026-11-13T16:20:05+08:00",
    "orderNo": "PZPO20261113-00001",
    "merchantOrderNo": "B20261113000123",
    "userId": "U10023841",
    "status": "completed",
    "statusVersion": 4,
    "fiatCurrency": "CNY",
    "fiatAmount": "100000",
    "settlementCurrency": "USDT",
    "merchantRate": "7.1000",
    "merchantAmount": "14084.51",
    "feeAmount": "14.09",
    "totalDebitAmount": "14098.60",
    "confirmationSource": "merchant_portal",
    "completedAt": "2026-11-13T16:20:05+08:00"
  }
}
```

---

### Status and Enums {#status}

#### Order status

| status | Terminal | Meaning |
| ---------------------- | -- | -------------------------------------- |
| `pending_accept` | | Pending acceptance; the status right after a successful creation |
| `pending_payment` | | Accepted by a partner, waiting for payment |
| `pending_confirmation` | | The partner submitted a valid payment, waiting for the merchant to confirm |
| `appealed` | | Timeout, amount mismatch or dispute; waiting for a platform ruling |
| `completed` | Yes | Confirmed by the merchant or ruled as fully paid; the frozen USDT has been deducted |
| `failed` | Yes | Released by a platform ruling |
| `rejected` | Yes | Rejected by the partner or not accepted in time; the frozen USDT has been released |

#### appealSource

| Value | Meaning |
| ----------------- | -------------- |
| `timeout` | Payment timeout, raised by the system |
| `confirm_timeout` | Confirmation timeout, raised by the system |
| `merchant` | Dispute submitted by the merchant |
| `partner` | Payment exception reported by the partner |
| `customer` | Feedback from the recipient |

#### appealReasonCode

| Source | Values |
| ------------------ | ---------------------------------------------------------------------------------------------------- |
| `merchant` dispute | `not_received` / `amount_mismatch` |
| `partner` payment exception | `underpaid` / `overpaid` / `split_payment` / `bank_processing` / `returned` / `wrong_account` / `other` |
| Other sources | Empty |

---

### Bank Codes {#banks}

Use a code from the table below for `recipient.bankCode`. A code not in the table returns `500105106`; a code in the table with no pay-out channel currently supporting that bank returns `500105107`.

| Code | Bank | Code | Bank |
| ------- | -------- | ------ | -------- |
| `ICBC` | Industrial and Commercial Bank of China | `CGB` | China Guangfa Bank |
| `CCB` | China Construction Bank | `PAB` | Ping An Bank |
| `ABC` | Agricultural Bank of China | `CIB` | Industrial Bank |
| `BOC` | Bank of China | `SPDB` | Shanghai Pudong Development Bank |
| `BOCOM` | Bank of Communications | `CZB` | China Zheshang Bank |
| `PSBC` | Postal Savings Bank of China | `CBHB` | China Bohai Bank |
| `CMB` | China Merchants Bank | `HFB` | Hengfeng Bank |
| `CITIC` | China CITIC Bank | `BOB` | Bank of Beijing |
| `CEB` | China Everbright Bank | `BOS` | Bank of Shanghai |
| `HXB` | Hua Xia Bank | `JSB` | Bank of Jiangsu |
| `CMBC` | China Minsheng Bank | `NJCB` | Bank of Nanjing |
| | | `NBCB` | Bank of Ningbo |
| | | `HZB` | Bank of Hangzhou |

---

### Error Codes {#error-codes}

Business rejections still return HTTP 200 and are distinguished by `code` in the body. When the first language in `Accept-Language` is `zh-CN`, `msg` is Chinese; otherwise it is English. `data` is `null` unless noted below. Handle errors by code; messages are for display.

#### Authentication and General

All three APIs check these before any business logic, in this order: missing signature headers → timestamp → API key → signature → IP whitelist → scope.

| code | Meaning | Chinese msg | English msg |
| ----------- | ------------------------------------- | ------------ | ------------------------------------------------------------------------------ |
| `500105001` | Signature headers missing | 请求缺少必要的认证信息 | Request missing required authentication information |
| `500105002` | Invalid API key, including disabled keys | 无效的API Key | Invalid API Key |
| `500105003` | Signature verification failed | 签名验证失败 | Signature verification failed |
| `500105004` | Timestamp outside the receive window | 请求时间戳已过期 | Request timestamp has expired |
| `500105005` | Malformed timestamp or receive window | 无效的时间戳格式 | Invalid timestamp format |
| `500105011` | Source IP not in the API key's whitelist (an empty whitelist means no restriction) | IP不在白名单中 | IP address is not in the whitelist |
| `500105010` | The API key does not have Fiat Pay-out | 没有权限访问该API | No permission to access this API |
| `429100000` | Rate limit exceeded (120 per merchant per API per 60 seconds); the HTTP status is still 200 | 访问过于频繁，请稍候再试 | You have exceeded the request limit. Please wait a moment before trying again. |
| `405100000` | Wrong HTTP method, e.g. GET on a POST API. `msg` is always English | `Request method 'GET' is not supported` | `Request method 'GET' is not supported` |
| `500100000` | Missing fields and wrong lengths, enums, card numbers, URL formats, or an amount that is not a positive whole-number string. `msg` is an English field description; several fields are joined with a comma and a space | See meaning | See meaning |

`500100000` has two more messages that follow the language: 「参数类型错误」/ `parameter type error` when the body is not valid JSON or a field has the wrong type, and 「系统异常」/ `System error` for an unexpected runtime error. When the create API returns 「系统异常」/ `System error`, the result is unknown; handle it like `500105116`.

#### Business Rejections

| code | When | Chinese msg | English msg |
| ----------- | -------------------------------------------------------------------------------------- | ----------------------------------- | -------------------------------------------------------------------------------------------------- |
| `500105101` | The merchant does not have fiat pay-out enabled | 法币代付服务未开通，请联系客服。 | Fiat pay-out is not enabled. Please contact customer support. |
| `500105102` | ID number format mismatch, or an empty `returnUrl`. Also returned by the queries when the order number is not exactly one of two, the page or time is invalid, or a status is not recognized. `{0}` and `{1}` are replaced by the field name and the reason | 参数 {0} 不合法：{1} | Invalid parameter {0}: {1} |
| `500105103` | The recipient account name does not match the KYC name, or the name is empty after normalization | 收款账户名须与收款人 KYC 姓名一致 | The recipient account name must match the KYC name of the recipient. |
| `500105104` | The amount is a whole number but outside the platform range | 代付金额需为 {0} - {1} CNY 之间的整数 | The pay-out amount must be a whole number between {0} and {1} CNY. |
| `500105105` | The same ID already has a pay-out in progress with any merchant | 该客户有一笔代付订单正在处理中，请完成后再发起。 | This customer already has a pay-out order in progress. Please try again after it is completed. |
| `500105106` | `bankCode` is not in [**Bank codes**](#banks) | 不支持的收款银行 | The recipient bank is not supported. |
| `500105107` | No pay-out channel can take this amount and bank right now, including channels outside their service window or less than 60 minutes before it ends | 当前没有可用的代付通道，请稍后再试。 | No pay-out channel is available right now. Please try again later. |
| `500105108` | The channel's quota for the current window is used up | 代付通道额度不足，请稍后再试。 | The pay-out channel limit has been reached. Please try again later. |
| `500105109` | The merchant's available USDT cannot cover `X + F`, or the merchant has no USDT account or balance record yet | 账户可用余额不足 | Insufficient available balance. |
| `500105110` | The parameters differ from the first request with the same `merchantOrderNo` | 同一请求标识的参数与首次请求不一致 | The request parameters do not match the first request with the same identifier. |
| `500105111` | Another request with the same `merchantOrderNo` is in progress | 请求处理中，请稍后查询 | The request is being processed. Please query the result later. |
| `500105115` | The queried order does not exist or belongs to another merchant | 无权访问该资源 | You do not have access to this resource. |
| `500105116` | The creation result could not be confirmed: an internal call timed out or failed, the system is busy (the channel lock was not obtained within 3 seconds), the daily order numbers ran out, or another unexpected error occurred. The order may have been created | 处理结果未知，请使用原请求标识重试或查询 | The result is unknown. Please retry with the original request identifier or query the order. |

`500105116` has a second English wording, `The result is unknown. Retry with the same request id, or query the order.`; handle it by code.

`data` of `500105104` carries the current platform range as whole-number strings:

```json
{
  "code": 500105104,
  "msg": "The pay-out amount must be a whole number between 1000 and 100000 CNY.",
  "data": { "minAmount": "1000", "maxAmount": "100000" }
}
```

`500105101` to `500105109` are fixed by the first result. Submitting the same `merchantOrderNo` again returns the first error code even if the balance or channel has recovered since. Only a new merchant order number is validated again.

#### Reasons for 500105102 {#invalid-reasons}

| API | Field | Reason `{1}` |
| -- | ------------- | ------------------------------------------------ |
| Create | `kyc.idNo` | `must not be blank or contain spaces` (empty after normalization, or whitespace inside) |
| Create | `kyc.idNo` | `format does not match ID_CARD` / `format does not match PASSPORT` |
| Create | `returnUrl` | `must be an https URL` (an empty string was passed) |
| Details | `orderNo` | `orderNo 与 merchantOrderNo 二选一` |
| List | `page` | `须大于等于 1` |
| List | `pageSize` | `范围为 1–100` |
| List | `createdFrom` / `createdTo` | `须为带时区的 ISO 8601 时间` |
| List | `createdTo` | `须晚于 createdFrom` |
| List | `status` | `存在无法识别的订单状态` |

The create reasons are English and the query reasons are Chinese, and neither follows `Accept-Language`; only the outer template 「参数 … 不合法」/ `Invalid parameter …` follows the language.

---

### Amounts and Validation {#rules}

- **Amount range**: an inclusive range configured by the platform, 1000–100000 CNY by default. `minAmount` and `maxAmount` in a `500105104` response are authoritative.
- **Settlement**: the rate and fee are locked at creation and shown in the successful response. Do not compute a different rate on the page you show to users.
  - Merchant settlement rate Rm = partner rate Rc × (1 − spread), 4 decimals, truncated.
  - Payment amount X = pay-out amount ÷ Rm, rounded up to the cent.
  - Fee F: the configured USDT amount for a fixed fee, or X × fee rate rounded up to the cent for a percentage fee.
  - Frozen amount = X + F.
- **Example**: 100,000 CNY, Rc 7.2000, spread 1%, fee 0.1% → Rm `7.1280`, X `14029.19`, F `14.03`, frozen `14043.22`.
- **Freeze split**:
  - Funds are taken from the TRON network's available balance first, then from the other networks in descending order of available balance, ties broken by network identifier.
  - Balances are truncated to 2 decimals before the calculation.
  - If the total is not enough, `500105109` is returned; no order is created and nothing is partially frozen.
- **Cross-merchant duplicate check**: while the same ID type + normalized ID number has an unfinished pay-out (pending acceptance, pending payment, pending confirmation or appealed) with any merchant, no new one can be placed; `500105105` is returned.
- **Case sensitivity**: `bankCode`, `fiatCurrency`, `idType` and the list `status` values are case-sensitive, and `returnUrl` must start with lowercase `https://`.
- **Validation order**:
  - Signature → IP whitelist → scope → field format (`500100000`) → rate limit (`429100000`) → idempotent replay.
  - Business checks in order: `500105101` → empty `returnUrl` (`500105102`) → ID number (`500105102`) → `500105103` → `500105106` → `500105104` → `500105105` → `500105107` / `500105108` → `500105109`.
- **Hosted page**:
  - `lang=en` shows English and `lang=zh` shows Chinese; without it the page keeps the recipient's last choice, Chinese by default.
  - The page refreshes every 5 seconds.
  - Pending acceptance and pending payment both show as "pending payment"; rejected shows as failed (not accepted); after the recipient confirms receipt it shows "awaiting merchant confirmation".
  - The countdown follows the deadline of the current stage.
  - The recipient can report a problem only once.
  - With a `returnUrl`, the "Return to merchant" button is shown in every state.
