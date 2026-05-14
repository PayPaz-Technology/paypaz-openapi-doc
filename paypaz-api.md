# 提现业务接口

以下业务接口，都需要通过签名认证

---

### 提现对接流程说明
1. 调用公共接口 [**获取所有的网络**](base-data-api.md#get-all-networks) 获取所有支持的CHAIN信息。
2. 调用公共接口 [**获取该币种下所有的token配置**](base-data-api.md#get-token-config) 获取对应币种下的充提配置信息。
3. 发起提现前调用接口[**根据tokenId查询该商户下所有资产**](#query-user-asset)获取对应币种的余额（查询主账户余额，子账户的余额由商户自行管理）是否满足提现金额。
4. 调用接口[**根据UID发起提币**](#create-withdraw-order)创建提现订单。
5. 商户将接收到来自Paypaz的webhook通知。  
   **重要：** 商户需配置回调地址并订阅以下事件：
   - transaction.withdrawal.succeeded: 提现成功
   - transaction.withdrawal.failed: 提现失败
6. 如果不配置回调地址您可以通过以下方式获取提币结果：
   - [**根据客户子用户Uid、地址、订单号查询提币订单**](#query-withdraw-order-page)
   - [**查询提币订单详情**](#query-withdraw-order-detail)

---

### 1.GET 根据tokenId查询该商户下所有资产 {#query-user-asset}

查询指定币种或所有币种的资产信息

GET /t-api/openapi/v1/op/openapi/assets

#### tokenId 特殊说明

预发布环境tokenId: USDT 预发布环境chainId: TRON

#### 请求参数

| 名称      | 位置    | 类型     | 必选 | 说明   |
| ------- | ----- | ------ | -- | ---- |
| tokenId | query | string | 否  | 币种ID |

> 返回示例

> 200 Response

```
{"code":200,"msg":"success","data":[{"id":"1953034545579294720","accountId":"1951221207996780544","tokenId":"USDT","userId":"468001460","total":"0.070013132010012112","assetTotal":"0.070013132010012112","availableAssetTotal":"0.070013132010012112","marketPrice":"0","locked":"0","available":"0.070013132010012112","indebted":"0","createdAt":"1754474695950","updatedAt":"1754559221966","accountType":1},{"id":"1953010034274480128","accountId":"1951221207996780544","tokenId":"BNB","userId":"468001460","total":"1","assetTotal":"799.31","availableAssetTotal":"798.51069","marketPrice":"799.31","locked":"0.001","available":"0.999","indebted":"0","createdAt":"1754468851999","updatedAt":"1754559160300","accountType":1},{"id":"1952934600546816000","accountId":"1951221207996780544","tokenId":"TBSC_BNB","userId":"468001460","total":"0.272193990000000101","assetTotal":"0.272193990000000101","availableAssetTotal":"0.272098890000000101","marketPrice":"0","locked":"0.0000951","available":"0.272098890000000101","indebted":"0","createdAt":"1754450867197","updatedAt":"1754920359479","accountType":1}]}
```

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                     | 必选    | 约束   | 中文名 | 说明    |
| ------ | ---------------------- | ----- | ---- | --- | ----- |
| » code | integer(int32)         | false | none |     | none  |
| » msg  | string                 | false | none |     | none  |
| » data | \[\[BalanceOpenApiVO]] | false | none |     | \[资产] |

#### BalanceOpenApiVO属性

| 名称        | 类型     | 必选    | 约束   | 中文名 | 说明     |
| --------- | ------ | ----- | ---- | --- | ------ |
| id        | string | false | none |     | none   |
| accountId | string | false | none |     | 账号     |
| tokenId   | string | false | none |     | none   |
| userId    | string | false | none |     | none   |
| total     | string | false | none |     | 总资产    |
| locked    | string | false | none |     | 已冻结资产。 |
| available | string | false | none |     | 可用资产   |
| indebted  | string | false | none |     | 负债资产   |
| createdAt | string | false | none |     | 创建时间   |
| updatedAt | string | false | none |     | 更新时间   |

---

### 2.POST 根据UID发起提币 {#create-withdraw-order}

为指定子用户创建提币订单，这个子用户的 2FA 要由调用方自己来验证。

POST /t-api/openapi/v1/op/openapi/createWithdrawal

#### tokenId 特殊说明

预发布环境tokenId: USDT 预发布环境chainId: TRON

> Body 请求参数

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

#### 请求参数

| 名称   | 位置   | 类型                         | 必选 | 说明   |
| ---- | ---- | -------------------------- | -- | ---- |
| body | body | \[CreateWithdrawalRequest] | 否  | none |

#### CreateWithdrawalRequest 属性

| 名称                      | 类型      | 必选   | 约束   | 中文名 | 说明                                        |
| ----------------------- | ------- | ---- | ---- | --- | ----------------------------------------- |
| clientSubUserId         | string  | true | none |     | 客户子用户唯一标识                                 |
| tokenId                 | string  | true | none |     | 币种ID                                      |
| chainId                 | string  | true | none |     | 链ID                                       |
| address                 | string  | true | none |     | 提币地址                                      |
| amount                  | string  | true | none |     | 提币数量                                      |
| clientWithdrawalId      | string  | true | none |     | 客户端订单ID（用于幂等性控制）                          |
| twoFactorAuthentication | boolean | true | none |     | Two-factor authentication (2FA) confirmed |

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":"string","clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainTokenId":"string","userId":"string","subUserId":"string","address":"string","arriveQuantity":"string","statusInfo":0,"createdAt":"string","updatedAt":"string","arriveTime":"string","totalQuantity":"string","txId":"string"}}
```

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                          | 必选    | 约束   | 中文名 | 说明   |
| ------ | --------------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)              | false | none |     | none |
| » msg  | string                      | false | none |     | none |
| » data | \[WithdrawalOrderOpenApiVO] | false | none |     | none |

#### WithdrawalOrderOpenApiVO 属性

| 名称                 | 类型             | 必选    | 约束   | 中文名 | 说明                    |
| ------------------ | -------------- | ----- | ---- | --- | --------------------- |
| id                 | string         | false | none |     | 平台生成                  |
| clientWithdrawalId | string         | false | none |     | 客户端订单ID               |
| transactionId      | string         | false | none |     | 订单ID                  |
| tokenId            | string         | false | none |     | tokenId               |
| chainId            | string         | false | none |     | 链tokenId              |
| userId             | string         | false | none |     | none                  |
| subUserId          | string         | false | none |     | 企业-子用户id              |
| address            | string         | false | none |     | 提现地址 用户的              |
| platformFee        | string         | false | none |     | 手续费                   |
| arriveQuantity     | string         | false | none |     | 实际提币数量                |
| statusInfo         | integer(int32) | false | none |     | 1,提币处理 2，提币成功， 3，提币失败 |
| createdAt          | string         | false | none |     | 创建时间                  |
| updatedAt          | string         | false | none |     | 更新时间                  |
| arriveTime         | string         | false | none |     | 提现到账时间                |
| totalQuantity      | string         | false | none |     | 用户输入的提现数量             |
| txId               | string         | false | none |     | transaction hash      |

---

### 3.POST 根据客户子用户Uid、地址、订单号查询提币订单 {#query-withdraw-order-page}

分页查询指定条件下的提币订单列表

POST /t-api/openapi/v1/op/openapi/withdrawalOrders

#### 特殊说明

预发布环境tokenId: USDT 预发布环境chainId: TRON

> Body 请求参数

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

#### 请求参数

| 名称   | 位置   | 类型                          | 必选 | 说明   |
| ---- | ---- | --------------------------- | -- | ---- |
| body | body | QueryWithdrawalOrderRequest | 否  | none |

#### QueryWithdrawalOrderRequest 属性

| 名称                 | 类型             | 必选    | 约束   | 中文名 | 说明            |
| ------------------ | -------------- | ----- | ---- | --- | ------------- |
| clientSubUserId    | string         | false | none |     | 客户子用户唯一标识     |
| address            | string         | false | none |     | 提币地址          |
| clientWithdrawalId | string         | false | none |     | 客户端提币订单ID     |
| tokenId            | string         | false | none |     | 币种ID          |
| chainId            | string         | false | none |     | 链ID           |
| txId               | string         | false | none |     | 交易ID（区块链交易哈希） |
| startTime          | string         | false | none |     | 开始时间（毫秒时间戳）   |
| endTime            | string         | false | none |     | 结束时间（毫秒时间戳）   |
| pageNo             | integer(int32) | false | none |     | 页码，从1开始       |
| pageSize           | integer(int32) | false | none |     | 每页大小，范围1-100  |

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":"string","clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainId":"string","userId":"string","subUserId":"string","address":"string","platformFee":"string","arriveQuantity":"string","statusInfo":0,"createdAt":"string","updatedAt":"string","arriveTime":"string","totalQuantity":"string","txId":"string"}]}
```

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                             | 必选    | 约束   | 中文名 | 说明   |
| ------ | ------------------------------ | ----- | ---- | --- | ---- |
| » code | integer(int32)                 | false | none |     | none |
| » msg  | string                         | false | none |     | none |
| » data | \[\[WithdrawalOrderOpenApiVO]] | false | none |     | none |

#### WithdrawalOrderOpenApiVO 属性

| 名称                 | 类型             | 必选    | 约束   | 中文名 | 说明                    |
| ------------------ | -------------- | ----- | ---- | --- | --------------------- |
| id                 | string         | false | none |     | 平台生成                  |
| clientWithdrawalId | string         | false | none |     | 客户端订单ID               |
| transactionId      | string         | false | none |     | 订单ID                  |
| tokenId            | string         | false | none |     | tokenId               |
| chainId            | string         | false | none |     | 链tokenId              |
| userId             | string         | false | none |     | none                  |
| subUserId          | string         | false | none |     | 企业-子用户id              |
| address            | string         | false | none |     | 提现地址 用户的              |
| platformFee        | string         | false | none |     | 手续费                   |
| arriveQuantity     | string         | false | none |     | 实际提币数量                |
| statusInfo         | integer(int32) | false | none |     | 1,提币处理 2，提币成功， 3，提币失败 |
| createdAt          | string         | false | none |     | 创建时间                  |
| updatedAt          | string         | false | none |     | 更新时间                  |
| arriveTime         | string         | false | none |     | 提现到账时间                |
| totalQuantity      | string         | false | none |     | 用户输入的提现数量             |
| txId               | string         | false | none |     | transaction hash      |


---

### 4.GET 查询提币订单详情 {#query-withdraw-order-detail}

根据客户端提币订单ID查询提币订单详细信息

GET /t-api/openapi/v1/op/openapi/withdrawalOrderInfo

#### tokenId 特殊说明

预发布环境tokenId: USDT 预发布环境chainId: TRON

#### 请求参数

| 名称                 | 位置    | 类型     | 必选 | 说明        |
| ------------------ | ----- | ------ | -- | --------- |
| clientWithdrawalId | query | string | 是  | 客户端提币订单ID |

> 返回示例

> 200 Response

```
{"code":200,"msg":"success","data":{"id":"1954903600726814720","clientWithdrawalId":"WKxXXnkaD0luIGvnZVrglg7UALaYDPTLiQdEbYvUZjL9qI4ekEqW","transactionId":null,"tokenId":"TBSC_BNB","chainTokenId":"TBSC_BNB","userId":"468001460","subUserId":"468001460825249908","address":"0xa8c5eea944c3af945203e18cf990905519a158ad","arriveQuantity":"0.000000099","statusInfo":1,"createdAt":"1754920313415","updatedAt":"1754920313415","arriveTime":null,"totalQuantity":"0.0000001","txId":null}}
```

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型                          | 必选    | 约束   | 中文名 | 说明   |
| ------ | --------------------------- | ----- | ---- | --- | ---- |
| » code | integer(int32)              | false | none |     | none |
| » msg  | string                      | false | none |     | none |
| » data | \[WithdrawalOrderOpenApiVO] | false | none |     | none |

#### WithdrawalOrderOpenApiVO 属性

| 名称                 | 类型             | 必选    | 约束   | 中文名 | 说明                    |
| ------------------ | -------------- | ----- | ---- | --- | --------------------- |
| id                 | string         | false | none |     | 平台生成                  |
| clientWithdrawalId | string         | false | none |     | 客户端订单ID               |
| transactionId      | string         | false | none |     | 订单ID                  |
| tokenId            | string         | false | none |     | tokenId               |
| chainId            | string         | false | none |     | 链tokenId              |
| userId             | string         | false | none |     | none                  |
| subUserId          | string         | false | none |     | 企业-子用户id              |
| address            | string         | false | none |     | 提现地址 用户的              |
| platformFee        | string         | false | none |     | 手续费                   |
| arriveQuantity     | string         | false | none |     | 实际提币数量                |
| statusInfo         | integer(int32) | false | none |     | 1,提币处理 2，提币成功， 3，提币失败 |
| createdAt          | string         | false | none |     | 创建时间                  |
| updatedAt          | string         | false | none |     | 更新时间                  |
| arriveTime         | string         | false | none |     | 提现到账时间                |
| totalQuantity      | string         | false | none |     | 用户输入的提现数量             |
| txId               | string         | false | none |     | transaction hash      |

