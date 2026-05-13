# 充值业务接口

以下业务接口，都需要通过签名认证

---

### 充值对接流程说明

1. 调用公共接口 [**获取所有的网络**](base-data-api.md#get-all-networks) 获取所有支持的CHAIN信息。
2. 调用公共接口 [**获取该币种下所有的token配置**](base-data-api.md#get-token-config) 获取对应币种下的充提配置信息。
3. 充值有两种方式：
   - 订单模式: 调用接口[**创建支付订单**](#create-payin-order)直接为对应的用户创建待支付订单。
   - 无订单模式: 调用接口[**根据子用户uid和tokenid获取充值地址**](#create-deposit-address)为用户生成充币地址 。  
   两种模式商户根据自己的业务的处理逻辑使用。
4. 用户链上充值完成
5. 商户将接收到来自Paypaz的webhook通知  
   **重要：** 商户需配置回调地址并订阅以下四个事件
   - transaction.payinorder.expired: 支付订单过期事件。
   - transaction.payinorder.underpaid: 支付订单未足额支付事件。
   - transaction.payinorder.completed: 支付订单完成事件。
   - transaction.deposit.succeeded: 充值成功事件。 

   >  - 充值成功通知一定会发送(链上转账成功)  
   >  - 如果存在支付订单未过期时将同步收到 `支付订单未足额支付`或`支付订单完成`事件通知  
   >  - 如果支付订单过期后将收到类型 `支付订单过期` 事件通知
   
6. 如果不配置回调地址您可以通过以下方式获取充值结果：
   - 订单模式可通过轮询接口[**分页查询支付订单**](#query-payin-order-page)或[**查询支付订单详情**](#query-payin-order-detail)获取支付订单信息（可选）。
   - 无订单模式可通过接口[**根据客户子UID，地址，订单号查询充值订单**](#query-deposit-order)查询充值订单（可选）。

**备注：**
- 支付订单是指商户通过订单模式创建的支付订单（仅在订单模式下有此支付订单）。
- 充值订单是指Paypaz接收到链上的转账成功通知。



---

### 1.POST 创建支付订单 {#create-payin-order}

为指定子用户创建支付订单。

POST /t-api/openapi/v1/op/openapi/createPayInOrder

#### 特殊说明

预发布环境 tokenId: USDT 预发布环境 chainId: TRON

使用 (TRON 测试链) 在UAT环境进行测试， 测试币水龙头：https://nileex.io/join/getJoinPage 领取 USDT test tokens

> Body 请求参数

```json
{
  "clientSubUserId": "csub_abc123",
  "payOrderNo": "P202601010001",
  "tokenId": "USDT",
  "chainId": "TRON",
  "payAmount": 10
}
```

#### 请求参数

| 名称   | 位置   | 类型                     | 必选 | 说明   |
| ---- | ---- | ---------------------- | -- | ---- |
| body | body | CreatePayInOrderRequest | 否  | none |

#### CreatePayInOrderRequest 属性

| 名称              | 类型             | 必选   | 约束   | 中文名 | 说明                              |
| --------------- | -------------- | ---- | ---- | --- |---------------------------------|
| clientSubUserId | string         | true | none |     | 客户子用户唯一标识                       |
| payOrderNo      | string         | true | none |     | 支付订单号(唯一键，幂等)                   |
| tokenId         | string         | true | none |     | 币种ID                            |
| chainId         | string         | true | none |     | 链ID                             |
| payAmount       | number         | true | none |     | 支付金额(必须大于0，大于等于最低充值金额，并且支持2位精度) |

> 返回示例

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

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                     | 必选    | 约束   | 中文名 | 说明   |
| ------ | ---------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)         | false | none |     | none |
| » msg  | string                 | false | none |     | none |
| » data | PayInOrderOpenApiVO    | false | none |     | none |

#### PayInOrderOpenApiVO 属性

| 名称          | 类型             | 必选    | 约束   | 中文名 | 说明                    |
| ----------- | -------------- | ----- | ---- | --- | --------------------- |
| id          | integer(int64) | false | none |     | ID                    |
| payOrderNo  | string         | false | none |     | 支付订单号                 |
| orderStatus | integer(int32) | false | none |     | 订单状态(1=处理中,2=未足额支付,3=已经完成,4=已经过期) |
| orderSource | integer(int32) | false | none |     | 订单来源(1=broker,2=paypaz) |
| tokenId     | string         | false | none |     | tokenId               |
| chainId     | string         | false | none |     | chainId               |
| walletAddress | string       | false | none |     | 钱包地址                  |
| payAmount   | string         | false | none |     | 支付金额                  |
| amount      | string         | false | none |     | 链上金额                  |
| fee         | string         | false | none |     | 手续费                   |
| netAmount   | string         | false | none |     | 收到金额（扣除手续费后金额）        |
| arriveTime  | string         | false | none |     | 到账时间(毫秒时间戳)            |
| fromAddress | string         | false | none |     | 来源地址                  |
| txId        | string         | false | none |     | 交易ID（区块链交易哈希）         |
| expireSeconds | integer(int32) | false | none |     | 有效期(秒)                 |
| userId      | integer(int64) | false | none |     | 用户ID(主账号)              |
| subUserId   | integer(int64) | false | none |     | 子用户ID                  |
| createdAt   | string         | false | none |     | 创建时间(毫秒时间戳)            |
| updatedAt   | string         | false | none |     | 修改时间(毫秒时间戳)            |

#### 错误码

- `500105033`：支付订单号已存在
- `500105034`：支付订单金额精度超过限制
- `500105035`：支付金额不能小于最小充币金额

---

### 2.GET 查询支付订单详情 {#query-payin-order-detail}

根据支付订单号查询支付订单详细信息

GET /t-api/openapi/v1/op/openapi/payInOrderInfo

#### 请求参数

| 名称        | 位置    | 类型     | 必选 | 说明     |
| --------- | ----- | ------ | -- | ------ |
| payOrderNo | query | string | 是  | 支付订单号 |

> 返回示例

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

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                  | 必选    | 约束   | 中文名 | 说明   |
| ------ | ------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)      | false | none |     | none |
| » msg  | string              | false | none |     | none |
| » data | PayInOrderOpenApiVO | false | none |     | none |

---

### 3.POST 分页查询支付订单 {#query-payin-order-page}

分页查询指定条件下的支付订单列表

POST /t-api/openapi/v1/op/openapi/payInOrders

#### 特殊说明

`startTime` 与 `endTime` 为必填项，当前要求长度为13位毫秒值，支持的最长查询间隔为30天。

> Body 请求参数

```json
{
  "subUid": 123456789,
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

#### 请求参数

| 名称   | 位置   | 类型                  | 必选 | 说明   |
| ---- | ---- | ------------------- | -- | ---- |
| body | body | QueryPayInOrderRequest | 否  | none |

#### QueryPayInOrderRequest 属性

| 名称              | 类型             | 必选    | 约束   | 中文名 | 说明            |
| --------------- | -------------- |-------| ---- | --- | ------------- |
| subUid          | integer(int64) | false | none |     | 子用户ID         |
| clientSubUserId | string         | false | none |     | 客户子用户唯一标识     |
| payOrderNo      | string         | false | none |     | 支付订单号         |
| depositOrderId  | integer(int64) | false | none |     | 充值记录id        |
| tokenId         | string         | false | none |     | 币种ID          |
| chainId         | string         | false | none |     | 链ID           |
| walletAddress   | string         | false | none |     | 钱包地址          |
| orderStatus     | integer(int32) | false | none |     | 订单状态(1=处理中,2=未足额支付,3=已经完成,4=已经过期) |
| startTime       | integer(int64) | true  | none |     | 开始时间（毫秒时间戳）   |
| endTime         | integer(int64) | true | none |     | 结束时间（毫秒时间戳）   |
| pageNo          | integer(int32) | false | none |     | 页码，从1开始       |
| pageSize        | integer(int32) | false | none |     | 每页大小，范围1-100  |

> 返回示例

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

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                        | 必选    | 约束   | 中文名 | 说明   |
| ------ | ------------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)            | false | none |     | none |
| » msg  | string                    | false | none |     | none |
| » data | \[\[PayInOrderOpenApiVO]] | false | none |     | none |

---

### 4.POST 根据子用户UID和tokenID获取充值地址 {#create-deposit-address}

为指定子用户和币种获取充值地址，如果不存在则创建新地址

POST /t-api/openapi/v1/op/openapi/depositAddress

#### tokenId 特殊说明

预发布环境tokenId: USDT 预发布环境chainId: TRON

> Body 请求参数

```json
{
  "clientSubUserId": "csub_abc123",
  "tokenId": "USDT",
  "chainId": "TRON"
}
```

#### 请求参数

| 名称   | 位置   | 类型                          | 必选 | 说明   |
| ---- | ---- | --------------------------- | -- | ---- |
| body | body | \[GetDepositAddressRequest] | 否  | none |

#### GetDepositAddressRequest 属性

| 名称              | 类型     | 必选   | 约束   | 中文名 | 说明        |
| --------------- | ------ | ---- | ---- | --- | --------- |
| clientSubUserId | string | true | none |     | 客户子用户唯一标识 |
| tokenId         | string | true | none |     | 币种ID      |
| chainId         | string | true | none |     | 链ID       |

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":"string","userId":"string","tokenId":"string","chainId":"string","address":"string","createdAt":"string","updatedAt":"string","tag":"string","subUserId":"string"}}
```

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                           | 必选    | 约束   | 中文名 | 说明                      |
| ------ | ---------------------------- | ----- | ---- | --- | ----------------------- |
| » code | integer(int32)               | false | none |     | none                    |
| » msg  | string                       | false | none |     | none                    |
| » data | \[SubWalletAddressOpenApiVO] | false | none |     | <p>企业子用户钱包地址实体类<br></p> |

#### SubWalletAddressOpenApiVO属性

| 名称        | 类型     | 必选    | 约束   | 中文名 | 说明                                   |
| --------- | ------ | ----- | ---- | --- | ------------------------------------ |
| id        | string | false | none |     | 主键ID                                 |
| userId    | string | false | none |     | <p>用户ID<br>主账号用户的标识符</p>             |
| tokenId   | string | false | none |     | <p>币种ID<br>例如：USDT，USDC等</p>         |
| chainId   | string | false | none |     | <p>chainId<br>例如：BNB，TRON</p>        |
| address   | string | false | none |     | <p>钱包地址<br>区块链上的钱包地址，用于接收充值</p>      |
| createdAt | string | false | none |     | <p>创建时间<br>记录创建的时间戳（毫秒）</p>          |
| updatedAt | string | false | none |     | <p>更新时间<br>记录最后更新的时间戳（毫秒）</p>        |
| tag       | string | false | none |     | <p>标签<br>某些币种（如XRP、XLM等）需要的额外标识符</p> |
| subUserId | string | false | none |     | <p>子用户ID<br>企业-子用户的ID，标识具体的子账号</p>   |

---

### 5.POST 根据客户子UID，地址，订单号查询充值订单 {#query-deposit-order}

分页查询充值订单, 根据条件查询充值订单列表，支持分页、时间范围、币种、钱包地址等筛选条件

POST /t-api/openapi/v1/op/openapi/depositOrders

#### tokenId 特殊说明

预发布环境tokenId: USDT 预发布环境chainId: TRON

> Body 请求参数

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

#### 请求参数

| 名称   | 位置   | 类型                          | 必选 | 说明   |
| ---- | ---- | --------------------------- | -- | ---- |
| body | body | \[QueryDepositOrderRequest] | 否  | none |

#### QueryDepositOrderRequest属性

| 名称              | 类型             | 必选    | 约束   | 中文名 | 说明           |
| --------------- | -------------- | ----- | ---- | --- | ------------ |
| clientSubUserId | string         | false | none |     | 客户子用户唯一标识    |
| walletAddress   | string         | false | none |     | 钱包地址         |
| orderNo         | string         | false | none |     | 订单号          |
| tokenId         | string         | false | none |     | 币种ID         |
| chainId         | string         | false | none |     | 链ID          |
| startTime       | string         | false | none |     | 开始时间（毫秒时间戳）  |
| endTime         | string         | false | none |     | 结束时间（毫秒时间戳）  |
| pageNo          | integer(int32) | false | none |     | 页码，从1开始      |
| pageSize        | integer(int32) | false | none |     | 每页大小，范围1-100 |

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":123456789,"orderNo":"D202501010001","tokenId":"USDT","chainId":"TRON","quantity":0.01,"fee":0,"netAmount":0.01,"fromAddress":"0x1234567890abcdef1234567890abcdef12345678","walletAddress":"0xabcdef1234567890abcdef1234567890abcdef12","txId":"0x9876543210abcdef9876543210abcdef98765432","status":1,"createdAt":1626307200000,"updatedAt":1626307260000,"userId":123456,"subUserId":789012}]}
```

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                          | 必选    | 约束   | 中文名 | 说明                 |
| ------ | --------------------------- | ----- | ---- | --- | ------------------ |
| » code | integer(int32)              | false | none |     | none               |
| » msg  | string                      | false | none |     | none               |
| » data | \[\[DepositOrderOpenApiVO]] | false | none |     | \[充值订单OpenAPI响应VO] |
|        |                             |       |      |     |                    |

#### DepositOrderOpenApiVO 属性

| 名称            | 类型             | 必选    | 约束   | 中文名 | 说明               |
| ------------- | -------------- | ----- | ---- | --- | ---------------- |
| id            | string         | false | none |     | 充值订单ID           |
| orderNo       | string         | false | none |     | 订单号              |
| tokenId       | string         | false | none |     | 币种ID             |
| chainId       | string         | false | none |     | 链上币种ID           |
| quantity      | string         | false | none |     | 充值数量             |
| fee           | string         | false | none |     | 手续费              |
| netAmount     | string         | false | none |     | 净入账金额            |
| fromAddress   | string         | false | none |     | 来源地址             |
| walletAddress | string         | false | none |     | 钱包地址             |
| txId          | string         | false | none |     | 交易ID（区块链交易哈希）    |
| status        | integer(int32) | false | none |     | 充值状态： 1: 完成 已到账; |
| createdAt     | string         | false | none |     | 创建时间（毫秒时间戳）      |
| updatedAt     | string         | false | none |     | 更新时间（毫秒时间戳）      |
| userId        | string         | false | none |     | 用户ID（主账号）        |
| subUserId     | string         | false | none |     | 子用户ID            |

