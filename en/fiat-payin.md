# Fiat Pay-in APIs

All of the following APIs require signature authentication. See [**Authentication API**](README.md) for the common headers and the signing algorithm. The merchant is identified by the API key; do not pass a merchant ID in the body.

The API key must have **Fiat Pay-in** enabled (scope key `fiatPayIn`). Otherwise all three APIs return `500105010`, no order is created and no data is returned. Webhooks are delivered according to the merchant's webhook configuration and do not depend on the API key's scope.

---

### Integration Flow {#flow}

1. The user chooses a fiat deposit on the merchant side. The amount is a whole number of CNY between 200,000 and 500,000. Do not show an exchange rate or an estimated USDT amount on the merchant page.
2. The merchant server calls [**Create Fiat Pay-in Order**](#create-fiat-payin-order) and hands the returned `checkoutUrl` to that user, either as a full-page redirect or in an iframe.
3. The user transfers the funds on the Paypaz hosted checkout and uploads the payment proof. After the collection partner verifies the funds, Paypaz credits USDT to the merchant balance at the rate locked when the order was created, and sends a webhook.
4. The merchant subscribes to the 7 [**Webhook**](#webhook) events. If a callback does not arrive, fall back to [**Get Fiat Pay-in Order Details**](#query-fiat-payin-order) or [**Query Fiat Pay-in Orders**](#query-fiat-payin-order-list).
5. A succeeded order is already credited; the merchant does not need to start a settlement. How to tell the user the result is up to the merchant.

```
POST createFiatPayInOrder → orderNo and checkoutUrl returned
  ↓
The user opens checkoutUrl, transfers and uploads the proof → Webhook proof_submitted
  ↓
The collection partner verifies; during an appeal or refund → Webhook appealed / refunding
  ↓
Terminal state → Webhook completed / refunded / failed / cancelled
  ↓
If a callback is missing → GET fiatPayInOrderInfo to reconcile
```

**Conventions:**
- Every response is `{ "code": 200, "msg": "success", "data": { } }`; `code = 200` means success. Business rejections still return HTTP 200 and are distinguished by `code` in the body.
- `msg` depends on the language; for `zh-CN` the success message is「成功」. Decide on `code` only, never on the text of `msg`.
- When the first language in `Accept-Language` is `zh-CN`, `msg` is Chinese; everything else, including `zh` and `zh-TW`, gets English.
- All amounts are decimal strings. All times are 13-digit millisecond timestamps **returned as strings**, e.g. `"1791622800000"`.
- Card number, account name and bank appear only on the checkout; the query APIs never return them.

---

### 1.POST Create Fiat Pay-in Order {#create-fiat-payin-order}

Server-to-server call that creates a CNY fiat deposit order.

POST /t-api/openapi/v1/op/openapi/createFiatPayInOrder

#### Notes

- **Rate limit**: at most 120 calls per merchant per 60 seconds. Beyond that the API returns `429100000` (the HTTP status is still 200).
- **Idempotency**: `merchantOrderNo` is unique per merchant.
  - If an order already exists for it, the API returns that order's **current** state without comparing the other fields of this request. The order may no longer be `PENDING_PAYMENT`; fields follow the same rules as [**Get Fiat Pay-in Order Details**](#query-fiat-payin-order).
  - If the previous attempt was rejected, this request is validated again from scratch; the previous `code` and `msg` are not kept.
  - A repeated request must still pass parameter validation and amount parsing.
- **Unknown result**: if an internal call fails the API returns `code = 500` with msg「系统异常」/ `System error`. The order may have been created, so retry with the same `merchantOrderNo` or query the order.
- See [**Validation order**](#create-validation-order).

> Request Body

```json
{
  "merchantOrderNo": "B20261010000123",
  "userId": "U10023841",
  "fiatCurrency": "CNY",
  "fiatAmount": "216000",
  "customer": {
    "name": "Zhang San",
    "idType": "ID_CARD",
    "idNo": "110101199001011234",
    "country": "CN",
    "ethnicity": "01",
    "dateOfBirth": "1990-05-20"
  },
  "returnUrl": "https://broker.example.com/deposit/result"
}
```

#### Request Parameters

| Name | Location | Type | Required | Description |
| ---- | ---- | --------------------------- | -- | ---- |
| body | body | CreateFiatPayInOrderRequest | Yes | none |

#### CreateFiatPayInOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | ------ | ----- | -------------------------- | ------------------------------------------------------------------ |
| merchantOrderNo | string | true | 1–64 chars, `[A-Za-z0-9_-]` | Merchant order number; idempotency key, unique per merchant |
| userId | string | true | 1–64 chars | Merchant-side customer ID. Stored on the order and returned in queries, reconciliation and webhooks |
| fiatCurrency | string | true | `CNY` only | Case-sensitive; `cny` is rejected |
| fiatAmount | string | true | Whole number, 200000–500000 inclusive | Write a plain whole number without decimal point, thousands separators or leading zeros. Numerically equal forms such as `216000.00` are accepted; thousands separators or a non-zero fraction are rejected |
| customer | object | true | none | Customer profile, see [**customer Properties**](#customer) |
| returnUrl | string | true | `http://` or `https://`, up to 1024 | "Return to merchant" URL on the result page, per order. No whitespace; the scheme must be lowercase |

#### customer Properties {#customer}

| Name | Type | Required | Constraint | Description |
| ----------- | ------ | ----- | ------------------------------ | -------------------------------------------------------- |
| name | string | true | 1–64 chars | Customer name; leading and trailing spaces are trimmed |
| idType | string | true | `ID_CARD` / `PASSPORT` | Case-sensitive, see [**KYC enums**](#kyc) |
| idNo | string | true | 1–64 chars | Spaces are removed and letters upper-cased before storage and before the cross-merchant duplicate check |
| country | string | true | `CN` / `HK` / `MO` / `TW` | Case-sensitive |
| ethnicity | string | false | `01`–`56` | GB/T 3304 two-digit code. If omitted, the order is never assigned to a card with an ethnicity condition |
| dateOfBirth | string | false | `YYYY-MM-DD` | Must not be later than today in Asia/Shanghai. If omitted, the order is never assigned to a card with an age condition |

- The 64-character limits on `name`, `idNo` and `userId` apply to the raw value before trimming.
- An invalid enum, date, URL or length returns `500100000` with the field named in `msg`; see [**Invalid parameters**](#invalid-params).
- An amount that is not a whole number, is out of range or cannot be parsed returns scenario ⑥ `500105052`.

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "orderNo": "PZPI20261010-00001",
    "merchantOrderNo": "B20261010000123",
    "userId": "U10023841",
    "fiatCurrency": "CNY",
    "fiatAmount": "216000.00",
    "status": "PENDING_PAYMENT",
    "checkoutUrl": "https://pay.paypaz.com/fiat-deposit?pageId=mF3n0Q2sX6v9rT1bK8yW4zC7pL5aE2uH0jN9dG3kV6qR8tY1oI4wS7xZb5Lc2",
    "expireAt": "1791622800000",
    "createdAt": "1791621000000"
  }
}
```

#### Response Schema

Status code **200**

| Name | Type | Required | Constraint | Description |
| ------ | ----------------------- | ----- | ---- | ---- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | FiatPayInOrderOpenApiVO | false | none | none |

#### FiatPayInOrderOpenApiVO Properties {#fiat-payin-order-vo}

Create, single-order query and paged query all return this structure. Fields for steps that have not happened yet are omitted; do not treat them as 0.

| Name | Type | Present when | Description |
| ----------------- | ------ | --------------------- | -------------------------------------------------------------------------------------- |
| orderNo | string | Always | Paypaz order number: `PZPI` + Shanghai date + `-` + a daily sequence of at least 5 digits, e.g. `PZPI20261010-00001`. Used end to end |
| merchantOrderNo | string | Always | Merchant order number |
| userId | string | Always | Merchant-side customer ID |
| fiatCurrency | string | Always | `CNY` |
| fiatAmount | string | Always | Order amount in CNY with two decimals. The request takes a whole number; the response adds `.00` |
| status | string | Always | Order status, see [**Order status**](#status). Always `PENDING_PAYMENT` on creation |
| checkoutUrl | string | Always | Hosted checkout URL with path `/fiat-deposit?pageId=`. See the notes below |
| expireAt | string | Always | End of the payment window, 30 minutes after creation. Without a proof by then the order becomes `CANCELLED` |
| createdAt | string | Always | Creation time |
| proofSubmittedAt | string | Proof submitted | When the user submitted the payment proof |
| settlementRate | string | `SUCCEEDED` | Merchant settlement rate Rm, meaning 1 USDT = Rm CNY, 4 decimals |
| creditAmount | string | `SUCCEEDED` | USDT credited to the merchant, 2 decimals |
| feeAmount | string | `SUCCEEDED` | Deposit fee in USDT, 2 decimals; `"0.00"` when there is no fee |
| netCreditAmount | string | `SUCCEEDED` | `creditAmount − feeAmount`, 2 decimals |
| completedAt | string | `SUCCEEDED` | When the collection was confirmed or the ruling took effect |
| refundAmount | string | `REFUNDING` / `REFUNDED` | Actual refund in CNY, 2 decimals; may be less than the order amount |
| refundSubmittedAt | string | The partner returned a refund proof | When the refund proof was returned; updated if it is returned again after a rejection. Orders that operations marked as refunded manually do not have this field |
| refundedAt | string | `REFUNDED` | When Paypaz confirmed the refund or operations marked it as refunded |
| failReason | string | `FAILED` | `USER_NOT_PAID` / `FORGED_PROOF` / `WRONG_ACCOUNT` / `OTHER` |
| failedAt | string | `FAILED` | Failure time |
| cancelledAt | string | `CANCELLED` | Cancellation time, equal to `expireAt` |

- **`checkoutUrl`**:
  - The URL string may differ on every response, but it always points to the same order and stays valid; after a terminal state it opens the result page. Do not compare or deduplicate on it.
  - `pageId` is an encrypted token that cannot be parsed; any change to it makes the link invalid.
- **Settlement fields**: returned only when the order is `SUCCEEDED`; a newly created order has none.
- **Never returned**: the collection partner's rate Rc, the spread, the partner's name, and the card number, account name and bank.
- **Appeal details**: provided only in the `appealed` webhook; the query APIs do not return them.

#### Validation Order {#create-validation-order}

The request is rejected at the first failing step and no order is created:

1. Authentication: missing signature headers → timestamp → API key → signature → IP whitelist → scope.
2. Request body field format (`500100000`).
3. Rate limit (`429100000`).
4. `fiatCurrency` must be `CNY` (`500100000`).
5. The amount must parse as a number, otherwise scenario ⑥.
6. Date of birth is valid and not later than today (`500100000`).
7. If an order already exists for this merchant order number, it is returned as is.
8. Business checks: ⑦ not enabled → ⑥ amount → ① duplicate → ② qualification → one reason by priority ④ > ③ > ⑤.

Business rules:
- **Cross-merchant duplicate check**: if the same ID type + ID number already has an order in progress (pending payment, verifying, appealing or refunding) with any merchant, scenario ① is returned. This is separate from merchant order number idempotency.
- **Card assignment**:
  - Cards are matched on single-order amount, country, ethnicity and age. When several cards match, they rotate by "least recently assigned".
  - A condition a card does not set means "no restriction". If a card sets a condition and the order lacks the matching profile field, the card does not match.
- **When every qualifying card is unavailable**, only one reason is returned, by priority: insufficient withdrawable balance (④) > outside the service window or not enough quota left in the current window (③) > card disabled, partner disabled, rate unavailable or the partner's pay-in business switched off (⑤).
- **Service window**: Beijing time, start inclusive and end exclusive. New orders can be assigned throughout the window and none outside it.
- **Suggested window for scenario ③**: when a next window can be suggested, `data` carries `startTime` / `endTime` as `HH:mm` without a date. It is the earliest-starting window among the qualifying enabled cards: today's window if it has not started yet, otherwise tomorrow's.

```json
{
  "code": 500105049,
  "msg": "Service is paused for the current period. Please try again between 09:00 and 17:00.",
  "data": { "startTime": "09:00", "endTime": "17:00" }
}
```

#### Error Codes

- `500105010`: the API key does not have Fiat Pay-in
- `500100000`: invalid parameter, see [**Invalid parameters**](#invalid-params)
- `429100000`: rate limit exceeded
- `500105047`–`500105053`: creation rejected, see [**Creation rejections**](#create-rejections)
- `500`: unknown result; retry with the same `merchantOrderNo` or query the order

---

### 2.GET Get Fiat Pay-in Order Details {#query-fiat-payin-order}

Gets one order by order number or merchant order number. Only orders of the current merchant are visible.

GET /t-api/openapi/v1/op/openapi/fiatPayInOrderInfo

#### Request Parameters

| Name | Location | Type | Required | Description |
| --------------- | ----- | ------ | -- | ---------- |
| orderNo | query | string | No | Paypaz order number |
| merchantOrderNo | query | string | No | Merchant order number |

- Pass at least one of `orderNo` and `merchantOrderNo`; with neither, the API returns `500105008`.
- If both are passed they must refer to the same order, otherwise the order is treated as not found and `500105054` is returned.
- The signed `requestPath` must include the query string exactly as sent, e.g. `/t-api/openapi/v1/op/openapi/fiatPayInOrderInfo?orderNo=PZPI20261010-00001`.

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "orderNo": "PZPI20261010-00001",
    "merchantOrderNo": "B20261010000123",
    "userId": "U10023841",
    "fiatCurrency": "CNY",
    "fiatAmount": "216000.00",
    "status": "SUCCEEDED",
    "checkoutUrl": "https://pay.paypaz.com/fiat-deposit?pageId=Vq7pL2nX9sK4tR8bC1yW6zM3dF5aE0uH2jN7gG9kQ4rS8vY1oI6wT3xZc5Ld0",
    "expireAt": "1791622800000",
    "createdAt": "1791621000000",
    "proofSubmittedAt": "1791621600000",
    "settlementRate": "7.2720",
    "creditAmount": "29702.97",
    "feeAmount": "148.52",
    "netCreditAmount": "29554.45",
    "completedAt": "1791624300000"
  }
}
```

#### Response Schema

Status code **200**

| Name | Type | Required | Constraint | Description |
| ------ | ----------------------- | ----- | ---- | ------------------------------------------- |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | FiatPayInOrderOpenApiVO | false | none | See [**FiatPayInOrderOpenApiVO Properties**](#fiat-payin-order-vo) |

#### Error Codes

- `500105008`: neither `orderNo` nor `merchantOrderNo` was passed
- `500105054`: the order does not exist, belongs to another merchant, or the two numbers refer to different orders
- `500105010`: the API key does not have Fiat Pay-in

---

### 3.POST Query Fiat Pay-in Orders {#query-fiat-payin-order-list}

Pages through the fiat pay-in orders of the merchant that owns the API key, sorted by `createdAt` descending. The request body may be omitted.

POST /t-api/openapi/v1/op/openapi/fiatPayInOrders

> Request Body

```json
{
  "merchantOrderNo": "B20261010000123",
  "orderNo": "PZPI20261010-00001",
  "userId": "U10023841",
  "statuses": ["SUCCEEDED", "REFUNDED"],
  "createdStart": 1791532800000,
  "createdEnd": 1791619199000,
  "pageNo": 1,
  "pageSize": 20
}
```

#### QueryFiatPayInOrderRequest Properties

| Name | Type | Required | Constraint | Description |
| --------------- | -------------- | ----- | ----------- | ------------------------------------- |
| merchantOrderNo | string | false | none | Exact match |
| orderNo | string | false | none | Exact match |
| userId | string | false | none | Exact match |
| statuses | array[string] | false | See order status | Empty or omitted means all |
| createdStart | integer(int64) | false | 13-digit ms | Creation time from, inclusive. Pass together with `createdEnd` or not at all |
| createdEnd | integer(int64) | false | 13-digit ms | Creation time to, inclusive. The range must not exceed 30 days |
| pageNo | integer(int32) | false | ≥ 1 | Defaults to 1 |
| pageSize | integer(int32) | false | 1–100 | Defaults to 20 |

> Response Example

> 200 Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": "1",
    "pageNo": 1,
    "pageSize": 20,
    "list": []
  }
}
```

#### Response Schema

| Name | Type | Description |
| ------------ | ------------------------------ | ---------------------------------------- |
| » total | string | Total number of matching orders, as a decimal string |
| » pageNo | integer(int32) | Page number |
| » pageSize | integer(int32) | Page size |
| » list | array[FiatPayInOrderOpenApiVO] | Each item is the same as the single-order `data`, see [**FiatPayInOrderOpenApiVO Properties**](#fiat-payin-order-vo) |

- An order whose payment window has expired but that the system has not cancelled yet is still filtered under `PENDING_PAYMENT` by `statuses`, while its returned `status` already shows `CANCELLED`.

#### Error Codes

- `500105036`: only one of `createdStart` and `createdEnd` was passed
- `500105037`: `createdStart` is later than `createdEnd`
- `500105038`: the range exceeds 30 days
- `500105039`: the time is not a 13-digit millisecond timestamp
- `500100000`: invalid page number or page size, or an unknown value in `statuses`
- `500105010`: the API key does not have Fiat Pay-in

---

### 4.Webhook {#webhook}

Configure callback URLs and subscribe to events under "Developer › Webhooks" in the merchant portal. Every enabled callback URL receives its own delivery; a URL with an event subscription only receives the subscribed events. See [**Webhook Guide**](WEBHOOK_GUIDE.md) for signing, headers and retries. In short:

| Item | Value |
| ---- | ------------------------------------------------------------------------- |
| Method | `POST`, `Content-Type: application/json` |
| Signature headers | `PAYPAZ-WEBHOOK-SIGN`, `PAYPAZ-WEBHOOK-TIMESTAMP` |
| Signed string | `payload + "\|" + timestamp`, HMAC-SHA256 then Base64. The key is the webhook public key generated in the merchant portal (one per merchant, shared by all callback URLs); without a key, requests carry no signature headers |
| Timeout | 5 seconds each for connect and read |
| Success | The response body contains `success` (case-insensitive); the HTTP status is not checked |
| Retries | Up to 6 retries after a failed first delivery (7 deliveries in total), about 1, 2, 3, 4, 5 and 6 minutes apart |

The body has only `eventType` and `data`. The common fields of `data` are `orderNo`, `merchantOrderNo`, `userId` and `fiatAmount`.

| eventType | Trigger | Extra fields |
| ----------------------------------------- | ----------------------------------------- | ---------------------------------------------------------- |
| `transaction.fiat.payin.proof_submitted` | The user submitted the proof; the order is verifying | `proofSubmittedAt` |
| `transaction.fiat.payin.appealed` | The order entered an appeal (raised by the partner or the merchant) | `appealType`, `appealedAt` |
| `transaction.fiat.payin.refunding` | The partner returned a refund proof. Sent again if the proof is returned again after a rejection | `refundAmount`, `refundSubmittedAt` |
| `transaction.fiat.payin.completed` | Collection confirmed, or settled automatically after a "no abnormality / funds arrived" ruling | `settlementRate`, `creditAmount`, `feeAmount`, `completedAt` |
| `transaction.fiat.payin.refunded` | Paypaz confirmed the refund, or operations marked it as refunded | `refundAmount`, `refundedAt` |
| `transaction.fiat.payin.cancelled` | No proof within the payment window | `cancelledAt` |
| `transaction.fiat.payin.failed` | Ruled that the funds did not arrive | `failReason`, `failedAt` |

**The payload format differs from the query APIs:**
- Amounts are decimal strings without trailing zeros, e.g. `"216000"`, `"7.272"`; a zero fee is `"0"`.
- Times are millisecond timestamps as strings.
- The payload has no `fiatCurrency`, `status` or `netCreditAmount`. The event name already identifies the status; compute the net credit as `creditAmount − feeAmount`.

```json
{
  "eventType": "transaction.fiat.payin.completed",
  "data": {
    "orderNo": "PZPI20261010-00001",
    "merchantOrderNo": "B20261010000123",
    "userId": "U10023841",
    "fiatAmount": "216000",
    "settlementRate": "7.272",
    "creditAmount": "29702.97",
    "feeAmount": "148.52",
    "completedAt": "1791624300000"
  }
}
```

- **`appealType` values**: `ARRIVAL_ABNORMAL` abnormal arrival / `NOT_RECEIVED` no incoming funds found / `MERCHANT` merchant appeal.
- **When nothing is sent**:
  - A synchronous creation rejection sends nothing. A rejected creation is different from the asynchronous terminal state "failed": the former is an error code of the create API, the latter is `transaction.fiat.payin.failed`.
  - A rejected refund proof sends nothing; the order goes back to appealing, and `refunding` is sent again once the partner returns a new proof.
  - When operations mark an order as refunded manually, `refunded` is sent directly without a preceding `refunding`.
- **When `cancelled` is sent**: a scheduled job sends it after the payment window expires. `cancelledAt` equals `expireAt`, not the delivery time.
- **Deduplication**: callbacks carry no event ID. Deduplicate on `orderNo + eventType + that event's time field`. Deduplicating on order number + event name alone drops a second `refunding`.
- **What `completed` contains**: no partner rate Rc and no spread. The fee is charged separately from the merchant balance; `creditAmount` is not net of the fee.

---

### Order Status {#status}

Each order ends in exactly one irreversible terminal state. The checkout shows verifying, appealing and refunding all as "Processing"; the merchant APIs and webhooks use the detailed statuses below.

| status | Terminal | Meaning |
| ----------------- | -- | -------------------------------- |
| `PENDING_PAYMENT` | | Created, waiting for the user to transfer and upload the proof |
| `VERIFYING` | | Proof submitted |
| `APPEALING` | | Under platform ruling, or ruled for refund and waiting for the refund proof |
| `REFUNDING` | | Refund proof returned, waiting for Paypaz to confirm. Goes back to appealing if rejected |
| `SUCCEEDED` | Yes | USDT credited |
| `REFUNDED` | Yes | Refunded to the original account; nothing credited to the merchant |
| `FAILED` | Yes | Funds did not arrive; no refund |
| `CANCELLED` | Yes | Payment window expired without a proof |

---

### KYC Enums {#kyc}

#### idType

| Value | Meaning |
| ---------- | ----- |
| `ID_CARD` | PRC resident identity card |
| `PASSPORT` | Passport |

#### country

| Value | Meaning |
| ---- | ---- |
| `CN` | Mainland China |
| `HK` | Hong Kong, China |
| `MO` | Macao, China |
| `TW` | Taiwan, China |

A card's country condition can currently only be `CN` or unrestricted, so customers from `HK`, `MO` and `TW` can only be assigned to cards without a country condition.

#### ethnicity (GB/T 3304)

| Code | Ethnicity | Code | Ethnicity | Code | Ethnicity | Code | Ethnicity |
| -- | ----- | -- | ----- | -- | ----- | -- | ---- |
| 01 | Han | 02 | Mongol | 03 | Hui | 04 | Tibetan |
| 05 | Uyghur | 06 | Miao | 07 | Yi | 08 | Zhuang |
| 09 | Buyei | 10 | Korean | 11 | Manchu | 12 | Dong |
| 13 | Yao | 14 | Bai | 15 | Tujia | 16 | Hani |
| 17 | Kazakh | 18 | Dai | 19 | Li | 20 | Lisu |
| 21 | Wa | 22 | She | 23 | Gaoshan | 24 | Lahu |
| 25 | Sui | 26 | Dongxiang | 27 | Nakhi | 28 | Jingpo |
| 29 | Kyrgyz | 30 | Tu | 31 | Daur | 32 | Mulao |
| 33 | Qiang | 34 | Blang | 35 | Salar | 36 | Maonan |
| 37 | Gelao | 38 | Xibe | 39 | Achang | 40 | Pumi |
| 41 | Tajik | 42 | Nu | 43 | Uzbek | 44 | Russian |
| 45 | Evenki | 46 | De'ang | 47 | Bonan | 48 | Yugur |
| 49 | Gin | 50 | Tatar | 51 | Derung | 52 | Oroqen |
| 53 | Hezhen | 54 | Monba | 55 | Lhoba | 56 | Jino |

---

### Error Codes {#error-codes}

Business rejections still return HTTP 200 and are distinguished by `code` in the body. When the first language in `Accept-Language` is `zh-CN`, `msg` is Chinese; otherwise it is English. `data` is `null` unless noted below. Handle errors by code and do not change the meaning of the messages.

Create, single-order query and paged query share the codes below. Do not reuse the crypto payment order codes `500105033`–`500105035`. Note that in the deposit address API `500105047` means "Meta Trader is not enabled"; interpret it per API.

#### Creation Rejections {#create-rejections}

| Scenario | code | Meaning | Chinese msg | English msg |
| ---------- | ----------- | ------------------------------------------------------------------------------------- | -------------------------- | ------------------------------------------------------------------------------------ |
| ① Duplicate order | `500105047` | The same ID type + ID number already has an order in progress (pending payment, verifying, appealing or refunding); checked across merchants | 您有一笔入金订单正在处理中，请完成后再发起。 | You have a deposit order in progress. Please complete it before starting a new one. |
| ② Qualification mismatch | `500105048` | No card at all, enabled or disabled, matches the single-order amount, country, ethnicity and age; also returned when there are no cards. An amount inside the platform range that fits no card's single-order range also gets this code | 不满足服务商要求，请联系客服。 | Does not meet the service provider's requirements. Please contact support. |
| ③ Paused, next window known | `500105049` | The matched cards are outside their service window, or the current window does not have enough quota left for this order, and a next window can be computed. `data.startTime` / `data.endTime` are `HH:mm` | 当前时段服务暂停，请于 09:00 - 17:00 再试。 | Service is paused for the current period. Please try again between 09:00 and 17:00. |
| ③ Paused, no next window | `500105049` | Same code as the row above. Without a suggested window `data` is `null` and the message differs | 当前时段服务暂停，请稍后再试。 | Service is paused for the current period. Please try again later. |
| ④ Busy | `500105050` | The matched collection partner does not have enough USDT to freeze. Also returned, with the same message, when a creation lock (same merchant order number, same ID, same partner) cannot be taken within 5 seconds | 当前服务繁忙，请联系客服，稍后再试。 | The service is busy. Please contact customer support or try again later. |
| ⑤ Unavailable | `500105051` | Card disabled, partner disabled, rate unavailable, or the partner's pay-in business switched off | 当前服务不可用，请联系客服 | The service is unavailable. Please contact customer support. |
| ⑥ Invalid amount | `500105052` | The amount is not a whole number, is outside the platform range 200,000–500,000, or `fiatAmount` cannot be parsed as a number. The range in the message is fixed and does not follow the cards' single-order ranges | 入金金额需为 200,000 - 500,000 CNY 之间的整数 | The deposit amount must be a whole number between 200,000 and 500,000 CNY. |
| ⑦ Not enabled | `500105053` | The merchant has no enabled CNY/USDT fiat pay-in configuration | 服务暂未开通，请联系客服。 | Service is not available. Please contact customer support. |

#### Query and List

| code | API | Meaning | Chinese msg | English msg |
| ----------- | -- | ---------------------------------------- | -------------------- | ------------------------------------------------------------------- |
| `500105054` | Single | The order does not exist or belongs to another merchant. Also returned when both numbers are passed but refer to different orders | 订单不存在 | Order not found |
| `500105008` | Single | Neither `orderNo` nor `merchantOrderNo` was passed | 查询参数不能同时为空 | Query parameters cannot all be empty simultaneously |
| `500105036` | List | Only one of `createdStart` and `createdEnd` was passed | 开始时间和结束时间不能为空 | Order query time cannot be null |
| `500105037` | List | `createdStart` is later than `createdEnd` | 开始时间不能大于结束时间 | Start time must not be later than end time |
| `500105038` | List | The range exceeds 30 days | 开始时间与结束时间间隔不能超过30天 | The interval between start time and end time cannot exceed 30 days |
| `500105039` | List | The time is not a 13-digit millisecond timestamp | 时间格式错误，请传入正确的13位毫秒时间戳 | Invalid time format. Please enter a valid 13-digit millisecond timestamp. |

#### Authentication and General

All three APIs check these before any business logic, in this order: missing signature headers → timestamp → API key → signature → IP whitelist → scope.

| code | Meaning | Chinese msg | English msg |
| ----------- | ----------------------------------- | --------------- | ------------------------------------------------------------------------------ |
| `500105001` | Signature headers missing | 请求缺少必要的认证信息 | Request missing required authentication information |
| `500105002` | Invalid API key, including disabled keys | 无效的API Key | Invalid API Key |
| `500105003` | Signature verification failed | 签名验证失败 | Signature verification failed |
| `500105004` | Timestamp outside the receive window | 请求时间戳已过期 | Request timestamp has expired |
| `500105005` | Malformed timestamp or receive window | 无效的时间戳格式 | Invalid timestamp format |
| `500105011` | Source IP not in the API key's whitelist (an empty whitelist means no restriction) | IP不在白名单中 | IP address is not in the whitelist |
| `500105010` | The API key does not have Fiat Pay-in | 没有权限访问该API | No permission to access this API |
| `429100000` | Create API rate limit exceeded (120 per merchant per 60 seconds); the HTTP status is still 200 | 访问过于频繁，请稍候再试 | You have exceeded the request limit. Please wait a moment before trying again. |
| `500` | An internal call failed and the result is unknown. For creation, retry with the same `merchantOrderNo` or query the order | 系统异常 | System error |

#### Invalid Parameters `500100000` {#invalid-params}

`data` is `null`. Field validation messages are English field descriptions and do not follow `Accept-Language`. When several fields are invalid, the messages are joined with a comma and a space in one `msg`, in no fixed order, and one field may appear more than once. `fiatCurrency must be CNY`, the date of birth checks, the paging check and the `statuses` check run only after the other fields pass, and return one message at a time.

| API | msg | Meaning |
| -- | -------------------------------------------------------------------- | ------------------------------------------ |
| Create | `merchantOrderNo must not be blank` | Merchant order number is empty |
| Create | `merchantOrderNo length must be between 1 and 64` | Merchant order number is not 1–64 characters |
| Create | `merchantOrderNo contains invalid characters` | Contains characters outside `[A-Za-z0-9_-]` |
| Create | `userId must not be blank` | Customer ID is empty |
| Create | `userId length must be at most 64` | Customer ID exceeds 64 characters |
| Create | `fiatCurrency must not be blank` | Currency is empty |
| Create | `fiatCurrency must be CNY` | Currency is not `CNY` |
| Create | `fiatAmount must not be blank` | Amount is empty. An amount that cannot be parsed returns `500105052` instead |
| Create | `customer must not be null` | `customer` is missing |
| Create | `customer.name must not be blank` | Name is empty |
| Create | `customer.name length must be at most 64` | Name exceeds 64 characters |
| Create | `customer.idType must not be blank` | ID type is empty |
| Create | `customer.idType must be ID_CARD or PASSPORT` | ID type is neither value |
| Create | `customer.idNo must not be blank` | ID number is empty |
| Create | `customer.idNo length must be at most 64` | ID number exceeds 64 characters |
| Create | `customer.country must not be blank` | Country is empty |
| Create | `customer.country must be CN, HK, MO or TW` | Country is not one of the four values |
| Create | `customer.ethnicity is invalid` | Ethnicity is neither empty nor `01`–`56` |
| Create | `customer.dateOfBirth must be YYYY-MM-DD` | Wrong date format, or not a real date |
| Create | `customer.dateOfBirth must not be after today` | Date of birth is later than today in Asia/Shanghai |
| Create | `returnUrl must not be blank` | Return URL is empty |
| Create | `returnUrl length must be at most 1024` | Return URL exceeds 1024 characters |
| Create | `returnUrl must start with http:// or https://` | Return URL is not an http(s) URL |
| List | `pageNo must be at least 1 and pageSize must be between 1 and 100` | Page number below 1, or page size outside 1–100 |
| List | `statuses contains an unknown value` | A status that is not one of the 8 values in [**Order status**](#status) |
| All | `参数类型错误` / `parameter type error` | The body is not valid JSON, or a field has the wrong type. This one follows the language |
| All | `系统异常` / `System error` | Unexpected runtime error. This one follows the language |

```json
{
  "code": 500100000,
  "msg": "customer.country must be CN, HK, MO or TW",
  "data": null
}
```

---

### Amounts, Idempotency and Edge Cases {#rules}

- **Rate lock**: the rate, markup and fee are locked when the order is created. Later changes only affect new orders; settlement uses the snapshot on the order.
- **Settlement**:
  - Merchant settlement rate Rm = partner rate Rc × (1 + markup), 4 decimals, rounded up.
  - Credit `creditAmount` = order amount ÷ Rm, rounded down to 2 decimals.
  - Fee `feeAmount`: `creditAmount × fee rate` for a percentage fee, or the configured USDT amount for a fixed fee; both rounded up to 2 decimals.
  - Net credit `netCreditAmount` = `creditAmount − feeAmount`.
- **Example**: 216,000 CNY, partner rate 7.20, markup 1%, fee 0.5% → Rm `7.2720`, credit `29702.97`, fee `148.52`, net credit `29554.45`.
- **Credit network**: USDT is credited to the credit network in the merchant's fiat pay-in configuration, TRON by default.
- **Idempotency**: an order created for a `merchantOrderNo` can always be fetched again; a previous rejection is validated again with the new request. Concurrent requests are serialized by a lock on "merchant + merchant order number".
- **Duplicate check scope**: orders in progress are checked by ID across all merchants. Cancelled, failed, succeeded and refunded orders do not block a new order. Briefly after the payment window expires and before the system cancels the order, it may still count as in progress and trigger scenario ①.
- **Checkout**:
  - The language follows the browser: Chinese for `zh*`, English otherwise; the user can switch on the page. The link carries no language parameter.
  - The checkout has no appeal entry.
  - If the signature was tampered with, the order does not exist or the page request fails, the checkout shows "invalid link" without a return-to-merchant button.
  - The page refreshes the status every 10 seconds. If the order is still processing 30 minutes after the proof was submitted, it shows a timeout notice.
  - The "Return to merchant" button appears only on terminal pages and after a processing timeout.
  - "Return to merchant" navigates the top-level window. When the checkout is embedded in an iframe with `sandbox`, allow top navigation (`allow-top-navigation`).
  - Give `checkoutUrl` only to the user of that order.
- **Merchant appeal**: raised in the merchant portal under "Fiat Pay-in/Pay-out › Fiat Pay-in Records", not through these OpenAPIs. Allowed only while the order is verifying, at least 30 minutes after the proof was submitted, and only if the order has never been appealed.
