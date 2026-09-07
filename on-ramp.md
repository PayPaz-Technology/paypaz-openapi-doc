# On-Ramp 法币入金业务接口

以下业务接口都需要签名认证。公共 Header、签名算法见 [认证 API 文档](https://bdy-tech.gitbook.io/paypaz/docs?fallback=true)。商户 ID 由 API Key 识别，无需在 Body 中传递。

---

### 对接流程 {#flow}

KYC 绑定在终端用户上（同一商户下固定 `clientSubUserId`），不绑定某一笔订单。建单返回同时给出**订单 `status`** 和 **KYC `kycStatus`**。新用户订单一般为 `KYC_REQUIRED`，必须先走 initiate 把资料同步到 PayPaz。

**引导用户只打开建单返回的 `embedUrl`**（订单托管页）。页内会完成剩余 KYC，通过后同一页继续选支付方式并下单。`kycCheckoutUrl` 是「只有 KYC、没有订单」的页面，**不要用来测支付**。

```
新用户:
  POST /onramp/kyc/initiate   （有 shareToken 则复用 Sumsub，否则用户填写）
       ↓
  POST /onramp/orders         → status=KYC_REQUIRED，返回 embedUrl
       ↓
  打开 embedUrl               → 页内补 KYC → 激活报价 → 支付

老用户（kycStatus=APPROVED）:
  跳过 initiate
       ↓
  POST /onramp/orders         → 通常直接 CREATED（已报价）
       ↓
  打开 embedUrl               → 支付
```

- initiate 可在建单前或建单后调用。新用户建议**先 initiate 再建单**，打开托管页时 profile 已预填。
- 报价、证件/活体、支付控件由托管页调用，商户不必对接这些内部接口。
- 查单笔用 `GET /onramp/orders/info?brokerOrderRef=`；按时间列表用 `POST /onramp/orders/query`。

---

### 1. POST 同步 KYC 资料（initiate） {#sync-kyc-profile}

按 `clientSubUserId` 提交并归档 KYC 资料，不依赖已有订单。**不必传 `mode`**：请求带了非空 `shareToken` 即走 Sumsub 复用；未带则走托管页用户填写（证件/活体在页内完成）。

POST `/t-api/openapi/v1/op/openapi/onramp/kyc/initiate`

> Body（用户填写，无 shareToken）

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

> Body（Sumsub 复用，带 shareToken）

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

#### OnRampKycInitiateRequest

| 名称 | 类型 | 必选 | 说明 |
| --- | --- | --- | --- |
| clientSubUserId | string | true | 终端用户 ID，同一用户必须固定 |
| email | string | true | 终端客户邮箱 |
| shareToken | string | 否 | 非空则走 Sumsub 复用；为空则用户在托管页填写。一次性，勿长期落库 |
| userIp | string | 建议 | 终端用户 IP |
| profile | object | 见说明 | KYC 个人资料，字段名 camelCase |

#### profile（KYC 个人资料）

| 名称 | 必选 | 说明 |
| --- | --- | --- |
| firstName / lastName / birthdate | 建议齐 | 姓名与生日 YYYY-MM-DD |
| countryIso2Code | 有 shareToken 时必填 | 居住国 ISO2，须与地址一致 |
| state / city / address / zipCode | 建议 | 住址 |
| citizenshipIso2Codes | 建议 | 国籍 ISO2 |
| placeOfBirth | 建议 | 出生地 ISO2 |

无 `shareToken` 时，未齐的字段由用户在 `embedUrl` 补填。带 `shareToken` 时须在 initiate 带齐标准个人资料，证件由 Sumsub 复用。仍兼容旧的下划线字段名。

#### 如何获取 shareToken {#share-token}

商户在自己的 Sumsub 后台开通 **Share applicants data**，用 App Token 调用 Sumsub `POST /resources/accessTokens/shareToken`。`forClientId` 由 PayPaz 商务提供，不要填商户自己的 clientId。token 一次性、短 TTL，生成后立刻交给 initiate，不要落库。

> 请求体

```json
{
  "applicantId": "6a41dab9f1fa3809f65ceae6",
  "forClientId": "<PayPaz 提供的 Sumsub clientId>",
  "ttlInSecs": 600
}
```

| 名称 | 类型 | 必选 | 说明 |
| --- | --- | --- | --- |
| applicantId | string | true | 商户 Sumsub 侧该申请人的 ID（Dashboard 或 Applicant API 返回） |
| forClientId | string | true | 接收方 Sumsub clientId，由 PayPaz 商务提供；不要填商户自己的 clientId |
| ttlInSecs | number | 否 | token 有效秒数，建议 600（10 分钟）；过期需重新生成 |

> curl 示例（需按 Sumsub 规则对 timestamp+METHOD+path+body 做 HMAC-SHA256 hex 签名）

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

> 成功响应（取 `token` 作为 initiate 的 `shareToken`）

```json
{
  "token": "_act-sbx-jwt-eyJhbGciOiJub25lIn0...."
}
```

沙盒与生产都使用 `https://api.sumsub.com`，环境由 App Token 前缀区分（沙盒一般为 `sbx:`）。申请人须已在商户 Sumsub 侧审核通过，且 verification level 含证件核验（不只是活体）。

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

此接口不返回订单 `embedUrl`。拿到 `kycStatus=STARTED` 后请再[创建订单](#create-onramp-order)。

---

### 2. POST 创建 On-Ramp 订单 {#create-onramp-order}

按净加密数量建单。法币与支付方式由用户在托管页选择。无论 KYC 是否完成都会落单：未通过时 `status=KYC_REQUIRED`；已通过时会尝试激活报价，成功则多为 `CREATED`。

POST `/t-api/openapi/v1/op/openapi/onramp/orders`

> Body

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
  "chainId": "TRON"
}
```

#### CreateOnRampOrderRequest

| 名称 | 类型 | 必选 | 说明 |
| --- | --- | --- | --- |
| brokerOrderRef | string | true | 商户订单号，同商户唯一 |
| clientSubUserId | string | true | 须与 initiate 一致 |
| netCryptoAmount | string | true | 净加密数量，须大于 0 |
| email | string | true | 终端客户邮箱 |
| cryptoCurrency | string | false | 默认 USDT |
| fiatCurrency | string | false | 可选；用户仍可在托管页改 |
| tokenId | string | false | 缺省 USDT |
| chainId | string | false | 缺省 TRON |
| network | string | false | 缺省同 chainId |
| userIp | string | 建议 | 终端用户 IP |

> 返回（新用户）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "paypazOrderId": "PZO2091798976151027712",
    "brokerOrderRef": "ONRAMP-20260825-0001",
    "status": "KYC_REQUIRED",
    "cryptoCurrency": "USDT",
    "cryptoAmountNet": "1000",
    "embedUrl": "https://pay.example.com/onramp?pageId=....",
    "kycCheckoutUrl": "https://pay.example.com/onramp/kyc?pageId=....",
    "kycStatus": "STARTED",
    "kycMode": "sdk_v2",
    "expireAt": null,
    "createdAt": "1787558714317"
  }
}
```

#### OnRampOrderOpenApiVO

| 名称 | 说明 |
| --- | --- |
| paypazOrderId | PayPaz 业务单号 |
| brokerOrderRef | 商户订单号 |
| status | 订单状态 |
| kycStatus / kycMode | 该用户 KYC 状态与模式 |
| cryptoAmountNet | 净加密数量 |
| fiatAmountCharge | 应付法币；未激活时为 null |
| fiatAmountPaid | 实际支付法币；成交前回写前为空 |
| embedUrl | **请打开此链接**（KYC + 支付） |
| kycCheckoutUrl | 仅 KYC 页，不要用来走支付 |
| pageId / email | 托管页令牌、邮箱 |
| expireAt | 报价过期时间（毫秒）；未激活可能为 null |

- `status=KYC_REQUIRED` 时订单已创建成功。打开 `embedUrl` 即可。
- 若尚未 initiate，托管页仍会引导 KYC；建议商户侧先 initiate。
- KYC 通过后同一页继续支付，无需再创建订单。

---

### 3. GET 查询 On-Ramp 订单详情 {#query-onramp-order-detail}

GET `/t-api/openapi/v1/op/openapi/onramp/orders/info`

| 名称 | 位置 | 必选 | 说明 |
| --- | --- | --- | --- |
| brokerOrderRef | query | true | 商户订单号 |

返回结构同创建订单的 `OnRampOrderOpenApiVO`。当前以轮询本接口或列表接口为准。On-Ramp 入账 Webhook 尚未对商户开放。

---

### 4. POST 查询 On-Ramp 订单列表 {#query-onramp-order-list}

POST `/t-api/openapi/v1/op/openapi/onramp/orders/query`

按当前 API Key 所属商户分页查询，按 `createdAt` 倒序。需要 `onRamp` 权限。

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

| 名称 | 必选 | 说明 |
| --- | --- | --- |
| clientSubUserId | false | 按客户过滤；不传则查商户下全部。传了但不存在该子用户则报错 |
| brokerOrderRef | false | 商户订单号（精确匹配） |
| paypazOrderId | false | PayPaz 订单号（精确匹配） |
| status | false | 订单状态，如 `CREATED` / `COMPLETED` |
| startTime / endTime | 见说明 | 创建时间范围（13 位毫秒）。须同时传或同时不传；都不传则默认 UTC 当天；间隔 ≤30 天 |
| pageNo | false | 从 1 开始，默认 1 |
| pageSize | false | 1–100，默认 20 |

- 只传一端时间：`500105036`；开始大于结束：`500105037`；间隔超过 30 天：`500105038`；时间戳非法：`500105039`。
- 列表项字段与 `OnRampOrderOpenApiVO` 相同，但**不额外查 KYC**，`kycStatus` / `kycMode` 可能为空。

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
        "fiatAmountCharge": "1084.90",
        "fiatAmountPaid": null,
        "network": "TRON",
        "embedUrl": "https://pay.example.com/onramp?pageId=....",
        "createdAt": 1787558714317
      }
    ]
  }
}
```

---

### 订单与 KYC 状态 {#status}

#### 订单 status

| status | 含义 |
| --- | --- |
| KYC_REQUIRED | 未完成 KYC，尚未报价/开地址 |
| ACTIVATING | KYC 已通过，正在开地址 / 取报价 |
| CREATED | 已激活，可进入收银台 |
| AWAITING_PAYMENT | 用户已进入支付 |
| PAYMENT_PROCESSING | 支付已受理 |
| FIAT_RECEIVED | 法币已到账 |
| CRYPTO_TRANSFERRING | 打币中 |
| PENDING_VERIFICATION | 合规补充验证 |
| COMPLETED | 完成 |
| FAILED | 失败 |
| EXPIRED | 过期 |

常见流转：`KYC_REQUIRED` →（KYC 通过）→ `CREATED` → `AWAITING_PAYMENT` → `PAYMENT_PROCESSING` → `CRYPTO_TRANSFERRING` → `COMPLETED`

#### kycStatus

| kycStatus | 含义 |
| --- | --- |
| NOT_STARTED | 尚未 initiate |
| STARTED | 已 initiate |
| PENDING | 审核中 |
| APPROVED | 可通过；建单时会尝试激活订单 |
| REJECTED | 拒绝 |
