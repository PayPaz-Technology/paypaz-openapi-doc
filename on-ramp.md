# On-Ramp 法币入金业务接口

以下业务接口，都需要通过签名认证。公共 Header 与签名算法见 [**认证 API 文档**](README.md)。商户 ID 由 API Key 识别，无需在 Body 中传递。

---

### On-Ramp 对接流程说明

1. KYC 绑定在终端用户上（同一商户下固定 `clientSubUserId`），不绑定某一笔订单。
2. 新用户：先调用接口[**同步 KYC 资料**](#sync-kyc-profile)把资料同步到 PayPaz，再调用接口[**创建 On-Ramp 订单**](#create-onramp-order)。
   - 有 Sumsub `shareToken` 时复用商户已有的 KYC 资料；没有则由用户在托管页填写。
3. 老用户（`kycStatus=APPROVED`）：可跳过同步资料，直接创建订单，通常直接返回 `CREATED`（已报价）。
4. 引导用户打开建单返回的 `embedUrl`（订单托管页）。页内会完成剩余 KYC，通过后在同一页继续选择支付方式并下单。
5. 商户可通过以下方式获取订单结果：
   - 单笔查询：接口[**查询 On-Ramp 订单详情**](#query-onramp-order-detail)。
   - 列表查询：接口[**查询 On-Ramp 订单列表**](#query-onramp-order-list)。

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

**备注：**
- 建单返回同时给出**订单 `status`** 和 **KYC `kycStatus`**，两者含义不同，见[**订单与 KYC 状态**](#status)。
- 同步 KYC 资料可在建单前或建单后调用。建议**先同步再建单**，这样打开托管页时资料已预填。
- `kycCheckoutUrl` 是「只有 KYC、没有订单」的页面，**不要用来测支付**，请始终引导用户打开 `embedUrl`。
- 报价、证件/活体、支付控件均由托管页内部调用，商户无需对接这些接口。
- On-Ramp 入账 Webhook 尚未对商户开放，当前请以轮询查询接口为准。

---

### 1.POST 同步 KYC 资料 {#sync-kyc-profile}

按 `clientSubUserId` 提交并归档终端用户的 KYC 资料，不依赖已有订单。

POST /t-api/openapi/v1/op/openapi/onramp/kyc/initiate

#### 特殊说明

**不必传 `mode`**：请求带了非空 `shareToken` 即走 Sumsub 复用；未带则走托管页由用户填写（证件/活体在页内完成）。

无 `shareToken` 时，未填齐的字段由用户在 `embedUrl` 补填。带 `shareToken` 时须在本接口带齐标准个人资料，证件由 Sumsub 复用。`profile` 字段名为 camelCase，仍兼容旧的下划线字段名。

> Body 请求参数（用户填写，无 shareToken）

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

> Body 请求参数（Sumsub 复用，带 shareToken）

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

#### 请求参数

| 名称   | 位置   | 类型                       | 必选 | 说明   |
| ---- | ---- | ------------------------ | -- | ---- |
| body | body | OnRampKycInitiateRequest | 否  | none |

#### OnRampKycInitiateRequest 属性

| 名称              | 类型     | 必选    | 约束   | 中文名 | 说明                                       |
| --------------- | ------ | ----- | ---- | --- | ---------------------------------------- |
| clientSubUserId | string | true  | none |     | 终端用户唯一标识，同一用户必须固定                        |
| email           | string | true  | none |     | 终端客户邮箱                                   |
| shareToken      | string | false | none |     | 非空则走 Sumsub 复用；为空则用户在托管页填写。一次性，勿长期落库     |
| userIp          | string | false | none |     | 终端用户 IP，建议传                               |
| profile         | object | false | none |     | KYC 个人资料，字段名 camelCase，见下表               |
| mode            | string | false | none |     | 已废弃，由 `shareToken` 是否为空推断，传了也会被忽略         |

#### profile 属性

| 名称                   | 类型     | 必选    | 约束   | 中文名 | 说明                          |
| -------------------- | ------ | ----- | ---- | --- | --------------------------- |
| firstName            | string | false | none |     | 名，建议传                       |
| lastName             | string | false | none |     | 姓，建议传                       |
| birthdate            | string | false | none |     | 出生日期，格式 YYYY-MM-DD，建议传      |
| countryIso2Code      | string | false | none |     | 居住国 ISO2，须与地址一致；有 shareToken 时必填 |
| state                | string | false | none |     | 州/省，建议传                     |
| city                 | string | false | none |     | 城市，建议传                      |
| address              | string | false | none |     | 详细住址，建议传                    |
| zipCode              | string | false | none |     | 邮编，建议传                      |
| citizenshipIso2Codes | string | false | none |     | 国籍 ISO2，建议传                 |
| placeOfBirth         | string | false | none |     | 出生地 ISO2，建议传                |

#### 如何获取 shareToken {#share-token}

商户在自己的 Sumsub 后台开通 **Share applicants data**，用 App Token 调用 Sumsub `POST /resources/accessTokens/shareToken`。`forClientId` 由 PayPaz 商务提供，不要填商户自己的 clientId。token 一次性、短 TTL，生成后立刻交给本接口，不要落库。

沙盒与生产都使用 `https://api.sumsub.com`，环境由 App Token 前缀区分（沙盒一般为 `sbx:`）。申请人须已在商户 Sumsub 侧审核通过，且 verification level 含证件核验（不只是活体）。

> Sumsub 请求体

```json
{
  "applicantId": "6a41dab9f1fa3809f65ceae6",
  "forClientId": "<PayPaz 提供的 Sumsub clientId>",
  "ttlInSecs": 600
}
```

| 名称          | 类型     | 必选    | 约束   | 中文名 | 说明                                        |
| ----------- | ------ | ----- | ---- | --- | ----------------------------------------- |
| applicantId | string | true  | none |     | 商户 Sumsub 侧该申请人的 ID（Dashboard 或 Applicant API 返回） |
| forClientId | string | true  | none |     | 接收方 Sumsub clientId，由 PayPaz 商务提供；不要填商户自己的 clientId |
| ttlInSecs   | number | false | none |     | token 有效秒数，建议 600（10 分钟）；过期需重新生成          |

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

> Sumsub 成功响应（取 `token` 作为本接口的 `shareToken`）

```json
{
  "token": "_act-sbx-jwt-eyJhbGciOiJub25lIn0...."
}
```

> 返回示例

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

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                    | 必选    | 约束   | 中文名 | 说明   |
| ------ | --------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)        | false | none |     | none |
| » msg  | string                | false | none |     | none |
| » data | OnRampOrderOpenApiVO  | false | none |     | none |

本接口不返回订单 `embedUrl`。拿到 `kycStatus=STARTED` 后请再[**创建订单**](#create-onramp-order)。

#### 错误码

- `500105001`：请求缺少必要的认证信息（带 `shareToken` 时 `profile` 缺少居住国）
- `500105007`：子用户不存在
- `500105008`：查询参数不能同时为空（`clientSubUserId` 为空）

---

### 2.POST 创建 On-Ramp 订单 {#create-onramp-order}

按净加密数量创建 On-Ramp 订单，法币与支付方式由用户在托管页选择。

POST /t-api/openapi/v1/op/openapi/onramp/orders

#### 特殊说明

无论 KYC 是否完成都会落单：未通过时 `status=KYC_REQUIRED`；已通过时会尝试激活报价，成功后一般为 `CREATED`。

`brokerOrderRef` 为商户订单号，同一商户下唯一，重复提交返回同一笔订单。

> Body 请求参数

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

#### 请求参数

| 名称   | 位置   | 类型                       | 必选 | 说明   |
| ---- | ---- | ------------------------ | -- | ---- |
| body | body | CreateOnRampOrderRequest | 否  | none |

#### CreateOnRampOrderRequest 属性

| 名称              | 类型     | 必选    | 约束   | 中文名 | 说明                                  |
| --------------- | ------ | ----- | ---- | --- | ----------------------------------- |
| brokerOrderRef  | string | true  | none |     | 商户订单号，同一商户下唯一                       |
| clientSubUserId | string | true  | none |     | 终端用户唯一标识，须与同步 KYC 资料时一致             |
| netCryptoAmount | string | true  | none |     | 净加密数量，须大于等于 0.000001                |
| email           | string | true  | none |     | 终端客户邮箱                              |
| cryptoCurrency  | string | false | none |     | 加密币种，缺省 USDT                        |
| fiatCurrency    | string | false | none |     | 法币币种，可选；用户仍可在托管页修改                  |
| tokenId         | string | false | none |     | PayPaz 入金币种，缺省 USDT                 |
| chainId         | string | false | none |     | PayPaz 入金链，缺省 TRON                  |
| network         | string | false | none |     | Legend 侧网络名，缺省与 `chainId` 一致        |
| userIp          | string | false | none |     | 终端用户 IP，建议传                         |
| returnUrl       | string | false | none |     | 支付结果页「返回商户」的跳转地址，须为 https，最长 2048 字符；支付结果通知不使用此字段 |

> 返回示例

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

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                    | 必选    | 约束   | 中文名 | 说明   |
| ------ | --------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)        | false | none |     | none |
| » msg  | string                | false | none |     | none |
| » data | OnRampOrderOpenApiVO  | false | none |     | none |

#### OnRampOrderOpenApiVO 属性

| 名称               | 类型     | 必选    | 约束   | 中文名 | 说明                              |
| ---------------- | ------ | ----- | ---- | --- | ------------------------------- |
| paypazOrderId    | string | false | none |     | PayPaz 业务单号                     |
| brokerOrderRef   | string | false | none |     | 商户订单号                           |
| clientSubUserId  | string | false | none |     | 终端用户唯一标识                        |
| status           | string | false | none |     | 订单状态，取值见[**订单与 KYC 状态**](#status) |
| cryptoCurrency   | string | false | none |     | 加密币种                            |
| cryptoAmountNet  | string | false | none |     | 净加密数量                           |
| fiatCurrency     | string | false | none |     | 法币币种                            |
| fiatAmountCharge | string | false | none |     | 应付法币金额；订单未激活时为 null             |
| fiatAmountPaid   | string | false | none |     | 实际支付法币金额；成交回写前为空                |
| network          | string | false | none |     | 网络名                             |
| walletAddress    | string | false | none |     | PayPaz 入金地址；订单未激活时为空            |
| embedUrl         | string | false | none |     | **订单托管页地址，请引导用户打开此链接**（KYC + 支付） |
| kycCheckoutUrl   | string | false | none |     | 仅 KYC 页地址，不要用来走支付               |
| pageId           | string | false | none |     | 托管页令牌                           |
| legendUid        | string | false | none |     | 渠道侧用户标识                         |
| email            | string | false | none |     | 终端客户邮箱                          |
| returnUrl        | string | false | none |     | 支付结果页「返回商户」的跳转地址                |
| kycStatus        | string | false | none |     | 该用户 KYC 状态，取值见[**订单与 KYC 状态**](#status) |
| kycMode          | string | false | none |     | KYC 模式，如 `sdk_v2` / `sumsub_share` |
| paymentMethod    | string | false | none |     | 支付方式，用户在托管页选择后回写                |
| txHash           | string | false | none |     | 打币交易哈希                          |
| expireAt         | string | false | none |     | 报价过期时间（毫秒时间戳）；订单未激活时可能为 null    |
| createdAt        | string | false | none |     | 创建时间（毫秒时间戳）                     |
| completedAt      | string | false | none |     | 完成时间（毫秒时间戳）                     |

- `status=KYC_REQUIRED` 时订单已创建成功，打开 `embedUrl` 即可。
- 若尚未同步 KYC 资料，托管页仍会引导用户完成 KYC；建议商户侧先调用同步接口。
- KYC 通过后在同一页继续支付，无需再次创建订单。

#### 错误码

- `500105007`：子用户不存在
- `500105008`：查询参数不能同时为空（`clientSubUserId` 为空）
- `500105026`：充值币种不存在（`tokenId` / `chainId` 组合无效）
- `500105028`：不允许充值（该币种未开启入金）
- `500105041`：On-Ramp 交易对不可用
- `500105042`：On-Ramp 金额低于最低限额
- `500105043`：On-Ramp 链不支持
- `500105044`：On-Ramp 链未启用

---

### 3.GET 查询 On-Ramp 订单详情 {#query-onramp-order-detail}

根据商户订单号查询 On-Ramp 订单详细信息。

GET /t-api/openapi/v1/op/openapi/onramp/orders/info

#### 请求参数

| 名称             | 位置    | 类型     | 必选 | 说明    |
| -------------- | ----- | ------ | -- | ----- |
| brokerOrderRef | query | string | 是  | 商户订单号 |

> 返回示例

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

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                    | 必选    | 约束   | 中文名 | 说明   |
| ------ | --------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)        | false | none |     | none |
| » msg  | string                | false | none |     | none |
| » data | OnRampOrderOpenApiVO  | false | none |     | none |

#### 错误码

- `500105008`：查询参数不能同时为空（`brokerOrderRef` 为空）
- `500105045`：On-Ramp 订单不存在

---

### 4.POST 查询 On-Ramp 订单列表 {#query-onramp-order-list}

按当前 API Key 所属商户分页查询 On-Ramp 订单，按 `createdAt` 倒序。需要 `onRamp` 权限。

POST /t-api/openapi/v1/op/openapi/onramp/orders/query

#### 特殊说明

`startTime` 与 `endTime` 须同时传或同时不传，长度为 13 位毫秒值，支持的最长查询间隔为 30 天。都不传时默认查询 UTC 当天。

列表项字段与 `OnRampOrderOpenApiVO` 相同，但**不额外查询 KYC**，`kycStatus` / `kycMode` 可能为空。

> Body 请求参数

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

#### 请求参数

| 名称   | 位置   | 类型                      | 必选 | 说明   |
| ---- | ---- | ----------------------- | -- | ---- |
| body | body | QueryOnRampOrderRequest | 否  | none |

#### QueryOnRampOrderRequest 属性

| 名称              | 类型             | 必选    | 约束   | 中文名 | 说明                                |
| --------------- | -------------- | ----- | ---- | --- | --------------------------------- |
| clientSubUserId | string         | false | none |     | 终端用户唯一标识，不传则查商户下全部；传了但该子用户不存在会报错  |
| brokerOrderRef  | string         | false | none |     | 商户订单号（精确匹配）                       |
| paypazOrderId   | string         | false | none |     | PayPaz 订单号（精确匹配）                  |
| status          | string         | false | none |     | 订单状态，如 `CREATED` / `COMPLETED`     |
| startTime       | integer(int64) | false | none |     | 开始时间（13 位毫秒时间戳），须与 `endTime` 同时传   |
| endTime         | integer(int64) | false | none |     | 结束时间（13 位毫秒时间戳），须与 `startTime` 同时传 |
| pageNo          | integer(int32) | false | none |     | 页码，从 1 开始，默认 1                    |
| pageSize        | integer(int32) | false | none |     | 每页大小，范围 1-100，默认 20               |

> 返回示例

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

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                          | 必选    | 约束   | 中文名 | 说明   |
| ------ | --------------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)              | false | none |     | none |
| » msg  | string                      | false | none |     | none |
| » data | \[\[OnRampOrderOpenApiVO]]  | false | none |     | 分页结果 |

#### 错误码

- `500105007`：子用户不存在
- `500105036`：开始时间和结束时间不能为空（只传了一端）
- `500105037`：开始时间不能大于结束时间
- `500105038`：开始时间与结束时间间隔不能超过 30 天
- `500105039`：时间格式错误，请传入正确的 13 位毫秒时间戳

---

### 订单与 KYC 状态 {#status}

#### 订单 status

| status              | 说明                |
| ------------------- | ----------------- |
| KYC_REQUIRED        | 未完成 KYC，尚未报价、开地址  |
| ACTIVATING          | KYC 已通过，正在开地址、取报价 |
| CREATED             | 已激活，可进入收银台        |
| AWAITING_PAYMENT    | 用户已进入支付           |
| PAYMENT_PROCESSING  | 支付已受理             |
| FIAT_RECEIVED       | 法币已到账             |
| CRYPTO_TRANSFERRING | 打币中               |
| PENDING_VERIFICATION | 合规补充验证           |
| COMPLETED           | 完成                |
| FAILED              | 失败                |
| EXPIRED             | 过期                |

常见流转：`KYC_REQUIRED` →（KYC 通过）→ `CREATED` → `AWAITING_PAYMENT` → `PAYMENT_PROCESSING` → `CRYPTO_TRANSFERRING` → `COMPLETED`

#### kycStatus

| kycStatus   | 说明                |
| ----------- | ----------------- |
| NOT_STARTED | 尚未同步 KYC 资料       |
| STARTED     | 已同步 KYC 资料        |
| PENDING     | 审核中               |
| APPROVED    | 已通过；建单时会尝试激活订单    |
| REJECTED    | 已拒绝               |
