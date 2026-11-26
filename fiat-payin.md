# 法币收款（代收）业务接口

以下业务接口都需要通过签名认证。公共 Header 与签名算法见 [**认证 API 文档**](README.md)。商户 ID 由 API Key 识别，无需在 Body 中传递。

API Key 必须勾选 **Fiat Pay-in**（scope 键 `fiatPayIn`），否则三个接口都返回 `500105010`，不建单、不返回数据。Webhook 按商户回调配置推送，与 API Key 的 scope 无关。

---

### 对接流程说明 {#flow}

1. 用户在商户侧选择法币入金，金额为 200,000–500,000 的整数 CNY。商户页面不要展示汇率或预估 USDT。
2. 商户服务端调用接口[**创建法币收款订单**](#create-fiat-payin-order)，成功后把返回的 `checkoutUrl` 交给该用户，整页跳转或 iframe 嵌入均可。
3. 用户在 Paypaz 托管收银台完成转账并上传凭证。收款服务商核验到账后，Paypaz 按建单时锁定的汇率把 USDT 记入商户余额，并推送 Webhook。
4. 商户订阅[**Webhook**](#webhook) 的 7 个事件。回调未到时，用接口[**查询法币收款订单详情**](#query-fiat-payin-order)或[**分页查询法币收款订单**](#query-fiat-payin-order-list)兜底。
5. 订单成功即已入账，商户无需另行发起结算。把结果告知用户的方式由商户决定。

```
POST createFiatPayInOrder → 返回 orderNo 与 checkoutUrl
  ↓
用户打开 checkoutUrl，转账并上传凭证 → Webhook proof_submitted
  ↓
收款服务商核验；申诉、退款时 → Webhook appealed / refunding
  ↓
订单终态 → Webhook completed / refunded / failed / cancelled
  ↓
回调丢失时 → GET fiatPayInOrderInfo 对账
```

**通用约定：**
- 统一响应为 `{ "code": 200, "msg": "success", "data": { } }`，`code = 200` 表示成功。业务拒绝仍是 HTTP 200，用 body 里的 `code` 区分。
- `msg` 随语言变化，`zh-CN` 时成功为「成功」。请只按 `code` 判断结果，不要依赖 `msg` 文案。
- 请求头 `Accept-Language` 的首选语言为 `zh-CN` 时 `msg` 为中文，其余（包括 `zh`、`zh-TW`）一律为英文。
- 金额全部是十进制字符串。时间全部是 13 位毫秒时间戳，**以字符串返回**，例如 `"1791622800000"`。
- 卡号、开户名、开户行只出现在收银台，查询接口不返回。

---

### 1.POST 创建法币收款订单 {#create-fiat-payin-order}

服务端对服务端调用，创建一笔 CNY 法币入金订单。

POST /t-api/openapi/v1/op/openapi/createFiatPayInOrder

#### 特殊说明

- **限频**：同一商户每 60 秒最多调用 120 次，超出返回 `429100000`（HTTP 状态仍为 200）。
- **幂等**：同一商户下 `merchantOrderNo` 唯一。
  - 已经落成订单时，直接返回该单的**当前**信息，不比对本次请求的其他字段。此时订单可能已经不是 `PENDING_PAYMENT`，字段规则同[**查询法币收款订单详情**](#query-fiat-payin-order)。
  - 此前被拒时，按本次请求重新校验，不保留上一次的 `code` 与 `msg`。
  - 重复提交的请求仍要先通过参数校验与金额解析。
- **结果未知**：服务端内部调用失败时返回 `code = 500`，msg 为「系统异常」/ `System error`。此时订单可能已经创建，请用同一个 `merchantOrderNo` 重试或查单。
- 校验顺序见[**建单校验顺序**](#create-validation-order)。

> Body 请求参数

```json
{
  "merchantOrderNo": "B20261010000123",
  "userId": "U10023841",
  "fiatCurrency": "CNY",
  "fiatAmount": "216000",
  "customer": {
    "name": "张三",
    "idType": "ID_CARD",
    "idNo": "110101199001011234",
    "country": "CN",
    "ethnicity": "01",
    "dateOfBirth": "1990-05-20"
  },
  "returnUrl": "https://broker.example.com/deposit/result"
}
```

#### 请求参数

| 名称   | 位置   | 类型                          | 必选 | 说明   |
| ---- | ---- | --------------------------- | -- | ---- |
| body | body | CreateFiatPayInOrderRequest | 是  | none |

#### CreateFiatPayInOrderRequest 属性

| 名称              | 类型     | 必选    | 约束                         | 中文名   | 说明                                                                 |
| --------------- | ------ | ----- | -------------------------- | ----- | ------------------------------------------------------------------ |
| merchantOrderNo | string | true  | 1–64 位，`[A-Za-z0-9_-]`     | 商户订单号 | 幂等键，同一商户内唯一                                                        |
| userId          | string | true  | 1–64 位                     | 客户 ID | 商户侧客户 ID，写入订单，查询、对账和 Webhook 中回传                                   |
| fiatCurrency    | string | true  | 仅 `CNY`                    | 法币币种  | 区分大小写，`cny` 会被拒绝                                                   |
| fiatAmount      | string | true  | 整数，200000–500000（闭区间）      | 法币金额  | 推荐写成不带小数点、千分位和前导零的整数。数值相等的写法（如 `216000.00`）也会被接受；带千分位或小数不为 0 时拒单 |
| customer        | object | true  | none                       | 客户资料  | 见[**customer 属性**](#customer)                                     |
| returnUrl       | string | true  | `http://` 或 `https://`，最长 1024 | 返回地址  | 结果页「返回商户」的跳转地址，按订单传入。不能含空白字符，协议须为小写                                |

#### customer 属性 {#customer}

| 名称          | 类型     | 必选    | 约束                             | 中文名   | 说明                                                       |
| ----------- | ------ | ----- | ------------------------------ | ----- | -------------------------------------------------------- |
| name        | string | true  | 1–64 字                         | 客户姓名  | 去首尾空格                                                    |
| idType      | string | true  | `ID_CARD` / `PASSPORT`         | 证件类型  | 区分大小写，见[**KYC 枚举**](#kyc)                               |
| idNo        | string | true  | 1–64 位                         | 证件号   | 入库与跨商户判重前去掉空格并转大写                                        |
| country     | string | true  | `CN` / `HK` / `MO` / `TW`      | 国家或地区 | 区分大小写                                                    |
| ethnicity   | string | false | `01`–`56`                      | 民族    | GB/T 3304 两位编码。不传则不会分到设置了民族条件的收款卡                        |
| dateOfBirth | string | false | `YYYY-MM-DD`                   | 出生日期  | 按 Asia/Shanghai 的当天校验，不能晚于当天。不传则不会分到设置了年龄条件的收款卡             |

- `name`、`idNo`、`userId` 的 64 位上限按去空格前的原文计算。
- 枚举、日期、URL、长度不合法时返回参数错误 `500100000`，`msg` 指出字段，见[**参数不合法**](#invalid-params)。
- 金额不是整数、不在区间内或无法解析时，返回场景⑥ `500105052`。

> 返回示例

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

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                      | 必选    | 约束   | 中文名 | 说明   |
| ------ | ----------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)          | false | none |     | none |
| » msg  | string                  | false | none |     | none |
| » data | FiatPayInOrderOpenApiVO | false | none |     | none |

#### FiatPayInOrderOpenApiVO 属性 {#fiat-payin-order-vo}

创建、单笔查询和分页查询返回的订单都是这个结构。尚未发生的字段不返回，不要当成 0。

| 名称                | 类型     | 何时返回                  | 说明                                                                                     |
| ----------------- | ------ | --------------------- | -------------------------------------------------------------------------------------- |
| orderNo           | string | 始终                    | Paypaz 订单号，`PZPI` + 上海日期 + `-` + 至少 5 位当日序号，例如 `PZPI20261010-00001`。全链路共用 |
| merchantOrderNo   | string | 始终                    | 商户订单号                                                                                  |
| userId            | string | 始终                    | 商户侧客户 ID                                                                               |
| fiatCurrency      | string | 始终                    | `CNY`                                                                                  |
| fiatAmount        | string | 始终                    | 订单金额 CNY，两位小数。入参是整数，出参补 `.00`                                                         |
| status            | string | 始终                    | 订单状态，见[**订单状态**](#status)。新建时固定为 `PENDING_PAYMENT`                                     |
| checkoutUrl       | string | 始终                    | 托管收银台地址，路径为 `/fiat-deposit?pageId=`。见下方说明                                              |
| expireAt          | string | 始终                    | 支付窗口截止时间，建单后 30 分钟。窗口内未上传凭证，订单变为 `CANCELLED`                                          |
| createdAt         | string | 始终                    | 建单时间                                                                                   |
| proofSubmittedAt  | string | 已提交凭证                 | 用户提交凭证的时间                                                                              |
| settlementRate    | string | `SUCCEEDED`           | 商户结算汇率 Rm，含义为 1 USDT = Rm CNY，4 位小数                                                    |
| creditAmount      | string | `SUCCEEDED`           | 商户入账 USDT，两位小数                                                                         |
| feeAmount         | string | `SUCCEEDED`           | 充值手续费 USDT，两位小数。为 0 时返回 `"0.00"`                                                      |
| netCreditAmount   | string | `SUCCEEDED`           | `creditAmount − feeAmount`，两位小数                                                        |
| completedAt       | string | `SUCCEEDED`           | 确认收款或裁定生效的时间                                                                           |
| refundAmount      | string | `REFUNDING` / `REFUNDED` | 实际退款 CNY，两位小数，可能小于订单金额                                                                |
| refundSubmittedAt | string | 收款服务商回传过退款凭证          | 回传退款凭证的时间，驳回后再次回传会更新。运营人工标记已退款的订单没有此字段                                                 |
| refundedAt        | string | `REFUNDED`            | Paypaz 确认退款或运营标记已退款的时间                                                                 |
| failReason        | string | `FAILED`              | `USER_NOT_PAID` / `FORGED_PROOF` / `WRONG_ACCOUNT` / `OTHER`                           |
| failedAt          | string | `FAILED`              | 失败时间                                                                                   |
| cancelledAt       | string | `CANCELLED`           | 取消时间，等于 `expireAt`                                                                     |

- **`checkoutUrl`**：
  - 每次返回的链接字符串都可能不同，但都指向同一笔订单，并且长期有效，终态后打开的是结果页。不要用它做比对或去重。
  - `pageId` 是加密令牌，不能解析，任何改动都会导致链接无效。
- **结算字段**：只在订单为 `SUCCEEDED` 时返回，新建的响应里没有。
- **不返回的信息**：收款服务商汇率 Rc、价差、收款服务商名称，以及卡号、户名、开户行。
- **申诉信息**：只在 Webhook `appealed` 中提供，查询接口不返回。

#### 建单校验顺序 {#create-validation-order}

任一步失败即拒绝，不建单：

1. 鉴权：缺少签名头 → 时间戳 → API Key → 签名 → IP 白名单 → scope。
2. 请求体字段格式（`500100000`）。
3. 限频（`429100000`）。
4. `fiatCurrency` 必须为 `CNY`（`500100000`）。
5. 金额能否解析成数字，不能则返回场景⑥。
6. 出生日期是否合法、是否晚于当天（`500100000`）。
7. 同一商户订单号已有订单时，直接返回该单。
8. 业务校验：⑦ 未开通 → ⑥ 金额 → ① 判重 → ② 资质 → 按 ④ ＞ ③ ＞ ⑤ 的优先级返回一个原因。

业务规则：
- **跨商户判重**：同一证件类型 + 证件号，在任意商户下已有进行中的订单（待付款、待核验、申诉中、退款中），返回场景①。这与「同一商户订单号幂等」不是一回事。
- **分配收款卡**：
  - 按单笔金额、国家、民族、年龄匹配收款卡，多张卡都匹配时按「最久未被分配」轮询。
  - 卡上没配的条件视为不限。卡配了某个条件而本单没传对应资料，视为不命中该卡。
- **资质命中的卡都不可用时**，按优先级只返回一个原因：可提现不足（④）＞ 不在服务时间或本时段剩余额度不足（③）＞ 卡停用、收款服务商停用、汇率不可用或收款服务商代收业务已关闭（⑤）。
- **服务时间**：按北京时间计算，含开始时刻、不含结束时刻。服务时间内都可以分配新订单，服务时间外不分配。
- **场景③ 推荐时段**：有可推荐的下一时段时，`data` 带 `startTime` / `endTime`，格式为 `HH:mm`，不带日期。取所有资质命中且启用的收款卡中最早开始的时段：当天的时段还没开始就推荐当天，否则推荐次日。

```json
{
  "code": 500105049,
  "msg": "当前时段服务暂停，请于 09:00 - 17:00 再试。",
  "data": { "startTime": "09:00", "endTime": "17:00" }
}
```

#### 错误码

- `500105010`：API Key 未勾选 Fiat Pay-in
- `500100000`：参数不合法，见[**参数不合法**](#invalid-params)
- `429100000`：请求频率超限
- `500105047`–`500105053`：建单拒绝，见[**建单拒绝**](#create-rejections)
- `500`：结果未知，请用同一个 `merchantOrderNo` 重试或查单

---

### 2.GET 查询法币收款订单详情 {#query-fiat-payin-order}

按订单号或商户订单号查询单笔订单。只能查到本商户的订单。

GET /t-api/openapi/v1/op/openapi/fiatPayInOrderInfo

#### 请求参数

| 名称              | 位置    | 类型     | 必选 | 说明         |
| --------------- | ----- | ------ | -- | ---------- |
| orderNo         | query | string | 否  | Paypaz 订单号 |
| merchantOrderNo | query | string | 否  | 商户订单号      |

- `orderNo` 与 `merchantOrderNo` 至少传一个，都不传返回 `500105008`。
- 两个都传时必须指向同一笔订单，否则按订单不存在处理，返回 `500105054`。
- 签名时 `requestPath` 要包含查询串，并且与实际发送的 URL 完全一致，例如 `/t-api/openapi/v1/op/openapi/fiatPayInOrderInfo?orderNo=PZPI20261010-00001`。

> 返回示例

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

#### 返回数据结构

状态码 **200**

| 名称     | 类型                      | 必选    | 约束   | 中文名 | 说明                                          |
| ------ | ----------------------- | ----- | ---- | --- | ------------------------------------------- |
| » code | integer(int32)          | false | none |     | none                                        |
| » msg  | string                  | false | none |     | none                                        |
| » data | FiatPayInOrderOpenApiVO | false | none |     | 见[**FiatPayInOrderOpenApiVO 属性**](#fiat-payin-order-vo) |

#### 错误码

- `500105008`：`orderNo` 与 `merchantOrderNo` 都没传
- `500105054`：订单不存在、不属于本商户，或两个单号不指向同一笔
- `500105010`：API Key 未勾选 Fiat Pay-in

---

### 3.POST 分页查询法币收款订单 {#query-fiat-payin-order-list}

按当前 API Key 所属商户分页查询法币收款订单，按 `createdAt` 倒序。请求体可以省略。

POST /t-api/openapi/v1/op/openapi/fiatPayInOrders

> Body 请求参数

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

#### QueryFiatPayInOrderRequest 属性

| 名称              | 类型             | 必选    | 约束          | 中文名     | 说明                                    |
| --------------- | -------------- | ----- | ----------- | ------- | ------------------------------------- |
| merchantOrderNo | string         | false | none        | 商户订单号   | 精确匹配                                  |
| orderNo         | string         | false | none        | 订单号     | 精确匹配                                  |
| userId          | string         | false | none        | 客户 ID   | 精确匹配                                  |
| statuses        | array[string]  | false | 见订单状态       | 状态      | 空或不传表示全部                              |
| createdStart    | integer(int64) | false | 13 位毫秒      | 建单开始时间  | 含该时刻。须与 `createdEnd` 同时传或同时不传         |
| createdEnd      | integer(int64) | false | 13 位毫秒      | 建单结束时间  | 含该时刻。起止间隔不超过 30 天                     |
| pageNo          | integer(int32) | false | ≥ 1         | 页码      | 默认 1                                  |
| pageSize        | integer(int32) | false | 1–100       | 每页条数    | 默认 20                                 |

> 返回示例

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

#### 返回数据结构

| 名称           | 类型                             | 说明                                       |
| ------------ | ------------------------------ | ---------------------------------------- |
| » total      | string                         | 符合条件的订单总数，十进制字符串                         |
| » pageNo     | integer(int32)                 | 页码                                       |
| » pageSize   | integer(int32)                 | 每页条数                                     |
| » list       | array[FiatPayInOrderOpenApiVO] | 每一项与单笔查询的 `data` 相同，见[**FiatPayInOrderOpenApiVO 属性**](#fiat-payin-order-vo) |

- 支付窗口到期的订单，在系统完成取消处理之前，按 `statuses` 筛选时仍归在 `PENDING_PAYMENT` 下，但返回的 `status` 已显示为 `CANCELLED`。

#### 错误码

- `500105036`：只传了 `createdStart` 或 `createdEnd` 其中一个
- `500105037`：`createdStart` 大于 `createdEnd`
- `500105038`：起止间隔超过 30 天
- `500105039`：时间不是 13 位毫秒时间戳
- `500100000`：页码或每页条数不合法，或 `statuses` 含有未知值
- `500105010`：API Key 未勾选 Fiat Pay-in

---

### 4.Webhook {#webhook}

在商户后台「开发者 › Webhooks」配置回调地址并订阅事件。每个启用的回调地址各推一次；回调地址设置了事件订阅时，只推送订阅的事件。签名、请求头与重试规则见 [**webhook说明**](WEBHOOK_GUIDE.md)，要点如下：

| 项    | 值                                                                         |
| ---- | ------------------------------------------------------------------------- |
| 方法   | `POST`，`Content-Type: application/json`                                   |
| 签名头  | `PAYPAZ-WEBHOOK-SIGN`、`PAYPAZ-WEBHOOK-TIMESTAMP`                          |
| 签名原文 | `payload + "\|" + timestamp`，HMAC-SHA256 后 Base64。密钥为商户后台生成的 Webhook 公钥（每个商户一把，所有回调地址共用）；未生成密钥时请求不带签名头 |
| 超时   | 连接、读取各 5 秒                                                                |
| 成功判定 | 响应体包含 `success`（不区分大小写），不看 HTTP 状态码                                       |
| 重试   | 首次投递失败后最多再重试 6 次（共 7 次），间隔约 1、2、3、4、5、6 分钟                               |

包体只有 `eventType` 和 `data`。`data` 的公共字段为 `orderNo`、`merchantOrderNo`、`userId`、`fiatAmount`。

| eventType                                 | 触发                                        | 额外字段                                                       |
| ----------------------------------------- | ----------------------------------------- | ---------------------------------------------------------- |
| `transaction.fiat.payin.proof_submitted`  | 用户提交凭证，进入待核验                              | `proofSubmittedAt`                                         |
| `transaction.fiat.payin.appealed`         | 进入申诉中（收款服务商或商户发起）                         | `appealType`、`appealedAt`                                  |
| `transaction.fiat.payin.refunding`        | 收款服务商回传退款凭证。驳回后再次回传会再推一次                  | `refundAmount`、`refundSubmittedAt`                         |
| `transaction.fiat.payin.completed`        | 确认收款，或裁定「无异常 / 资金已到账」后自动结算                | `settlementRate`、`creditAmount`、`feeAmount`、`completedAt`  |
| `transaction.fiat.payin.refunded`         | Paypaz 确认退款，或运营标记已退款                       | `refundAmount`、`refundedAt`                                |
| `transaction.fiat.payin.cancelled`        | 支付窗口内未提交凭证                                | `cancelledAt`                                              |
| `transaction.fiat.payin.failed`           | 裁定资金未到账                                   | `failReason`、`failedAt`                                    |

**载荷格式与查询接口不同：**
- 金额是去掉末尾 0 的十进制字符串，例如 `"216000"`、`"7.272"`；手续费为 0 时是 `"0"`。
- 时间是毫秒时间戳字符串。
- 载荷里没有 `fiatCurrency`、`status` 和 `netCreditAmount`。事件名已经对应状态；净入账请自行计算 `creditAmount − feeAmount`。

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

- **`appealType` 取值**：`ARRIVAL_ABNORMAL` 到账异常 / `NOT_RECEIVED` 未查到入账 / `MERCHANT` 商户申诉。
- **不推送的情况**：
  - 下单被同步拒绝时不推送。建单被拒与异步终态「失败」不同：前者是创建接口的错误码，后者是 `transaction.fiat.payin.failed`。
  - 退款凭证被驳回时不推送，订单回到申诉中；收款服务商重新回传后会再推一次 `refunding`。
  - 运营人工标记已退款时直接推 `refunded`，不会先推 `refunding`。
- **`cancelled` 的时机**：支付窗口到期后由定时任务推送，`cancelledAt` 等于 `expireAt`，不是推送时间。
- **去重**：回调不带事件 ID，请按 `orderNo + eventType + 该事件的时间字段` 去重。只按订单号 + 事件名去重，会丢掉第二次 `refunding`。
- **`completed` 的内容**：不含收款服务商汇率 Rc 与价差。手续费从商户余额另行扣收，`creditAmount` 不是先扣手续费后的金额。

---

### 订单状态 {#status}

终态唯一且不可逆。收银台把待核验、申诉中、退款中都显示为「处理中」；商户接口与 Webhook 使用下表的细分状态。

| status            | 中文  | 终态 | 含义                               |
| ----------------- | --- | -- | -------------------------------- |
| `PENDING_PAYMENT` | 待付款 |    | 已建单，等待用户转账并上传凭证                  |
| `VERIFYING`       | 待核验 |    | 凭证已提交                            |
| `APPEALING`       | 申诉中 |    | 平台裁定中，或已裁定需退款、等待回传               |
| `REFUNDING`       | 退款中 |    | 退款凭证已回传，等待 Paypaz 确认。驳回后回到申诉中    |
| `SUCCEEDED`       | 成功  | 是  | USDT 已入账                         |
| `REFUNDED`        | 已退款 | 是  | 原路退回，商户无入账                       |
| `FAILED`          | 失败  | 是  | 资金未到账，无退款                        |
| `CANCELLED`       | 已取消 | 是  | 支付窗口超时，未提交凭证                     |

---

### KYC 枚举 {#kyc}

#### 证件类型 idType

| 值          | 含义    |
| ---------- | ----- |
| `ID_CARD`  | 居民身份证 |
| `PASSPORT` | 护照    |

#### 国家或地区 country

| 值    | 含义   |
| ---- | ---- |
| `CN` | 中国大陆 |
| `HK` | 中国香港 |
| `MO` | 中国澳门 |
| `TW` | 中国台湾 |

收款卡的国家条件目前只能配置为 `CN` 或不限，因此 `HK`、`MO`、`TW` 的客户只能分到没有设置国家条件的卡。

#### 民族 ethnicity（GB/T 3304）

| 编码 | 民族    | 编码 | 民族    | 编码 | 民族    | 编码 | 民族   |
| -- | ----- | -- | ----- | -- | ----- | -- | ---- |
| 01 | 汉族    | 02 | 蒙古族   | 03 | 回族    | 04 | 藏族   |
| 05 | 维吾尔族  | 06 | 苗族    | 07 | 彝族    | 08 | 壮族   |
| 09 | 布依族   | 10 | 朝鲜族   | 11 | 满族    | 12 | 侗族   |
| 13 | 瑶族    | 14 | 白族    | 15 | 土家族   | 16 | 哈尼族  |
| 17 | 哈萨克族  | 18 | 傣族    | 19 | 黎族    | 20 | 傈僳族  |
| 21 | 佤族    | 22 | 畲族    | 23 | 高山族   | 24 | 拉祜族  |
| 25 | 水族    | 26 | 东乡族   | 27 | 纳西族   | 28 | 景颇族  |
| 29 | 柯尔克孜族 | 30 | 土族    | 31 | 达斡尔族  | 32 | 仫佬族  |
| 33 | 羌族    | 34 | 布朗族   | 35 | 撒拉族   | 36 | 毛南族  |
| 37 | 仡佬族   | 38 | 锡伯族   | 39 | 阿昌族   | 40 | 普米族  |
| 41 | 塔吉克族  | 42 | 怒族    | 43 | 乌孜别克族 | 44 | 俄罗斯族 |
| 45 | 鄂温克族  | 46 | 德昂族   | 47 | 保安族   | 48 | 裕固族  |
| 49 | 京族    | 50 | 塔塔尔族  | 51 | 独龙族   | 52 | 鄂伦春族 |
| 53 | 赫哲族   | 54 | 门巴族   | 55 | 珞巴族   | 56 | 基诺族  |

---

### 错误码 {#error-codes}

业务拒绝仍是 HTTP 200，用 body 的 `code` 区分。`Accept-Language` 的首选语言为 `zh-CN` 时 `msg` 为中文，其余为英文。除下面注明的情况外，`data` 为 `null`。请按错误码处理，不要改写文案含义。

创建、单笔查询、分页查询共用下面这些码。不要复用加密货币支付订单的 `500105033`–`500105035`。另外，`500105047` 在充币地址接口中表示「未开通 Meta Trader」，请按接口区分含义。

#### 建单拒绝 {#create-rejections}

| 场景         | code        | 含义                                                                                    | 中文 msg                     | English msg                                                                          |
| ---------- | ----------- | ------------------------------------------------------------------------------------- | -------------------------- | ------------------------------------------------------------------------------------ |
| ① 重复下单     | `500105047` | 同一证件类型 + 证件号已有进行中订单（待付款、待核验、申诉中、退款中），跨商户判重                                          | 您有一笔入金订单正在处理中，请完成后再发起。     | You have a deposit order in progress. Please complete it before starting a new one. |
| ② 资质不匹配    | `500105048` | 没有任何收款卡（含停用的卡）同时满足单笔金额、国家、民族、年龄；当前没有任何收款卡时也返回此码。金额已在平台区间内、但落不进任何卡的单笔区间，也走这个码             | 不满足服务商要求，请联系客服。            | Does not meet the service provider's requirements. Please contact support.           |
| ③ 暂停且有下一时段 | `500105049` | 命中的卡不在服务时间，或本时段剩余额度不足以容纳本单，并且能算出下一可服务时段。`data.startTime` / `data.endTime` 为 `HH:mm` | 当前时段服务暂停，请于 09:00 - 17:00 再试。 | Service is paused for the current period. Please try again between 09:00 and 17:00. |
| ③ 暂停且无下一时段 | `500105049` | 与上一行同一错误码。没有可推荐时段时 `data` 为 `null`，文案不同                                               | 当前时段服务暂停，请稍后再试。            | Service is paused for the current period. Please try again later.                   |
| ④ 服务繁忙     | `500105050` | 匹配到的收款服务商可冻结 USDT 不足。建单要排队的锁（同一商户订单号、同一证件、同一收款服务商）5 秒内没拿到时也返回此码，文案相同             | 当前服务繁忙，请联系客服，稍后再试。         | The service is busy. Please contact customer support or try again later.            |
| ⑤ 服务不可用    | `500105051` | 卡停用、收款服务商停用、汇率不可用，或收款服务商的代收业务已关闭                                                       | 当前服务不可用，请联系客服              | The service is unavailable. Please contact customer support.                         |
| ⑥ 金额不合法    | `500105052` | 金额不是整数、不在平台区间 200,000–500,000 内，或 `fiatAmount` 无法解析成数字。区间写在文案里，不随收款卡的单笔区间变化             | 入金金额需为 200,000 - 500,000 CNY 之间的整数 | The deposit amount must be a whole number between 200,000 and 500,000 CNY.          |
| ⑦ 未开通      | `500105053` | 该商户没有启用中的 CNY/USDT 法币收款配置                                                             | 服务暂未开通，请联系客服。              | Service is not available. Please contact customer support.                          |

#### 查询与列表

| code        | 接口 | 含义                                       | 中文 msg               | English msg                                                         |
| ----------- | -- | ---------------------------------------- | -------------------- | ------------------------------------------------------------------- |
| `500105054` | 单笔 | 订单不存在，或不属于本商户。两个单号都传了但不指向同一笔，同样返回此码       | 订单不存在                | Order not found                                                     |
| `500105008` | 单笔 | `orderNo` 与 `merchantOrderNo` 都没传         | 查询参数不能同时为空           | Query parameters cannot all be empty simultaneously                 |
| `500105036` | 列表 | 只传了 `createdStart` 或 `createdEnd` 其中一个    | 开始时间和结束时间不能为空        | Order query time cannot be null                                     |
| `500105037` | 列表 | `createdStart` 大于 `createdEnd`           | 开始时间不能大于结束时间         | Start time must not be later than end time                          |
| `500105038` | 列表 | 起止间隔超过 30 天                              | 开始时间与结束时间间隔不能超过30天   | The interval between start time and end time cannot exceed 30 days |
| `500105039` | 列表 | 时间不是 13 位毫秒时间戳                           | 时间格式错误，请传入正确的13位毫秒时间戳 | Invalid time format. Please enter a valid 13-digit millisecond timestamp. |

#### 鉴权与通用

三个接口在进入业务前都会校验，先后顺序为：缺签名头 → 时间戳 → API Key → 签名 → IP 白名单 → scope。

| code        | 含义                                  | 中文 msg          | English msg                                                                    |
| ----------- | ----------------------------------- | --------------- | ------------------------------------------------------------------------------ |
| `500105001` | 缺少签名头                               | 请求缺少必要的认证信息     | Request missing required authentication information                            |
| `500105002` | API Key 无效（含已停用的 Key）               | 无效的API Key      | Invalid API Key                                                                |
| `500105003` | 签名校验失败                              | 签名验证失败          | Signature verification failed                                                  |
| `500105004` | 时间戳超出接收窗口                           | 请求时间戳已过期        | Request timestamp has expired                                                  |
| `500105005` | 时间戳或接收窗口格式不对                        | 无效的时间戳格式        | Invalid timestamp format                                                       |
| `500105011` | 来源 IP 不在 API Key 的白名单中（白名单为空时不限制）    | IP不在白名单中        | IP address is not in the whitelist                                             |
| `500105010` | API Key 未勾选 Fiat Pay-in             | 没有权限访问该API      | No permission to access this API                                               |
| `429100000` | 建单接口超出限频（每商户每 60 秒 120 次），HTTP 状态仍为 200 | 访问过于频繁，请稍候再试    | You have exceeded the request limit. Please wait a moment before trying again. |
| `500`       | 服务端内部调用失败，结果未知。建单时请用同一个 `merchantOrderNo` 重试或查单 | 系统异常            | System error                                                                   |

#### 参数不合法 `500100000` {#invalid-params}

`data` 为 `null`。字段校验类 `msg` 是英文字段说明，不随 `Accept-Language` 切换。多个字段同时不合法时，用英文逗号加空格拼在同一个 `msg` 里，顺序不固定，同一字段可能出现多条。`fiatCurrency must be CNY`、出生日期、分页参数和 `statuses` 这几条在其余字段校验通过后才检查，一次只返回一条。

| 接口 | msg                                                                  | 含义                                         |
| -- | -------------------------------------------------------------------- | ------------------------------------------ |
| 创建 | `merchantOrderNo must not be blank`                                  | 商户订单号为空                                    |
| 创建 | `merchantOrderNo length must be between 1 and 64`                    | 商户订单号长度超出 1–64                             |
| 创建 | `merchantOrderNo contains invalid characters`                        | 含有 `[A-Za-z0-9_-]` 以外的字符                     |
| 创建 | `userId must not be blank`                                           | 客户 ID 为空                                   |
| 创建 | `userId length must be at most 64`                                   | 客户 ID 超过 64 位                              |
| 创建 | `fiatCurrency must not be blank`                                     | 法币为空                                       |
| 创建 | `fiatCurrency must be CNY`                                           | 法币不是 `CNY`                                 |
| 创建 | `fiatAmount must not be blank`                                       | 金额为空。无法解析成数字时走 `500105052`，不走本码             |
| 创建 | `customer must not be null`                                          | 缺少 `customer`                              |
| 创建 | `customer.name must not be blank`                                    | 姓名为空                                       |
| 创建 | `customer.name length must be at most 64`                            | 姓名超过 64 字                                  |
| 创建 | `customer.idType must not be blank`                                  | 证件类型为空                                     |
| 创建 | `customer.idType must be ID_CARD or PASSPORT`                        | 证件类型不是这两个值                                 |
| 创建 | `customer.idNo must not be blank`                                    | 证件号为空                                      |
| 创建 | `customer.idNo length must be at most 64`                            | 证件号超过 64 位                                 |
| 创建 | `customer.country must not be blank`                                 | 国家为空                                       |
| 创建 | `customer.country must be CN, HK, MO or TW`                          | 国家不在这四个值里                                  |
| 创建 | `customer.ethnicity is invalid`                                      | 民族不是空或 `01`–`56`                           |
| 创建 | `customer.dateOfBirth must be YYYY-MM-DD`                            | 出生日期格式不对，或不是合法日期                           |
| 创建 | `customer.dateOfBirth must not be after today`                       | 出生日期晚于 Asia/Shanghai 的当天                   |
| 创建 | `returnUrl must not be blank`                                        | 返回地址为空                                     |
| 创建 | `returnUrl length must be at most 1024`                              | 返回地址超过 1024                                |
| 创建 | `returnUrl must start with http:// or https://`                      | 返回地址不是 http(s) URL                         |
| 列表 | `pageNo must be at least 1 and pageSize must be between 1 and 100`   | 页码小于 1，或每页条数不在 1–100                       |
| 列表 | `statuses contains an unknown value`                                 | 状态不是[**订单状态**](#status)中的 8 个值之一            |
| 全部 | `参数类型错误` / `parameter type error`                                  | 请求体不是合法 JSON，或字段类型不对。此条随语言切换                  |
| 全部 | `系统异常` / `System error`                                              | 未知的运行时异常。此条随语言切换                           |

```json
{
  "code": 500100000,
  "msg": "customer.country must be CN, HK, MO or TW",
  "data": null
}
```

---

### 金额、幂等与边界 {#rules}

- **汇率锁定**：汇率、加点、手续费在建单时锁定。之后调整只影响新订单，结算使用订单上的快照值。
- **结算口径**：
  - 商户结算汇率 Rm = 收款服务商汇率 Rc × (1 + 加点)，保留 4 位小数，向上取整。
  - 入账 `creditAmount` = 订单金额 ÷ Rm，向下取整到 2 位小数。
  - 手续费 `feeAmount`：按比例时为 `creditAmount × 费率`，固定金额时为配置的 USDT 值，都向上取整到 2 位小数。
  - 净入账 `netCreditAmount` = `creditAmount − feeAmount`。
- **示例**：216,000 CNY，收款汇率 7.20，加点 1%，手续费 0.5% → Rm `7.2720`，入账 `29702.97`，手续费 `148.52`，净入账 `29554.45`。
- **入账网络**：USDT 记入商户法币收款配置中的入账网络，默认 TRON。
- **幂等**：同一 `merchantOrderNo` 已经落成的订单一直可以再取；此前被拒则按本次请求重新校验。并发请求由服务端按「商户 + 商户订单号」加锁。
- **判重范围**：进行中订单按证件判重，范围跨所有商户。已取消、失败、成功、已退款的订单不拦截新单。支付窗口到期后、系统完成取消处理前的短暂时间内，该订单仍可能被视为进行中，触发场景①。
- **收银台**：
  - 语言跟随浏览器，`zh` 开头显示中文，其余显示英文；页面上可以手动切换。链接不带语言参数。
  - 收银台没有申诉入口。
  - 签名被改动、订单不存在或接口请求失败时，显示「链接无效」，且没有返回商户按钮。
  - 页面每 10 秒刷新一次状态。凭证提交满 30 分钟仍在处理中时，显示超时提示。
  - 「返回商户」按钮只出现在终态页，以及处理中超时之后。
  - 「返回商户」通过顶层窗口跳转。以 iframe 嵌入且设置了 `sandbox` 时，需要允许顶层跳转（`allow-top-navigation`）。
  - `checkoutUrl` 只发给本单用户。
- **商户申诉**：在商户后台「法币收款/付款 › 法币收款记录」中发起，不在这组 OpenAPI 里。仅待核验、凭证提交已满 30 分钟、且该单从未申诉过时可以发起。
