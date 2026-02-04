# 业务接口

以下业务接口，都需要通过签名认证

### 1.POST 创建支付订单

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

| 名称              | 类型             | 必选   | 约束   | 中文名 | 说明               |
| --------------- | -------------- | ---- | ---- | --- | ---------------- |
| clientSubUserId | string         | true | none |     | 客户子用户唯一标识        |
| payOrderNo      | string         | true | none |     | 支付订单号(唯一键，幂等)   |
| tokenId         | string         | true | none |     | 币种ID             |
| chainId         | string         | true | none |     | 链ID              |
| payAmount       | number         | true | none |     | 支付金额(必须大于0)      |

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

### 2.GET 查询支付订单详情

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

### 3.POST 分页查询支付订单

分页查询指定条件下的支付订单列表

POST /t-api/openapi/v1/op/openapi/payInOrders

#### 特殊说明

`subUid` 与 `clientSubUserId` 二选一必传。

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
| --------------- | -------------- | ----- | ---- | --- | ------------- |
| subUid          | integer(int64) | false | none |     | 子用户ID         |
| clientSubUserId | string         | false | none |     | 客户子用户唯一标识     |
| payOrderNo      | string         | false | none |     | 支付订单号         |
| depositOrderId  | integer(int64) | false | none |     | 充值记录id        |
| tokenId         | string         | false | none |     | 币种ID          |
| chainId         | string         | false | none |     | 链ID           |
| walletAddress   | string         | false | none |     | 钱包地址          |
| orderStatus     | integer(int32) | false | none |     | 订单状态(1=处理中,2=未足额支付,3=已经完成,4=已经过期) |
| startTime       | integer(int64) | false | none |     | 开始时间（毫秒时间戳）   |
| endTime         | integer(int64) | false | none |     | 结束时间（毫秒时间戳）   |
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
