# 业务接口

以下业务接口，都需要通过签名认证

### 1.POST 根据客户子用户Uid，subUID、地址、订单号查询提币订单

分页查询指定条件下的提币订单列表

POST /t-api/openapi/v1/op/openapi/withdrawalOrders

#### 特殊说明

预发布环境tokenId: USDT 预发布环境chainId: TRON

> Body 请求参数

```json
{
  "subUid": 123456789,
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
| subUid             | string         | false | none |     | 子用户ID         |
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

### 2.POST 根据客户子UID，地址，订单号查询充值订单

分页查询充值订单, 根据条件查询充值订单列表，支持分页、时间范围、币种、钱包地址等筛选条件

POST /t-api/openapi/v1/op/openapi/depositOrders

#### tokenId 特殊说明

预发布环境tokenId: USDT 预发布环境chainId: TRON

> Body 请求参数

```json
 {
  "subUid": 123456789,
  "clientSubUserId": "csub_abc123",
  "walletAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "orderNo": "D202501010001",
  "tokenId": "USDC,USDT",
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
| subUid          | string         | false | none |     | 子用户ID        |
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

### 3.POST 根据子用户UID和tokenID获取充值地址

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

### 4.POST 根据UID发起提币

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

### 5.GET 查询提币订单详情

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

### 6.GET 根据tokenId查询该broker下所有资产

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

### 7.GET 获取该币种下所有的token配置

获取指定币种在不同链上的配置信息，包括充提币限额、手续费等

GET /t-api/openapi/v1/op/openapi/allToken

#### 请求参数

| 名称      | 位置    | 类型     | 必选 | 说明           |
| ------- | ----- | ------ | -- | ------------ |
| tokenId | query | string | 是  | 币种ID，例如：USDT |
| chainId | query | string | 否  | 链ID，例如：TRON  |

> 返回示例

> 200 Response

```json
{"code":200,"msg":"success","data":[{"id":"1","tokenId":"USDT","tokenName":"USDT","tokenFullName":"Tether USD","chainId":"TRON","createdAt":"1626307200000","updatedAt":"1626307200000","icon":"https://example.com/usdt.png","protocolName":"TRC20","chainName":"TRON","chainIcon":"https://example.com/tron.png","tokenChainId":"TRON_USDT","status":1,"allowDeposit":1,"allowWithdraw":1,"depositMinQuantity":"1","withdrawMinQuantity":"10","withdrawMaxDayQuantity":"100000","depositCharges":"0.001","depositChargesMinAmount":"0.1","withdrawalChargesMinAmount":"1","withdrawChargeValue":"0.01"}]}
```

#### 返回数据结构

状态码 **200**

_响应信息主体_

| 名称     | 类型             | 必选    | 约束   | 中文名 | 说明           |
| ------ | -------------- | ----- | ---- | --- | ------------ |
| » code | integer(int32) | false | none |     | none         |
| » msg  | string         | false | none |     | none         |
| » data | \[\[Token]]    | false | none |     | \[token基本配置] |

#### Token属性

| 名称                         | 类型             | 必选    | 约束   | 中文名 | 说明                    |
| -------------------------- | -------------- | ----- | ---- | --- | --------------------- |
| id                         | string         | false | none |     | none                  |
| tokenId                    | string         | false | none |     | 不用数字,"USDT"           |
| tokenName                  | string         | false | none |     | show name, can rename |
| tokenFullName              | string         | false | none |     | full name             |
| chainId                    | string         | false | none |     | 链tokenID,"TRON"       |
| createdAt                  | string         | false | none |     | none                  |
| updatedAt                  | string         | false | none |     | none                  |
| icon                       | string         | false | none |     | token icon            |
| protocolName               | string         | false | none |     | 链协议名称                 |
| chainName                  | string         | false | none |     | 链名称                   |
| chainIcon                  | string         | false | none |     | chain icon            |
| status                     | integer(int32) | false | none |     | none                  |
| allowDeposit               | integer(int32) | false | none |     | 1允许充币，0禁止             |
| allowWithdraw              | integer(int32) | false | none |     | 1允许提现，0禁止             |
| depositMinQuantity         | string         | false | none |     | 充币最小额度。小于这个额度，不入账     |
| withdrawMinQuantity        | string         | false | none |     | 提币的最小额度               |
| withdrawMaxDayQuantity     | string         | false | none |     | 该币种提币的最大额度            |
| depositCharges             | string         | false | none |     | 充币手续费百分比---broker 新配置 |
| depositChargesMinAmount    | string         | false | none |     | 充币的最小手续费---broker 新配置 |
| withdrawalChargesMinAmount | string         | false | none |     | 提币的最小手续费---broker 新配置 |
| withdrawChargeValue        | string         | false | none |     | 提币百分比手续费              |
