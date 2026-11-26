# 法币代付业务接口

以下业务接口都需要通过签名认证。公共 Header 与签名算法见 [**认证 API 文档**](README.md)。商户 ID 由 API Key 识别，请求体不要传商户号。

API Key 必须勾选 **Fiat Pay-out**（scope 键 `fiatPayOut`），否则三个接口都返回 `500105010`，不建单、不返回数据。Webhook 按商户回调配置推送，与 API Key 的 scope 无关。

---

### 对接流程说明 {#flow}

1. 商户服务端调用接口[**创建法币代付订单**](#create-fiat-payout-order)。Paypaz 按锁定汇率冻结商户 USDT，返回订单快照，其中包含 `orderNo` 和托管页地址 `hostedUrl`。
2. 商户把 `hostedUrl` 交给这笔订单的收款用户。收款用户在托管页查看进度、确认到账或反馈问题。
3. 收款服务商接单、付款，Paypaz 推送[**Webhook**](#webhook)。商户也可以用[**查询法币代付订单详情**](#query-fiat-payout-order)或[**查询法币代付订单列表**](#query-fiat-payout-order-list)跟进。
4. 收到款项后，商户在商户后台「法币收款/付款 › 法币付款记录」确认到账。提交异议、补充材料也在商户后台操作，这几项暂不提供 OpenAPI。

```
POST createFiatPayOutOrder → 冻结 USDT，返回 orderNo 与 hostedUrl
  ↓
把 hostedUrl 交给收款用户 → 托管页查看进度、确认到账或反馈问题
  ↓
服务商接单 → Webhook accepted
  ↓
服务商付款 → Webhook proof_submitted
  ↓
商户在商户后台确认到账 → Webhook completed
  （超时或有异议时 → Webhook appealed，平台裁定后 → completed / failed）
  ↓
回调丢失时 → GET fiatPayOutOrderInfo 对账
```

**订单生命周期：**
- **待接单**：建单后 60 分钟内没有服务商接单，订单变为 `rejected`（`rejectReason = accept_timeout`），冻结的 USDT 全额解冻。服务商主动拒单时同样变为 `rejected`（`partner_rejected`）。
- **待付款**：服务商接单后 30 分钟内没有提交有效付款，订单转入申诉（`appealSource = timeout`），`paymentTimeout` 变为 `true`。
- **待确认到账**：服务商提交金额与订单一致的付款后进入。30 分钟内商户没有确认，订单转入申诉（`appealSource = confirm_timeout`），`confirmationTimeout` 变为 `true`。
- **成功**：只有两种途径，一是商户在商户后台确认到账，二是平台裁定足额到账。收款用户在托管页确认到账只记录 `customerConfirmedAt` 并推送事件，不会完成订单。
- **失败**：平台裁定全额解冻，`failReason = platform_release`。

**通用约定：**
- 统一响应为 `{ "code": 200, "msg": "success", "data": { } }`，`code = 200` 表示成功。业务拒绝仍是 HTTP 200，用 body 里的 `code` 区分。
- `msg` 随语言变化，`zh-CN` 时成功为「成功」。请只按 `code` 判断结果，文案只用于展示。
- 请求头 `Accept-Language` 的首选语言为 `zh-CN` 时 `msg` 为中文，其余为英文。
- 金额是十进制字符串。时间是带 `+08:00` 的 ISO 8601，精确到秒，例如 `2026-11-13T15:08:00+08:00`。
- 订单快照中还没有发生的字段返回 `null`，不会省略。

---

### 1.POST 创建法币代付订单 {#create-fiat-payout-order}

服务端对服务端调用，创建一笔 CNY 法币代付。

POST /t-api/openapi/v1/op/openapi/createFiatPayOutOrder

#### 特殊说明

- **限频**：创建、查询详情、查询列表各自计算。同一商户每个接口每 60 秒最多 120 次，超出返回 `429100000`，HTTP 状态仍为 200。
- **收款账户**：必须是收款人本人的账户，账户名与 KYC 姓名一致。币种只支持 CNY，金额是区间内的整数字符串。
- **冻结**：下单成功后，商户 USDT 可用余额立即减少 `totalDebitAmount`，减少的部分按 `freezeSplit` 记在对应网络上。
- **托管页**：
  - 把 `hostedUrl` 交给这笔订单的收款用户，链接带签名，可以打开到订单终态之后 30 天。
  - 传了 `returnUrl` 时，托管页显示「返回商户」按钮；没传则不显示。
- **幂等**：同一商户下的 `merchantOrderNo` 是幂等键，见[**幂等**](#idempotency)。

> Body 请求参数

```json
{
  "merchantOrderNo": "B20261113000123",
  "userId": "U10023841",
  "kyc": {
    "name": "张三",
    "idType": "ID_CARD",
    "idNo": "110101199001011234"
  },
  "fiatCurrency": "CNY",
  "fiatAmount": "100000",
  "recipient": {
    "accountName": "张三",
    "accountNo": "6222021234567892223",
    "bankCode": "ICBC"
  },
  "returnUrl": "https://broker.example.com/payout/result"
}
```

#### 请求参数

| 名称   | 位置   | 类型                           | 必选 | 说明   |
| ---- | ---- | ---------------------------- | -- | ---- |
| body | body | CreateFiatPayOutOrderRequest | 是  | none |

#### CreateFiatPayOutOrderRequest 属性

| 名称              | 类型     | 必选    | 约束                   | 中文名    | 说明                                                                                          |
| --------------- | ------ | ----- | -------------------- | ------ | ------------------------------------------------------------------------------------------- |
| merchantOrderNo | string | true  | 1–64 位               | 商户订单号  | 幂等键，商户内唯一，不能有首尾空白                                                                         |
| userId          | string | true  | 1–64 位               | 客户 ID  | 商户侧客户 ID，写入订单并在响应、Webhook 里回传                                                              |
| kyc             | object | true  | none                 | 收款人 KYC | 见[**kyc 属性**](#kyc)                                                                        |
| fiatCurrency    | string | true  | 仅 `CNY`              | 法币币种   | 区分大小写                                                                                       |
| fiatAmount      | string | true  | 正整数字符串               | 代付金额   | 不要小数点、千分位或前导零。闭区间见[**金额与校验**](#rules)，默认 `1000`–`100000`                                  |
| recipient       | object | true  | none                 | 收款账户   | 见[**recipient 属性**](#recipient)                                                            |
| returnUrl       | string | false | `https://`，最长 512    | 返回地址   | 托管页「返回商户」地址。不传表示不显示该按钮。传入时必须以小写 `https://` 开头且不能含空白；传空字符串会被拒绝（`500105056`）                   |

#### kyc 属性 {#kyc}

| 名称     | 类型     | 必选   | 约束                     | 中文名   | 说明                                                                                        |
| ------ | ------ | ---- | ---------------------- | ----- | ----------------------------------------------------------------------------------------- |
| name   | string | true | 1–100 字                | 收款人姓名 | 须与 `recipient.accountName` 为同一人                                                            |
| idType | string | true | `ID_CARD` / `PASSPORT` | 证件类型  | `ID_CARD` 居民身份证 / `PASSPORT` 护照，区分大小写                                                    |
| idNo   | string | true | 1–64 位                 | 证件号   | 入库前全角转半角、去首尾空白并转大写，中间不能有空白。身份证为 17 位数字加最后一位数字或 `X`，只校验格式；护照为 5–20 位 `A–Z` 与数字 |

#### recipient 属性 {#recipient}

| 名称          | 类型     | 必选   | 约束        | 中文名   | 说明                                                                    |
| ----------- | ------ | ---- | --------- | ----- | --------------------------------------------------------------------- |
| accountName | string | true | 1–100 字   | 收款账户名 | 与 `kyc.name` 比较前先做 Unicode NFKC 规范化，并忽略大小写和首尾空白，连续空白视为一个空格             |
| accountNo   | string | true | 12–32 位数字 | 收款卡号  | 收款人本人的银行卡号，不要空格                                                       |
| bankCode    | string | true | 1–16 位    | 收款银行  | 见[**银行代码**](#banks)，区分大小写                                            |

- 缺字段、长度、枚举、卡号、URL 格式不对，或金额不是正整数字符串，由 OpenAPI 直接拒绝，返回 `500100000`，`msg` 是英文字段说明，例如 `fiatAmount must be a positive integer string`、`recipient.accountNo must be 12-32 digits without spaces`。
- 证件号格式、空的 `returnUrl`、姓名不一致、金额区间和通道校验走 `500105xxx`，见[**错误码**](#error-codes)。

> 返回示例

> 200 Response

下面示例里的汇率和金额只说明格式，以实际响应为准。

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

#### 返回数据结构

状态码 **200**

| 名称     | 类型                 | 必选    | 约束   | 中文名 | 说明                                         |
| ------ | ------------------ | ----- | ---- | --- | ------------------------------------------ |
| » code | integer(int32)     | false | none |     | none                                       |
| » msg  | string             | false | none |     | none                                       |
| » data | FiatPayOutOrderVO  | false | none |     | 订单快照，见[**FiatPayOutOrderVO 属性**](#fiat-payout-order-vo) |

#### FiatPayOutOrderVO 属性 {#fiat-payout-order-vo}

创建订单的成功响应，以及查询详情、查询列表返回的每一笔订单，都是这份快照。建单成功时状态为 `pending_accept`，并已冻结 USDT。

| 名称                     | 类型                       | 说明                                                                               |
| ---------------------- | ------------------------ | -------------------------------------------------------------------------------- |
| orderId                | string                   | 平台内部 ID，十进制字符串。对接请用 `orderNo` 标识订单                                               |
| orderNo                | string                   | Paypaz 订单号，`PZPO` + 上海日期 + `-` + 至少 5 位当日序号，例如 `PZPO20261113-00001`              |
| merchantOrderNo        | string                   | 商户订单号                                                                            |
| userId                 | string                   | 商户侧客户 ID                                                                         |
| status                 | string                   | 订单状态，见[**订单状态**](#status)。重放或查询时返回订单当前状态                                       |
| statusVersion          | integer                  | 状态版本，建单为 `1`。订单状态或申诉内容变化时递增，可用于丢弃过期的通知                                         |
| executionStatus        | string                   | 执行进度：`not_started` 未开始 / `accepted` 已接单 / `paid` 已付款                               |
| fiatCurrency           | string                   | `CNY`                                                                            |
| fiatAmount             | string                   | CNY 整数字符串，与请求金额相同                                                                |
| settlementCurrency     | string                   | 固定 `USDT`                                                                        |
| merchantRate           | string                   | 商户结算汇率 Rm，4 位小数，含义为 1 USDT = Rm CNY                                              |
| merchantAmount         | string                   | 付款金额 X，USDT，2 位小数                                                                |
| feeAmount              | string                   | 手续费 F，USDT，2 位小数。为 0 时返回 `"0.00"`                                               |
| totalDebitAmount       | string                   | 本次冻结的 USDT，等于 `merchantAmount + feeAmount`，2 位小数                                  |
| freezeSplit            | array[FreezeSplitItem]   | 冻结拆分，各项 `amount` 之和等于 `totalDebitAmount`                                          |
| bankCode               | string                   | 收款银行代码                                                                           |
| accountNo              | string                   | 收款卡号脱敏：前 4 位 + `****` + 后 4 位                                                    |
| createdAt              | string                   | 建单时间                                                                             |
| acceptDeadlineAt       | string                   | 接单截止时间，建单后 60 分钟。此时间前未接单，订单被拒绝并解冻                                              |
| paymentDeadlineAt      | string \| null           | 付款截止时间，服务商接单后才有，接单后 30 分钟                                                      |
| confirmationDeadlineAt | string \| null           | 确认截止时间，服务商提交有效付款后才有，提交后 30 分钟                                                   |
| serverTime             | string                   | 生成本快照时的服务器时间，可用于计算倒计时                                                          |
| paymentTimeout         | boolean                  | 付款超时转入申诉后为 `true`                                                               |
| confirmationTimeout    | boolean                  | 确认超时转入申诉后为 `true`                                                               |
| customerConfirmedAt    | string \| null           | 收款用户在托管页确认到账的时间                                                                 |
| confirmationSource     | string \| null           | 完成方式：`merchant_portal` 商户后台确认 / `platform_ruling` 平台裁定。`merchant_api` 为预留值，目前不会出现 |
| completedAt            | string \| null           | 成功时间                                                                             |
| failedAt               | string \| null           | 失败时间                                                                             |
| rejectedAt             | string \| null           | 拒绝时间                                                                             |
| rejectReason           | string \| null           | 拒绝原因：`partner_rejected` 服务商拒单 / `accept_timeout` 接单超时                             |
| failReason             | string \| null           | 失败原因：`platform_release` 平台裁定解冻                                                   |
| hostedUrl              | string                   | 托管页地址，路径为 `/fiat-pay-out?token=`。token 带签名，不能修改                                    |
| appealId               | string \| null           | 申诉 ID                                                                            |
| appealSource           | string \| null           | 申诉来源，见[**订单状态与枚举**](#status)                                                     |
| appealReason           | string \| null           | 发起方填写的申诉说明原文；系统转入的申诉为空                                                          |
| appealReasonCode       | string \| null           | 申诉原因代码，见[**订单状态与枚举**](#status)                                                   |
| appealRecords          | array[AppealRecordItem]  | 申诉记录，建单时为空数组                                                                    |
| payments               | array[PaymentItem]       | 付款记录，建单时为空数组                                                                    |

响应不含收款服务商汇率、加点、价差和收款服务商信息。

#### FreezeSplitItem 属性

| 名称      | 类型     | 说明                                     |
| ------- | ------ | -------------------------------------- |
| network | string | 网络，取值为 `TRON`、`ETH`、`BSC_BNB` 等链标识      |
| amount  | string | 该网络冻结的 USDT，2 位小数                      |

#### PaymentItem 属性

| 名称               | 类型            | 说明                                                                 |
| ---------------- | ------------- | ------------------------------------------------------------------ |
| paymentId        | string        | 付款记录 ID                                                            |
| kind             | string        | `normal` 付款 / `late` 补报付款                                          |
| bankTxnNo        | string        | 银行流水号                                                              |
| bankStatus       | string        | `processing` 处理中 / `succeeded` 成功 / `returned` 退回 / `failed` 失败    |
| paidAmount       | string        | 实付金额，CNY，2 位小数                                                     |
| paidAt           | string        | 付款时间                                                               |
| payerBankCode    | string        | 付款银行代码                                                             |
| payerAccountTail | string        | 付款账户后 4 位                                                          |
| proofFileIds     | array[string] | 付款凭证文件 ID。目前不提供下载接口                                                |
| submittedAt      | string        | 提交时间                                                               |

#### AppealRecordItem 属性

| 名称         | 类型            | 说明                                                                                      |
| ---------- | ------------- | --------------------------------------------------------------------------------------- |
| recordId   | string        | 记录 ID                                                                                   |
| recordType | integer       | `1` 发起 / `2` 补充材料 / `3` 补报付款 / `4` 异议追加 / `5` 客户反馈追加 / `6` 裁定                           |
| party      | string        | 提交方：`platform` 平台 / `merchant` 商户 / `partner` 收款服务商 / `customer` 收款用户                   |
| reasonCode | string        | 原因代码：发起记录同 `appealReasonCode`，异议追加为 `not_received` / `amount_mismatch`，其余为空              |
| content    | string        | 说明内容。裁定记录的内容为空                                                                         |
| fileIds    | array[string] | 附件文件 ID。目前不提供下载接口                                                                     |
| createdAt  | string        | 提交时间                                                                                  |

#### 错误码

- `500105010`：API Key 未勾选 Fiat Pay-out
- `500100000`：参数格式不合法
- `429100000`：请求频率超限
- `500105055`–`500105070`：业务拒绝，见[**错误码**](#error-codes)

---

### 2.GET 查询法币代付订单详情 {#query-fiat-payout-order}

按订单号或商户订单号查询单笔订单，只返回当前 API Key 所属商户的订单。

GET /t-api/openapi/v1/op/openapi/fiatPayOutOrderInfo

#### 请求参数

| 名称              | 位置    | 类型     | 必选  | 说明                                  |
| --------------- | ----- | ------ | --- | ----------------------------------- |
| orderNo         | query | string | 二选一 | Paypaz 订单号，与 `merchantOrderNo` 只能传一个 |
| merchantOrderNo | query | string | 二选一 | 创建订单时的商户订单号                         |

```
GET /t-api/openapi/v1/op/openapi/fiatPayOutOrderInfo?orderNo=PZPO20261113-00001
GET /t-api/openapi/v1/op/openapi/fiatPayOutOrderInfo?merchantOrderNo=B20261113000123
```

- 签名时 `requestPath` 要包含查询串，并且与实际发送的 URL 完全一致；GET 没有 body，签名里的 body 为空字符串。
- 两个都传、两个都不传，或只传空白，返回 `500105056`，字段名为 `orderNo`。
- 订单不存在或属于其他商户，都返回 `500105069`，不区分这两种情况。建单被拒的请求不会生成订单，用它的 `merchantOrderNo` 查询同样返回 `500105069`。

成功时 `data` 是[**订单快照**](#fiat-payout-order-vo)，包含当前 `hostedUrl`、付款记录和申诉记录。

#### 错误码

- `500105056`：单号不是二选一
- `500105069`：订单不存在或无权访问
- `500105010`：API Key 未勾选 Fiat Pay-out

---

### 3.POST 查询法币代付订单列表 {#query-fiat-payout-order-list}

分页查询当前商户的代付订单。请求体可以省略，或传 `{}`。关闭代付配置不影响历史订单查询。结果按 `createdAt` 倒序，同一时间再按 `orderNo` 倒序。

POST /t-api/openapi/v1/op/openapi/fiatPayOutOrders

> Body 请求参数

```json
{
  "page": 1,
  "pageSize": 20,
  "createdFrom": "2026-11-01T00:00:00+08:00",
  "createdTo": "2026-11-18T00:00:00+08:00",
  "status": ["pending_accept", "pending_payment"]
}
```

#### QueryFiatPayOutOrdersRequest 属性

| 名称          | 类型             | 必选    | 约束             | 中文名    | 说明                                                       |
| ----------- | -------------- | ----- | -------------- | ------ | -------------------------------------------------------- |
| page        | integer(int32) | false | ≥ 1            | 页码     | 从 1 开始，不传为 `1`                                           |
| pageSize    | integer(int32) | false | 1–100          | 每页条数   | 不传为 `20`                                                 |
| createdFrom | string         | false | 带时区的 ISO 8601  | 创建时间下界 | 含该时刻，例如 `2026-11-01T00:00:00+08:00`，也接受以 `Z` 结尾的 UTC 时间 |
| createdTo   | string         | false | 带时区的 ISO 8601  | 创建时间上界 | 不含该时刻。与 `createdFrom` 同时传入时必须晚于 `createdFrom`             |
| status      | array[string]  | false | 见订单状态          | 状态     | 可多选，区分大小写。不传表示不限状态                                       |

- 时间筛选按毫秒比较，而快照中的时间只到秒，按秒边界拼接查询区间时请注意。
- 页码超出最后一页时，`items` 为空数组，`total` 不变。
- 页码、每页条数、时间或状态不合法时返回 `500105056`，原因见[**500105056 的原因**](#invalid-reasons)。

> 返回示例

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

#### 返回数据结构

| 名称           | 类型                       | 说明                                                  |
| ------------ | ------------------------ | --------------------------------------------------- |
| » total      | string                   | 符合条件的订单总数，十进制字符串                                    |
| » page       | integer(int32)           | 实际使用的页码                                             |
| » pageSize   | integer(int32)           | 实际使用的每页条数                                           |
| » items      | array[FiatPayOutOrderVO] | 当前页订单，每一项都是[**订单快照**](#fiat-payout-order-vo)。没有结果时为 `[]` |

---

### 4.幂等 {#idempotency}

幂等键是「本商户 + `merchantOrderNo`」。参与比对的内容是 `userId`、规范化后的姓名和证件号、`idType`、`fiatCurrency`、`fiatAmount`、规范化后的账户名、卡号、`bankCode`、`returnUrl`。

| 再次提交                                    | 结果                                                                                         |
| --------------------------------------- | ------------------------------------------------------------------------------------------ |
| 参数与首次一致，且首次已建单                          | `code = 200`，返回该订单的当前快照。状态可能已经离开 `pending_accept`，不会再建一笔，也不会再次冻结                            |
| 参数与首次一致，且首次被业务拒绝（`500105055`–`500105063`） | 返回与首次相同的错误码。`msg` 按本次请求的语言重新生成；`500105058` 的 `data` 取当前配置的区间。要重新下单，换一个新的 `merchantOrderNo` |
| 同一个 `merchantOrderNo`，参数与首次不一致          | `500105064`                                                                                |
| 另一个使用同一 `merchantOrderNo` 的请求正在处理       | 等待 3 秒仍未处理完时返回 `500105065`，与参数是否相同无关。稍后用原请求再提交一次                                           |
| 结果未能确认                                  | `500105070`。用原请求原样重试；如果订单其实已经建成，重试会拿到该订单快照                                                |

- 重放检查在业务校验之前。不传 `returnUrl` 与传空字符串的比对结果相同：首次没传 `returnUrl` 且建单成功后，再用 `""` 重放会返回 `200`，而不是 `500105056`。
- 建单被拒不会生成订单，查询接口查不到被拒的请求。

---

### 5.Webhook {#webhook}

在商户后台「开发者 › Webhooks」配置回调地址并订阅事件，与其他业务共用同一套签名和重试规则，见 [**webhook说明**](WEBHOOK_GUIDE.md)。每个启用的回调地址各推一次；回调地址设置了事件订阅时，只推送订阅的事件。

包体为 `{ "eventType": "...", "data": { } }`。`data` 中没有值的字段不返回。金额、汇率、时间格式与订单快照相同。

**公共字段：**

| 名称              | 类型      | 说明                                          |
| --------------- | ------- | ------------------------------------------- |
| eventId         | string  | 事件 ID（UUID），每个事件唯一，重试时不变，可用于去重                  |
| occurredAt      | string  | 事件发生时间，ISO 8601（`+08:00`）                    |
| orderNo         | string  | Paypaz 订单号                                  |
| merchantOrderNo | string  | 商户订单号                                       |
| userId          | string  | 商户侧客户 ID                                    |
| status          | string  | 事件发生后的订单状态                                  |
| statusVersion   | integer | 事件发生后的状态版本，与订单快照的 `statusVersion` 一致          |
| fiatCurrency    | string  | `CNY`                                       |
| fiatAmount      | string  | CNY 整数字符串                                   |

**事件：**

| eventType                                    | 触发                                        | 事件后状态                  | 额外字段                                                                                                      |
| -------------------------------------------- | ----------------------------------------- | ---------------------- | --------------------------------------------------------------------------------------------------------- |
| `transaction.fiat.payout.accepted`           | 服务商接单                                     | `pending_payment`      | `acceptedAt`、`paymentDeadlineAt`（接单后 30 分钟）                                                               |
| `transaction.fiat.payout.proof_submitted`    | 服务商提交的付款与订单金额一致                           | `pending_confirmation` | `proofSubmittedAt`、`confirmationDeadlineAt`（提交后 30 分钟）、`proofFileIds`                                     |
| `transaction.fiat.payout.customer_confirmed` | 收款用户在托管页确认到账                              | 不变                     | `customerConfirmedAt`                                                                                     |
| `transaction.fiat.payout.appealed`           | 订单首次进入申诉                                  | `appealed`             | `appealId`、`appealSource`、`appealReason`、`appealReasonCode`、`appealedAt`                                  |
| `transaction.fiat.payout.completed`          | 商户在商户后台确认到账，或平台裁定足额到账                     | `completed`            | `settlementCurrency`、`merchantRate`、`merchantAmount`、`feeAmount`、`totalDebitAmount`、`confirmationSource`、`completedAt` |
| `transaction.fiat.payout.failed`             | 平台裁定解冻                                    | `failed`               | `failReason`、`failedAt`、`settlementCurrency`、`releasedAmount`、`freezeSplit`                               |
| `transaction.fiat.payout.rejected`           | 服务商拒单，或接单超时                               | `rejected`             | `rejectReason`、`rejectedAt`、`settlementCurrency`、`releasedAmount`、`freezeSplit`                           |

- **建单**：成功时不推送事件，以创建接口的响应为准。
- **付款金额不一致**：服务商提交的付款与订单金额不一致时，订单直接转入申诉（`appealSource = partner`），推送 `appealed`，不推送 `proof_submitted`。
- **申诉中**：订单已在申诉中时，再追加异议、材料或付款，都不会再推一次 `appealed`。
- **解冻金额**：`releasedAmount` 是解冻的 USDT，等于建单时冻结的 `totalDebitAmount`。`freezeSplit` 是各网络的解冻明细。
- **乱序**：回调可能乱序到达，请用 `statusVersion` 丢弃比本地更旧的通知，必要时调用查询接口确认。

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

### 订单状态与枚举 {#status}

#### 订单状态 status

| status                 | 中文    | 终态 | 含义                                     |
| ---------------------- | ----- | -- | -------------------------------------- |
| `pending_accept`       | 待接单   |    | 建单成功即是这个状态                             |
| `pending_payment`      | 待付款   |    | 服务商已接单，等待付款                            |
| `pending_confirmation` | 待确认到账 |    | 服务商已提交有效付款，等待商户确认                      |
| `appealed`             | 申诉中   |    | 超时、金额不一致或有异议，等待平台裁定                     |
| `completed`            | 成功    | 是  | 商户确认到账或平台裁定足额到账，冻结的 USDT 已扣除            |
| `failed`               | 失败    | 是  | 平台裁定解冻                                 |
| `rejected`             | 已拒绝   | 是  | 服务商拒单或接单超时，冻结的 USDT 已解冻                 |

#### 申诉来源 appealSource

| 值                 | 含义             |
| ----------------- | -------------- |
| `timeout`         | 付款超时，系统转入      |
| `confirm_timeout` | 确认到账超时，系统转入    |
| `merchant`        | 商户提交异议         |
| `partner`         | 服务商付款异常        |
| `customer`        | 收款用户反馈         |

#### 申诉原因代码 appealReasonCode

| 来源                 | 值                                                                                                    |
| ------------------ | ---------------------------------------------------------------------------------------------------- |
| `merchant` 商户异议    | `not_received` 未收到款项 / `amount_mismatch` 金额不符                                                        |
| `partner` 服务商付款异常 | `underpaid` 少付 / `overpaid` 多付 / `split_payment` 拆分付款 / `bank_processing` 银行处理中 / `returned` 退回 / `wrong_account` 付错账户 / `other` 其他 |
| 其余来源               | 空                                                                                                    |

---

### 银行代码 {#banks}

`recipient.bankCode` 取下表代码。不在表内返回 `500105060`；在表内但当前没有支持该银行的代付通道，返回 `500105061`。

| 代码      | 银行       | 代码     | 银行       |
| ------- | -------- | ------ | -------- |
| `ICBC`  | 中国工商银行   | `CGB`  | 广发银行     |
| `CCB`   | 中国建设银行   | `PAB`  | 平安银行     |
| `ABC`   | 中国农业银行   | `CIB`  | 兴业银行     |
| `BOC`   | 中国银行     | `SPDB` | 上海浦东发展银行 |
| `BOCOM` | 交通银行     | `CZB`  | 浙商银行     |
| `PSBC`  | 中国邮政储蓄银行 | `CBHB` | 渤海银行     |
| `CMB`   | 招商银行     | `HFB`  | 恒丰银行     |
| `CITIC` | 中信银行     | `BOB`  | 北京银行     |
| `CEB`   | 中国光大银行   | `BOS`  | 上海银行     |
| `HXB`   | 华夏银行     | `JSB`  | 江苏银行     |
| `CMBC`  | 中国民生银行   | `NJCB` | 南京银行     |
|         |          | `NBCB` | 宁波银行     |
|         |          | `HZB`  | 杭州银行     |

---

### 错误码 {#error-codes}

业务拒绝仍是 HTTP 200，用 body 的 `code` 区分。`Accept-Language` 的首选语言为 `zh-CN` 时 `msg` 为中文，其余为英文。除下面注明的情况外，`data` 为 `null`。请按错误码处理，文案只用于展示。

#### 鉴权与通用

三个接口在进入业务前都会校验，先后顺序为：缺签名头 → 时间戳 → API Key → 签名 → IP 白名单 → scope。

| code        | 含义                                    | 中文 msg       | English msg                                                                    |
| ----------- | ------------------------------------- | ------------ | ------------------------------------------------------------------------------ |
| `500105001` | 缺少签名头                                 | 请求缺少必要的认证信息  | Request missing required authentication information                            |
| `500105002` | API Key 无效（含已停用的 Key）                 | 无效的API Key   | Invalid API Key                                                                |
| `500105003` | 签名校验失败                                | 签名验证失败       | Signature verification failed                                                  |
| `500105004` | 时间戳超出接收窗口                             | 请求时间戳已过期     | Request timestamp has expired                                                  |
| `500105005` | 时间戳或接收窗口格式不对                          | 无效的时间戳格式     | Invalid timestamp format                                                       |
| `500105011` | 来源 IP 不在 API Key 的白名单中（白名单为空时不限制）      | IP不在白名单中     | IP address is not in the whitelist                                             |
| `500105010` | API Key 未勾选 Fiat Pay-out              | 没有权限访问该API   | No permission to access this API                                               |
| `429100000` | 超出限频（每商户每接口每 60 秒 120 次），HTTP 状态仍为 200 | 访问过于频繁，请稍候再试 | You have exceeded the request limit. Please wait a moment before trying again. |
| `405100000` | 请求方法不对，例如用 GET 调用 POST 接口。`msg` 固定为英文，不随语言切换 | `Request method 'GET' is not supported` | `Request method 'GET' is not supported`                                        |
| `500100000` | 缺字段、长度、枚举、卡号、URL 格式不对，或金额不是正整数字符串。`msg` 是英文字段说明，多个字段用英文逗号加空格拼接 | 见说明          | 见说明                                                                            |

`500100000` 还有两种随语言切换的文案：请求体不是合法 JSON 或字段类型不对时为「参数类型错误」/ `parameter type error`；未知运行时异常时为「系统异常」/ `System error`。创建接口遇到「系统异常」时结果未知，请按 `500105070` 处理。

#### 业务拒绝

| code        | 何时返回                                                                                   | 中文 msg                              | English msg                                                                                        |
| ----------- | -------------------------------------------------------------------------------------- | ----------------------------------- | -------------------------------------------------------------------------------------------------- |
| `500105055` | 该商户未开通法币代付                                                                             | 法币代付服务未开通，请联系客服。                    | Fiat pay-out is not enabled. Please contact customer support.                                      |
| `500105056` | 证件号格式不符，或传入了空的 `returnUrl`。查询时单号不是二选一、页码或时间非法、状态无法识别，也返回这个码。`{0}`、`{1}` 替换为字段名和原因 | 参数 {0} 不合法：{1}                      | Invalid parameter {0}: {1}                                                                         |
| `500105057` | 收款账户名与 KYC 姓名不一致，或姓名规范化后为空                                                            | 收款账户名须与收款人 KYC 姓名一致                 | The recipient account name must match the KYC name of the recipient.                               |
| `500105058` | 金额是整数，但不在平台区间内                                                                         | 代付金额需为 {0} - {1} CNY 之间的整数           | The pay-out amount must be a whole number between {0} and {1} CNY.                                 |
| `500105059` | 同一证件在任意商户下已有进行中的代付                                                                     | 该客户有一笔代付订单正在处理中，请完成后再发起。            | This customer already has a pay-out order in progress. Please try again after it is completed.     |
| `500105060` | `bankCode` 不在[**银行代码**](#banks)表内                                                     | 不支持的收款银行                            | The recipient bank is not supported.                                                               |
| `500105061` | 当前没有可接这笔金额和银行的代付通道，包括通道不在服务时间、或距服务结束不足 60 分钟                                         | 当前没有可用的代付通道，请稍后再试。                  | No pay-out channel is available right now. Please try again later.                                 |
| `500105062` | 通道本时段额度不足                                                                              | 代付通道额度不足，请稍后再试。                     | The pay-out channel limit has been reached. Please try again later.                                |
| `500105063` | 商户 USDT 可用余额不够冻结 `X + F`，或商户还没有 USDT 账户或余额记录                                           | 账户可用余额不足                            | Insufficient available balance.                                                                    |
| `500105064` | 同一 `merchantOrderNo` 的参数与首次不一致                                                          | 同一请求标识的参数与首次请求不一致                   | The request parameters do not match the first request with the same identifier.                    |
| `500105065` | 另一个使用同一 `merchantOrderNo` 的请求正在处理                                                      | 请求处理中，请稍后查询                         | The request is being processed. Please query the result later.                                     |
| `500105069` | 查询的订单不存在，或不属于当前商户                                                                      | 无权访问该资源                             | You do not have access to this resource.                                                           |
| `500105070` | 建单结果未能确认：内部调用超时或失败、系统繁忙（通道锁 3 秒内未拿到）、当日订单号用尽或其他未知异常。订单可能已经建成                       | 处理结果未知，请使用原请求标识重试或查询                | The result is unknown. Please retry with the original request identifier or query the order.       |

`500105070` 的英文文案有两种写法，另一种是 `The result is unknown. Retry with the same request id, or query the order.`，请按错误码处理。

`500105058` 的 `data` 带当前平台区间，金额为整数字符串：

```json
{
  "code": 500105058,
  "msg": "代付金额需为 1000 - 100000 CNY 之间的整数",
  "data": { "minAmount": "1000", "maxAmount": "100000" }
}
```

`500105055` 至 `500105063` 会按首次结果固定下来。同一 `merchantOrderNo` 再次提交，即使余额或通道后来恢复，仍然返回第一次的错误码。换新的商户订单号才会重新校验。

#### 500105056 的原因 {#invalid-reasons}

| 接口 | 字段            | 原因 `{1}`                                         |
| -- | ------------- | ------------------------------------------------ |
| 创建 | `kyc.idNo`    | `must not be blank or contain spaces`（规范化后为空或中间有空白） |
| 创建 | `kyc.idNo`    | `format does not match ID_CARD` / `format does not match PASSPORT` |
| 创建 | `returnUrl`   | `must be an https URL`（传了空字符串）                    |
| 详情 | `orderNo`     | `orderNo 与 merchantOrderNo 二选一`                   |
| 列表 | `page`        | `须大于等于 1`                                         |
| 列表 | `pageSize`    | `范围为 1–100`                                       |
| 列表 | `createdFrom` / `createdTo` | `须为带时区的 ISO 8601 时间`                     |
| 列表 | `createdTo`   | `须晚于 createdFrom`                                 |
| 列表 | `status`      | `存在无法识别的订单状态`                                     |

创建接口的原因是英文，查询接口的原因是中文，都不随 `Accept-Language` 切换，只有外层模板「参数 … 不合法」/ `Invalid parameter …` 随语言变化。

---

### 金额与校验 {#rules}

- **金额区间**：平台配置的闭区间，默认 1000–100000 CNY，以 `500105058` 响应里的 `minAmount`、`maxAmount` 为准。
- **结算口径**：汇率和手续费在下单时锁定，写在成功响应里。商户展示给用户的页面不要另算一版汇率。
  - 商户结算汇率 Rm = 收款服务商汇率 Rc × (1 − 价差)，保留 4 位小数，向下截断。
  - 付款金额 X = 代付金额 ÷ Rm，向上取整到分。
  - 手续费 F：固定金额时为配置的 USDT 值；按比例时为 X × 费率，向上取整到分。
  - 冻结金额 = X + F。
- **示例**：100,000 CNY，Rc 7.2000，价差 1%，手续费 0.1% → Rm `7.1280`，X `14029.19`，F `14.03`，冻结 `14043.22`。
- **冻结拆分**：
  - 先从 TRON 网络的可用余额中划，不够时按其余网络可用余额从大到小依次划，余额相同按网络标识排序。
  - 参与计算的余额先截断到 2 位小数。
  - 合计不足时返回 `500105063`，不建单、不部分冻结。
- **跨商户判重**：同一证件类型 + 规范化证件号，在任意商户下只要有一笔未终态的代付（待接单、待付款、待确认到账、申诉中），就不能再下一笔，返回 `500105059`。
- **区分大小写**：`bankCode`、`fiatCurrency`、`idType` 和列表的 `status` 都区分大小写，`returnUrl` 须以小写 `https://` 开头。
- **校验顺序**：
  - 签名 → IP 白名单 → scope → 字段格式（`500100000`）→ 限频（`429100000`）→ 幂等重放。
  - 业务校验依次为：`500105055` → 空的 `returnUrl`（`500105056`）→ 证件号（`500105056`）→ `500105057` → `500105060` → `500105058` → `500105059` → `500105061` / `500105062` → `500105063`。
- **托管页**：
  - 地址加 `lang=en` 显示英文，`lang=zh` 显示中文；不带时沿用收款用户上次在页面上的选择，默认中文。
  - 页面每 5 秒刷新一次。
  - 待接单与待付款都显示为「待付款」；已拒绝显示为失败（未接单）；收款用户确认到账后显示「待商户确认」。
  - 倒计时跟随当前阶段的截止时间。
  - 收款用户只能反馈一次问题。
  - 传了 `returnUrl` 时，所有状态下都显示「返回商户」按钮。
