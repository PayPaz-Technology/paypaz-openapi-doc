# Authentication

# Toocans Broker OpenAPI 文档

<a id="opIdqueryWithdrawalOrders"></a>

## POST 根据subUID、地址、订单号查询提币订单
 分页查询指定条件下的提币订单列表

POST /t-api/toocans-broker-api/v1/op/openapi/withdrawalOrders

根据subUID、地址、订单号查询提币订单
 分页查询指定条件下的提币订单列表

> Body 请求参数

```json
{
  "subUid": 123456789,
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "clientWithdrawalId": "client12345678",
  "tokenId": "TBSC_BNB",
  "txId": "0xabcd1234...",
  "startTime": 1626307200000,
  "endTime": 1626393600000,
  "pageNo": 1,
  "pageSize": 10
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[QueryWithdrawalOrderRequest](#schemaquerywithdrawalorderrequest)| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":0,"clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainTokenId":"string","userId":0,"subUserId":0,"address":"string","arriveQuantity":0,"status":0,"withdrawMethod":0,"createdAt":0,"updatedAt":0,"arriveTime":0,"totalQuantity":0,"txId":"string"}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|提币订单分页列表|[RPageWithdrawalOrderOpenApiVO](#schemarpagewithdrawalorderopenapivo)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)]|false|none||none|
|»» id|integer(int64)|false|none||平台生成|
|»» clientWithdrawalId|string|false|none||客户端订单ID|
|»» transactionId|string|false|none||订单ID|
|»» tokenId|string|false|none||tokenId|
|»» chainTokenId|string|false|none||链tokenId|
|»» userId|integer(int64)|false|none||none|
|»» subUserId|integer(int64)|false|none||企业-子用户id|
|»» address|string|false|none||提现地址 用户的|
|»» arriveQuantity|number|false|none||实际提币数量|
|»» status|integer(int32)|false|none||1,提币待审核中，2，系统自动审核，3人工审核通过 4 人工审核拒绝，5,钱包处理中,6钱包提交失败（可以重新请求）7,8 (订单提交中)  10，提币成功， 11 提币失败 12  人工审核拒绝失败|
|»» withdrawMethod|integer(int32)|false|none||0，init   1:区块链交易,2:内部转账|
|»» createdAt|integer(int64)|false|none||创建时间|
|»» updatedAt|integer(int64)|false|none||更新时间|
|»» arriveTime|integer(int64)|false|none||提现到账时间|
|»» totalQuantity|number|false|none||用户输入的提现数量|
|»» txId|string|false|none||transaction hash|
|»» stackTrace|string|false|none||none|
|»» pageNum|integer(int32)|false|none||none|
|»» pageSize|integer(int32)|false|none||none|
|»» startRow|integer(int64)|false|none||none|
|»» endRow|integer(int64)|false|none||none|
|»» total|integer(int64)|false|none||none|
|»» pages|integer(int32)|false|none||none|
|»» count|boolean|false|none||none|
|»» reasonable|boolean|false|none||none|
|»» pageSizeZero|boolean|false|none||none|
|»» countColumn|string|false|none||none|
|»» orderBy|string|false|none||none|
|»» orderByOnly|boolean|false|none||none|
|»» boundSqlInterceptor|[BoundSqlInterceptor](#schemaboundsqlinterceptor)|false|none||none|
|»» dialectClass|string|false|none||none|
|»» keepOrderBy|boolean|false|none||none|
|»» keepSubSelectOrderBy|boolean|false|none||none|
|»» asyncCount|boolean|false|none||none|
|»» result|[[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)]|false|none||none|
|»»» id|integer(int64)|false|none||平台生成|
|»»» clientWithdrawalId|string|false|none||客户端订单ID|
|»»» transactionId|string|false|none||订单ID|
|»»» tokenId|string|false|none||tokenId|
|»»» chainTokenId|string|false|none||链tokenId|
|»»» userId|integer(int64)|false|none||none|
|»»» subUserId|integer(int64)|false|none||企业-子用户id|
|»»» address|string|false|none||提现地址 用户的|
|»»» arriveQuantity|number|false|none||实际提币数量|
|»»» status|integer(int32)|false|none||1,提币待审核中，2，系统自动审核，3人工审核通过 4 人工审核拒绝，5,钱包处理中,6钱包提交失败（可以重新请求）7,8 (订单提交中)  10，提币成功， 11 提币失败 12  人工审核拒绝失败|
|»»» withdrawMethod|integer(int32)|false|none||0，init   1:区块链交易,2:内部转账|
|»»» createdAt|integer(int64)|false|none||创建时间|
|»»» updatedAt|integer(int64)|false|none||更新时间|
|»»» arriveTime|integer(int64)|false|none||提现到账时间|
|»»» totalQuantity|number|false|none||用户输入的提现数量|
|»»» txId|string|false|none||transaction hash|
|»» chain|[Chain](#schemachain)|false|none||none|
|»» empty|boolean|false|none||none|
|»» first|[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)|false|none||none|
|»»» id|integer(int64)|false|none||平台生成|
|»»» clientWithdrawalId|string|false|none||客户端订单ID|
|»»» transactionId|string|false|none||订单ID|
|»»» tokenId|string|false|none||tokenId|
|»»» chainTokenId|string|false|none||链tokenId|
|»»» userId|integer(int64)|false|none||none|
|»»» subUserId|integer(int64)|false|none||企业-子用户id|
|»»» address|string|false|none||提现地址 用户的|
|»»» arriveQuantity|number|false|none||实际提币数量|
|»»» status|integer(int32)|false|none||1,提币待审核中，2，系统自动审核，3人工审核通过 4 人工审核拒绝，5,钱包处理中,6钱包提交失败（可以重新请求）7,8 (订单提交中)  10，提币成功， 11 提币失败 12  人工审核拒绝失败|
|»»» withdrawMethod|integer(int32)|false|none||0，init   1:区块链交易,2:内部转账|
|»»» createdAt|integer(int64)|false|none||创建时间|
|»»» updatedAt|integer(int64)|false|none||更新时间|
|»»» arriveTime|integer(int64)|false|none||提现到账时间|
|»»» totalQuantity|number|false|none||用户输入的提现数量|
|»»» txId|string|false|none||transaction hash|
|»» last|[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)|false|none||none|
|»»» id|integer(int64)|false|none||平台生成|
|»»» clientWithdrawalId|string|false|none||客户端订单ID|
|»»» transactionId|string|false|none||订单ID|
|»»» tokenId|string|false|none||tokenId|
|»»» chainTokenId|string|false|none||链tokenId|
|»»» userId|integer(int64)|false|none||none|
|»»» subUserId|integer(int64)|false|none||企业-子用户id|
|»»» address|string|false|none||提现地址 用户的|
|»»» arriveQuantity|number|false|none||实际提币数量|
|»»» status|integer(int32)|false|none||1,提币待审核中，2，系统自动审核，3人工审核通过 4 人工审核拒绝，5,钱包处理中,6钱包提交失败（可以重新请求）7,8 (订单提交中)  10，提币成功， 11 提币失败 12  人工审核拒绝失败|
|»»» withdrawMethod|integer(int32)|false|none||0，init   1:区块链交易,2:内部转账|
|»»» createdAt|integer(int64)|false|none||创建时间|
|»»» updatedAt|integer(int64)|false|none||更新时间|
|»»» arriveTime|integer(int64)|false|none||提现到账时间|
|»»» totalQuantity|number|false|none||用户输入的提现数量|
|»»» txId|string|false|none||transaction hash|

<a id="opIdqueryDepositOrders"></a>

## POST 根据UID或地址查询充值订单
 分页查询指定条件下的充值订单列表

POST /t-api/toocans-broker-api/v1/op/openapi/depositOrders

根据UID或地址查询充值订单
 分页查询指定条件下的充值订单列表

> Body 请求参数

```json
{
  "subUid": 123456789,
  "walletAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "tokenId": "TBSC_BNB",
  "startTime": 1626307200000,
  "endTime": 1626393600000,
  "pageNo": 1,
  "pageSize": 10
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[QueryDepositOrderRequest](#schemaquerydepositorderrequest)| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":123456789,"tokenId":"TBSC_BNB","chainTokenId":"BNB","quantity":0.01,"fromAddress":"0x1234567890abcdef1234567890abcdef12345678","walletAddress":"0xabcdef1234567890abcdef1234567890abcdef12","txId":"0x9876543210abcdef9876543210abcdef98765432","status":1,"createdAt":1626307200000,"updatedAt":1626307260000,"userId":123456,"subUserId":789012}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|充值订单分页列表|[RPageDepositOrderOpenApiVO](#schemarpagedepositorderopenapivo)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[[DepositOrderOpenApiVO](#schemadepositorderopenapivo)]|false|none||[充值订单OpenAPI响应VO]|
|»» id|integer(int64)|false|none||充值订单ID|
|»» tokenId|string|false|none||币种ID|
|»» chainTokenId|string|false|none||链上币种ID|
|»» quantity|number|false|none||充值数量|
|»» fromAddress|string|false|none||来源地址|
|»» walletAddress|string|false|none||钱包地址|
|»» txId|string|false|none||交易ID（区块链交易哈希）|
|»» status|integer(int32)|false|none||充值状态： 0: 初始化 INIT; 1: 完成 已到账; 2: 处理中 充值中; 3: 失败|
|»» createdAt|integer(int64)|false|none||创建时间（毫秒时间戳）|
|»» updatedAt|integer(int64)|false|none||更新时间（毫秒时间戳）|
|»» userId|integer(int64)|false|none||用户ID（主账号）|
|»» subUserId|integer(int64)|false|none||子用户ID|
|»» stackTrace|string|false|none||none|
|»» pageNum|integer(int32)|false|none||none|
|»» pageSize|integer(int32)|false|none||none|
|»» startRow|integer(int64)|false|none||none|
|»» endRow|integer(int64)|false|none||none|
|»» total|integer(int64)|false|none||none|
|»» pages|integer(int32)|false|none||none|
|»» count|boolean|false|none||none|
|»» reasonable|boolean|false|none||none|
|»» pageSizeZero|boolean|false|none||none|
|»» countColumn|string|false|none||none|
|»» orderBy|string|false|none||none|
|»» orderByOnly|boolean|false|none||none|
|»» boundSqlInterceptor|[BoundSqlInterceptor](#schemaboundsqlinterceptor)|false|none||none|
|»» dialectClass|string|false|none||none|
|»» keepOrderBy|boolean|false|none||none|
|»» keepSubSelectOrderBy|boolean|false|none||none|
|»» asyncCount|boolean|false|none||none|
|»» result|[[DepositOrderOpenApiVO](#schemadepositorderopenapivo)]|false|none||[充值订单OpenAPI响应VO]|
|»»» id|integer(int64)|false|none||充值订单ID|
|»»» tokenId|string|false|none||币种ID|
|»»» chainTokenId|string|false|none||链上币种ID|
|»»» quantity|number|false|none||充值数量|
|»»» fromAddress|string|false|none||来源地址|
|»»» walletAddress|string|false|none||钱包地址|
|»»» txId|string|false|none||交易ID（区块链交易哈希）|
|»»» status|integer(int32)|false|none||充值状态： 0: 初始化 INIT; 1: 完成 已到账; 2: 处理中 充值中; 3: 失败|
|»»» createdAt|integer(int64)|false|none||创建时间（毫秒时间戳）|
|»»» updatedAt|integer(int64)|false|none||更新时间（毫秒时间戳）|
|»»» userId|integer(int64)|false|none||用户ID（主账号）|
|»»» subUserId|integer(int64)|false|none||子用户ID|
|»» chain|[Chain](#schemachain)|false|none||none|
|»» empty|boolean|false|none||none|
|»» first|[DepositOrderOpenApiVO](#schemadepositorderopenapivo)|false|none||充值订单OpenAPI响应VO|
|»»» id|integer(int64)|false|none||充值订单ID|
|»»» tokenId|string|false|none||币种ID|
|»»» chainTokenId|string|false|none||链上币种ID|
|»»» quantity|number|false|none||充值数量|
|»»» fromAddress|string|false|none||来源地址|
|»»» walletAddress|string|false|none||钱包地址|
|»»» txId|string|false|none||交易ID（区块链交易哈希）|
|»»» status|integer(int32)|false|none||充值状态： 0: 初始化 INIT; 1: 完成 已到账; 2: 处理中 充值中; 3: 失败|
|»»» createdAt|integer(int64)|false|none||创建时间（毫秒时间戳）|
|»»» updatedAt|integer(int64)|false|none||更新时间（毫秒时间戳）|
|»»» userId|integer(int64)|false|none||用户ID（主账号）|
|»»» subUserId|integer(int64)|false|none||子用户ID|
|»» last|[DepositOrderOpenApiVO](#schemadepositorderopenapivo)|false|none||充值订单OpenAPI响应VO|
|»»» id|integer(int64)|false|none||充值订单ID|
|»»» tokenId|string|false|none||币种ID|
|»»» chainTokenId|string|false|none||链上币种ID|
|»»» quantity|number|false|none||充值数量|
|»»» fromAddress|string|false|none||来源地址|
|»»» walletAddress|string|false|none||钱包地址|
|»»» txId|string|false|none||交易ID（区块链交易哈希）|
|»»» status|integer(int32)|false|none||充值状态： 0: 初始化 INIT; 1: 完成 已到账; 2: 处理中 充值中; 3: 失败|
|»»» createdAt|integer(int64)|false|none||创建时间（毫秒时间戳）|
|»»» updatedAt|integer(int64)|false|none||更新时间（毫秒时间戳）|
|»»» userId|integer(int64)|false|none||用户ID（主账号）|
|»»» subUserId|integer(int64)|false|none||子用户ID|

<a id="opIdgetDepositAddress"></a>

## POST 根据子用户UID和tokenID获取充值地址
 为指定子用户和币种获取充值地址，如果不存在则创建新地址

POST /t-api/toocans-broker-api/v1/op/openapi/depositAddress

根据子用户UID和tokenID获取充值地址
 为指定子用户和币种获取充值地址，如果不存在则创建新地址

> Body 请求参数

```json
{
  "subUid": 123456789,
  "tokenId": "TBSC_BNB"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|
|body|body|[GetDepositAddressRequest](#schemagetdepositaddressrequest)| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"userId":0,"tokenId":"string","address":"string","createdAt":0,"updatedAt":0,"tag":"string","subUserId":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|充值地址信息|[RSubWalletAddressOpenApiVO](#schemarsubwalletaddressopenapivo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[SubWalletAddressOpenApiVO](#schemasubwalletaddressopenapivo)|false|none||企业子用户钱包地址实体类<br /> 用于存储和管理子用户的钱包地址信息<br /> 对应数据库表：tb_sub_wallet_address|
|»» id|integer(int64)|false|none||主键ID|
|»» userId|integer(int64)|false|none||用户ID<br /> 主账号用户的标识符|
|»» tokenId|string|false|none||币种ID<br /> 例如：TBSC_BNB、TBSC_USDT等|
|»» address|string|false|none||钱包地址<br /> 区块链上的钱包地址，用于接收充值|
|»» createdAt|integer(int64)|false|none||创建时间<br /> 记录创建的时间戳（毫秒）|
|»» updatedAt|integer(int64)|false|none||更新时间<br /> 记录最后更新的时间戳（毫秒）|
|»» tag|string|false|none||标签<br /> 某些币种（如XRP、XLM等）需要的额外标识符|
|»» subUserId|integer(int64)|false|none||子用户ID<br /> 企业-子用户的ID，标识具体的子账号|

<a id="opIdcreateWithdrawal"></a>

## POST 根据UID发起提币
 为指定子用户创建提币订单

POST /t-api/toocans-broker-api/v1/op/openapi/createWithdrawal

根据UID发起提币
 为指定子用户创建提币订单

> Body 请求参数

```json
{
  "subUid": 123456789,
  "tokenId": "TBSC_BNB",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "amount": 0.01,
  "clientWithdrawalId": "client12345678901234"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|
|body|body|[CreateWithdrawalRequest](#schemacreatewithdrawalrequest)| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainTokenId":"string","userId":0,"subUserId":0,"address":"string","arriveQuantity":0,"status":0,"withdrawMethod":0,"createdAt":0,"updatedAt":0,"arriveTime":0,"totalQuantity":0,"txId":"string"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|提币订单ID|[RWithdrawalOrderOpenApiVO](#schemarwithdrawalorderopenapivo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)|false|none||none|
|»» id|integer(int64)|false|none||平台生成|
|»» clientWithdrawalId|string|false|none||客户端订单ID|
|»» transactionId|string|false|none||订单ID|
|»» tokenId|string|false|none||tokenId|
|»» chainTokenId|string|false|none||链tokenId|
|»» userId|integer(int64)|false|none||none|
|»» subUserId|integer(int64)|false|none||企业-子用户id|
|»» address|string|false|none||提现地址 用户的|
|»» arriveQuantity|number|false|none||实际提币数量|
|»» status|integer(int32)|false|none||1,提币待审核中，2，系统自动审核，3人工审核通过 4 人工审核拒绝，5,钱包处理中,6钱包提交失败（可以重新请求）7,8 (订单提交中)  10，提币成功， 11 提币失败 12  人工审核拒绝失败|
|»» withdrawMethod|integer(int32)|false|none||0，init   1:区块链交易,2:内部转账|
|»» createdAt|integer(int64)|false|none||创建时间|
|»» updatedAt|integer(int64)|false|none||更新时间|
|»» arriveTime|integer(int64)|false|none||提现到账时间|
|»» totalQuantity|number|false|none||用户输入的提现数量|
|»» txId|string|false|none||transaction hash|

<a id="opIdcreateSubUser"></a>

## POST 创建子用户UID
 为当前OpenAPI用户创建一个新的子用户

POST /t-api/toocans-broker-api/v1/op/openapi/createSubUser

创建子用户UID
 为当前OpenAPI用户创建一个新的子用户

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":0}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|用户ID|[RLong](#schemarlong)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|integer(int64)|false|none||none|

<a id="opIdwithdrawalOrderInfo"></a>

## GET 查询提币订单详情
 根据客户端提币订单ID查询提币订单详细信息

GET /t-api/toocans-broker-api/v1/op/openapi/withdrawalOrderInfo

查询提币订单详情
 根据客户端提币订单ID查询提币订单详细信息

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|clientWithdrawalId|query|string| 是 |客户端提币订单ID|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainTokenId":"string","userId":0,"subUserId":0,"address":"string","arriveQuantity":0,"status":0,"withdrawMethod":0,"createdAt":0,"updatedAt":0,"arriveTime":0,"totalQuantity":0,"txId":"string"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|提币订单详情|[RWithdrawalOrderOpenApiVO](#schemarwithdrawalorderopenapivo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)|false|none||none|
|»» id|integer(int64)|false|none||平台生成|
|»» clientWithdrawalId|string|false|none||客户端订单ID|
|»» transactionId|string|false|none||订单ID|
|»» tokenId|string|false|none||tokenId|
|»» chainTokenId|string|false|none||链tokenId|
|»» userId|integer(int64)|false|none||none|
|»» subUserId|integer(int64)|false|none||企业-子用户id|
|»» address|string|false|none||提现地址 用户的|
|»» arriveQuantity|number|false|none||实际提币数量|
|»» status|integer(int32)|false|none||1,提币待审核中，2，系统自动审核，3人工审核通过 4 人工审核拒绝，5,钱包处理中,6钱包提交失败（可以重新请求）7,8 (订单提交中)  10，提币成功， 11 提币失败 12  人工审核拒绝失败|
|»» withdrawMethod|integer(int32)|false|none||0，init   1:区块链交易,2:内部转账|
|»» createdAt|integer(int64)|false|none||创建时间|
|»» updatedAt|integer(int64)|false|none||更新时间|
|»» arriveTime|integer(int64)|false|none||提现到账时间|
|»» totalQuantity|number|false|none||用户输入的提现数量|
|»» txId|string|false|none||transaction hash|

<a id="opIdqueryBrokerAssets"></a>

## GET 根据tokenId查询该broker下所有资产
 查询当前OpenAPI用户下指定币种或所有币种的资产信息

GET /t-api/toocans-broker-api/v1/op/openapi/assets

根据tokenId查询该broker下所有资产
 查询当前OpenAPI用户下指定币种或所有币种的资产信息

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|tokenId|query|string| 否 |币种ID|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":0,"accountId":0,"tokenId":"string","userId":0,"total":0,"assetTotal":0,"availableAssetTotal":0,"marketPrice":0,"locked":0,"available":0,"indebted":0,"createdAt":0,"updatedAt":0,"accountType":0}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|broker资产列表|[RListBalanceOpenApiVO](#schemarlistbalanceopenapivo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[[BalanceOpenApiVO](#schemabalanceopenapivo)]|false|none||[现金资产表]|
|»» id|integer(int64)|false|none||none|
|»» accountId|integer(int64)|false|none||交易账号|
|»» tokenId|string|false|none||none|
|»» userId|integer(int64)|false|none||none|
|»» total|number|false|none||总资产|
|»» assetTotal|number|false|none||对应的usdt计价总资产|
|»» availableAssetTotal|number|false|none||对应的usdt计价可用总资产|
|»» marketPrice|number|false|none||实时价格|
|»» locked|number|false|none||已冻结资产。|
|»» available|number|false|none||可用资产|
|»» indebted|number|false|none||负债资产|
|»» createdAt|integer(int64)|false|none||创建时间|
|»» updatedAt|integer(int64)|false|none||更新时间|
|»» accountType|integer(int32)|false|none||1现货，2期权，3期货，4杠杆账户|

# 数据模型

<h2 id="tocS_RVoid">RVoid</h2>

<a id="schemarvoid"></a>
<a id="schema_RVoid"></a>
<a id="tocSrvoid"></a>
<a id="tocsrvoid"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {}
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|object|false|none||none|

<h2 id="tocS_RLong">RLong</h2>

<a id="schemarlong"></a>
<a id="schema_RLong"></a>
<a id="tocSrlong"></a>
<a id="tocsrlong"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": 0
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|integer(int64)|false|none||none|

<h2 id="tocS_QueryWithdrawalOrderRequest">QueryWithdrawalOrderRequest</h2>

<a id="schemaquerywithdrawalorderrequest"></a>
<a id="schema_QueryWithdrawalOrderRequest"></a>
<a id="tocSquerywithdrawalorderrequest"></a>
<a id="tocsquerywithdrawalorderrequest"></a>

```json
{
  "subUid": 123456789,
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "clientWithdrawalId": "client12345678",
  "tokenId": "TBSC_BNB",
  "txId": "0xabcd1234...",
  "startTime": 1626307200000,
  "endTime": 1626393600000,
  "pageNo": 1,
  "pageSize": 10
}

```

查询提币订单请求DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|subUid|integer(int64)|true|none||子用户ID|
|address|string|false|none||提币地址|
|clientWithdrawalId|string|false|none||客户端提币订单ID|
|tokenId|string|false|none||币种ID|
|txId|string|false|none||交易ID（区块链交易哈希）|
|startTime|integer(int64)|false|none||开始时间（毫秒时间戳）|
|endTime|integer(int64)|false|none||结束时间（毫秒时间戳）|
|pageNo|integer(int32)|false|none||页码，从1开始|
|pageSize|integer(int32)|false|none||每页大小，范围1-100|

<h2 id="tocS_CreateWithdrawalRequest">CreateWithdrawalRequest</h2>

<a id="schemacreatewithdrawalrequest"></a>
<a id="schema_CreateWithdrawalRequest"></a>
<a id="tocScreatewithdrawalrequest"></a>
<a id="tocscreatewithdrawalrequest"></a>

```json
{
  "subUid": 123456789,
  "tokenId": "TBSC_BNB",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "amount": 0.01,
  "clientWithdrawalId": "client12345678901234"
}

```

创建提币请求DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|subUid|integer(int64)|true|none||子用户ID|
|tokenId|string|true|none||币种ID|
|address|string|true|none||提币地址|
|amount|number|true|none||提币数量|
|clientWithdrawalId|string|true|none||客户端订单ID（用于幂等性控制）|

<h2 id="tocS_QueryDepositOrderRequest">QueryDepositOrderRequest</h2>

<a id="schemaquerydepositorderrequest"></a>
<a id="schema_QueryDepositOrderRequest"></a>
<a id="tocSquerydepositorderrequest"></a>
<a id="tocsquerydepositorderrequest"></a>

```json
{
  "subUid": 123456789,
  "walletAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "tokenId": "TBSC_BNB",
  "startTime": 1626307200000,
  "endTime": 1626393600000,
  "pageNo": 1,
  "pageSize": 10
}

```

查询充值订单请求DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|subUid|integer(int64)|true|none||子用户ID|
|walletAddress|string|false|none||钱包地址|
|tokenId|string|false|none||币种ID|
|startTime|integer(int64)|false|none||开始时间（毫秒时间戳）|
|endTime|integer(int64)|false|none||结束时间（毫秒时间戳）|
|pageNo|integer(int32)|false|none||页码，从1开始|
|pageSize|integer(int32)|false|none||每页大小，范围1-100|

<h2 id="tocS_GetDepositAddressRequest">GetDepositAddressRequest</h2>

<a id="schemagetdepositaddressrequest"></a>
<a id="schema_GetDepositAddressRequest"></a>
<a id="tocSgetdepositaddressrequest"></a>
<a id="tocsgetdepositaddressrequest"></a>

```json
{
  "subUid": 123456789,
  "tokenId": "TBSC_BNB"
}

```

获取子用户充值地址请求DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|subUid|integer(int64)|true|none||子用户ID|
|tokenId|string|true|none||币种ID|

<h2 id="tocS_WithdrawalOrderOpenApiVO">WithdrawalOrderOpenApiVO</h2>

<a id="schemawithdrawalorderopenapivo"></a>
<a id="schema_WithdrawalOrderOpenApiVO"></a>
<a id="tocSwithdrawalorderopenapivo"></a>
<a id="tocswithdrawalorderopenapivo"></a>

```json
{
  "id": 0,
  "clientWithdrawalId": "string",
  "transactionId": "string",
  "tokenId": "string",
  "chainTokenId": "string",
  "userId": 0,
  "subUserId": 0,
  "address": "string",
  "arriveQuantity": 0,
  "status": 0,
  "withdrawMethod": 0,
  "createdAt": 0,
  "updatedAt": 0,
  "arriveTime": 0,
  "totalQuantity": 0,
  "txId": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||平台生成|
|clientWithdrawalId|string|false|none||客户端订单ID|
|transactionId|string|false|none||订单ID|
|tokenId|string|false|none||tokenId|
|chainTokenId|string|false|none||链tokenId|
|userId|integer(int64)|false|none||none|
|subUserId|integer(int64)|false|none||企业-子用户id|
|address|string|false|none||提现地址 用户的|
|arriveQuantity|number|false|none||实际提币数量|
|status|integer(int32)|false|none||1,提币待审核中，2，系统自动审核，3人工审核通过 4 人工审核拒绝，5,钱包处理中,6钱包提交失败（可以重新请求）7,8 (订单提交中)  10，提币成功， 11 提币失败 12  人工审核拒绝失败|
|withdrawMethod|integer(int32)|false|none||0，init   1:区块链交易,2:内部转账|
|createdAt|integer(int64)|false|none||创建时间|
|updatedAt|integer(int64)|false|none||更新时间|
|arriveTime|integer(int64)|false|none||提现到账时间|
|totalQuantity|number|false|none||用户输入的提现数量|
|txId|string|false|none||transaction hash|

<h2 id="tocS_DepositOrderOpenApiVO">DepositOrderOpenApiVO</h2>

<a id="schemadepositorderopenapivo"></a>
<a id="schema_DepositOrderOpenApiVO"></a>
<a id="tocSdepositorderopenapivo"></a>
<a id="tocsdepositorderopenapivo"></a>

```json
{
  "id": 123456789,
  "tokenId": "TBSC_BNB",
  "chainTokenId": "BNB",
  "quantity": 0.01,
  "fromAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "walletAddress": "0xabcdef1234567890abcdef1234567890abcdef12",
  "txId": "0x9876543210abcdef9876543210abcdef98765432",
  "status": 1,
  "createdAt": 1626307200000,
  "updatedAt": 1626307260000,
  "userId": 123456,
  "subUserId": 789012
}

```

充值订单OpenAPI响应VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||充值订单ID|
|tokenId|string|false|none||币种ID|
|chainTokenId|string|false|none||链上币种ID|
|quantity|number|false|none||充值数量|
|fromAddress|string|false|none||来源地址|
|walletAddress|string|false|none||钱包地址|
|txId|string|false|none||交易ID（区块链交易哈希）|
|status|integer(int32)|false|none||充值状态： 0: 初始化 INIT; 1: 完成 已到账; 2: 处理中 充值中; 3: 失败|
|createdAt|integer(int64)|false|none||创建时间（毫秒时间戳）|
|updatedAt|integer(int64)|false|none||更新时间（毫秒时间戳）|
|userId|integer(int64)|false|none||用户ID（主账号）|
|subUserId|integer(int64)|false|none||子用户ID|

<h2 id="tocS_RWithdrawalOrderOpenApiVO">RWithdrawalOrderOpenApiVO</h2>

<a id="schemarwithdrawalorderopenapivo"></a>
<a id="schema_RWithdrawalOrderOpenApiVO"></a>
<a id="tocSrwithdrawalorderopenapivo"></a>
<a id="tocsrwithdrawalorderopenapivo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "id": 0,
    "clientWithdrawalId": "string",
    "transactionId": "string",
    "tokenId": "string",
    "chainTokenId": "string",
    "userId": 0,
    "subUserId": 0,
    "address": "string",
    "arriveQuantity": 0,
    "status": 0,
    "withdrawMethod": 0,
    "createdAt": 0,
    "updatedAt": 0,
    "arriveTime": 0,
    "totalQuantity": 0,
    "txId": "string"
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)|false|none||none|

<h2 id="tocS_BalanceOpenApiVO">BalanceOpenApiVO</h2>

<a id="schemabalanceopenapivo"></a>
<a id="schema_BalanceOpenApiVO"></a>
<a id="tocSbalanceopenapivo"></a>
<a id="tocsbalanceopenapivo"></a>

```json
{
  "id": 0,
  "accountId": 0,
  "tokenId": "string",
  "userId": 0,
  "total": 0,
  "assetTotal": 0,
  "availableAssetTotal": 0,
  "marketPrice": 0,
  "locked": 0,
  "available": 0,
  "indebted": 0,
  "createdAt": 0,
  "updatedAt": 0,
  "accountType": 0
}

```

现金资产表

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||none|
|accountId|integer(int64)|false|none||交易账号|
|tokenId|string|false|none||none|
|userId|integer(int64)|false|none||none|
|total|number|false|none||总资产|
|assetTotal|number|false|none||对应的usdt计价总资产|
|availableAssetTotal|number|false|none||对应的usdt计价可用总资产|
|marketPrice|number|false|none||实时价格|
|locked|number|false|none||已冻结资产。|
|available|number|false|none||可用资产|
|indebted|number|false|none||负债资产|
|createdAt|integer(int64)|false|none||创建时间|
|updatedAt|integer(int64)|false|none||更新时间|
|accountType|integer(int32)|false|none||1现货，2期权，3期货，4杠杆账户|

<h2 id="tocS_RListBalanceOpenApiVO">RListBalanceOpenApiVO</h2>

<a id="schemarlistbalanceopenapivo"></a>
<a id="schema_RListBalanceOpenApiVO"></a>
<a id="tocSrlistbalanceopenapivo"></a>
<a id="tocsrlistbalanceopenapivo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "id": 0,
      "accountId": 0,
      "tokenId": "string",
      "userId": 0,
      "total": 0,
      "assetTotal": 0,
      "availableAssetTotal": 0,
      "marketPrice": 0,
      "locked": 0,
      "available": 0,
      "indebted": 0,
      "createdAt": 0,
      "updatedAt": 0,
      "accountType": 0
    }
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|[[BalanceOpenApiVO](#schemabalanceopenapivo)]|false|none||[现金资产表]|

<h2 id="tocS_RPageWithdrawalOrderOpenApiVO">RPageWithdrawalOrderOpenApiVO</h2>

<a id="schemarpagewithdrawalorderopenapivo"></a>
<a id="schema_RPageWithdrawalOrderOpenApiVO"></a>
<a id="tocSrpagewithdrawalorderopenapivo"></a>
<a id="tocsrpagewithdrawalorderopenapivo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "id": 0,
      "clientWithdrawalId": "string",
      "transactionId": "string",
      "tokenId": "string",
      "chainTokenId": "string",
      "userId": 0,
      "subUserId": 0,
      "address": "string",
      "arriveQuantity": 0,
      "status": 0,
      "withdrawMethod": 0,
      "createdAt": 0,
      "updatedAt": 0,
      "arriveTime": 0,
      "totalQuantity": 0,
      "txId": "string"
    }
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|[[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)]|false|none||none|
|» stackTrace|string|false|none||none|
|» pageNum|integer(int32)|false|none||none|
|» pageSize|integer(int32)|false|none||none|
|» startRow|integer(int64)|false|none||none|
|» endRow|integer(int64)|false|none||none|
|» total|integer(int64)|false|none||none|
|» pages|integer(int32)|false|none||none|
|» count|boolean|false|none||none|
|» reasonable|boolean|false|none||none|
|» pageSizeZero|boolean|false|none||none|
|» countColumn|string|false|none||none|
|» orderBy|string|false|none||none|
|» orderByOnly|boolean|false|none||none|
|» boundSqlInterceptor|[BoundSqlInterceptor](#schemaboundsqlinterceptor)|false|none||none|
|» dialectClass|string|false|none||none|
|» keepOrderBy|boolean|false|none||none|
|» keepSubSelectOrderBy|boolean|false|none||none|
|» asyncCount|boolean|false|none||none|
|» result|[[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)]|false|none||none|
|» chain|[Chain](#schemachain)|false|none||none|
|» empty|boolean|false|none||none|
|» first|[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)|false|none||none|
|» last|[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)|false|none||none|

<h2 id="tocS_RPageDepositOrderOpenApiVO">RPageDepositOrderOpenApiVO</h2>

<a id="schemarpagedepositorderopenapivo"></a>
<a id="schema_RPageDepositOrderOpenApiVO"></a>
<a id="tocSrpagedepositorderopenapivo"></a>
<a id="tocsrpagedepositorderopenapivo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "id": 123456789,
      "tokenId": "TBSC_BNB",
      "chainTokenId": "BNB",
      "quantity": 0.01,
      "fromAddress": "0x1234567890abcdef1234567890abcdef12345678",
      "walletAddress": "0xabcdef1234567890abcdef1234567890abcdef12",
      "txId": "0x9876543210abcdef9876543210abcdef98765432",
      "status": 1,
      "createdAt": 1626307200000,
      "updatedAt": 1626307260000,
      "userId": 123456,
      "subUserId": 789012
    }
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|[[DepositOrderOpenApiVO](#schemadepositorderopenapivo)]|false|none||[充值订单OpenAPI响应VO]|
|» stackTrace|string|false|none||none|
|» pageNum|integer(int32)|false|none||none|
|» pageSize|integer(int32)|false|none||none|
|» startRow|integer(int64)|false|none||none|
|» endRow|integer(int64)|false|none||none|
|» total|integer(int64)|false|none||none|
|» pages|integer(int32)|false|none||none|
|» count|boolean|false|none||none|
|» reasonable|boolean|false|none||none|
|» pageSizeZero|boolean|false|none||none|
|» countColumn|string|false|none||none|
|» orderBy|string|false|none||none|
|» orderByOnly|boolean|false|none||none|
|» boundSqlInterceptor|[BoundSqlInterceptor](#schemaboundsqlinterceptor)|false|none||none|
|» dialectClass|string|false|none||none|
|» keepOrderBy|boolean|false|none||none|
|» keepSubSelectOrderBy|boolean|false|none||none|
|» asyncCount|boolean|false|none||none|
|» result|[[DepositOrderOpenApiVO](#schemadepositorderopenapivo)]|false|none||[充值订单OpenAPI响应VO]|
|» chain|[Chain](#schemachain)|false|none||none|
|» empty|boolean|false|none||none|
|» first|[DepositOrderOpenApiVO](#schemadepositorderopenapivo)|false|none||充值订单OpenAPI响应VO|
|» last|[DepositOrderOpenApiVO](#schemadepositorderopenapivo)|false|none||充值订单OpenAPI响应VO|

<h2 id="tocS_RSubWalletAddressOpenApiVO">RSubWalletAddressOpenApiVO</h2>

<a id="schemarsubwalletaddressopenapivo"></a>
<a id="schema_RSubWalletAddressOpenApiVO"></a>
<a id="tocSrsubwalletaddressopenapivo"></a>
<a id="tocsrsubwalletaddressopenapivo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "id": 0,
    "userId": 0,
    "tokenId": "string",
    "address": "string",
    "createdAt": 0,
    "updatedAt": 0,
    "tag": "string",
    "subUserId": 0
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|[SubWalletAddressOpenApiVO](#schemasubwalletaddressopenapivo)|false|none||企业子用户钱包地址实体类<br /> 用于存储和管理子用户的钱包地址信息<br /> 对应数据库表：tb_sub_wallet_address|

<h2 id="tocS_SubWalletAddressOpenApiVO">SubWalletAddressOpenApiVO</h2>

<a id="schemasubwalletaddressopenapivo"></a>
<a id="schema_SubWalletAddressOpenApiVO"></a>
<a id="tocSsubwalletaddressopenapivo"></a>
<a id="tocssubwalletaddressopenapivo"></a>

```json
{
  "id": 0,
  "userId": 0,
  "tokenId": "string",
  "address": "string",
  "createdAt": 0,
  "updatedAt": 0,
  "tag": "string",
  "subUserId": 0
}

```

企业子用户钱包地址实体类
 用于存储和管理子用户的钱包地址信息
 对应数据库表：tb_sub_wallet_address

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||主键ID|
|userId|integer(int64)|false|none||用户ID<br /> 主账号用户的标识符|
|tokenId|string|false|none||币种ID<br /> 例如：TBSC_BNB、TBSC_USDT等|
|address|string|false|none||钱包地址<br /> 区块链上的钱包地址，用于接收充值|
|createdAt|integer(int64)|false|none||创建时间<br /> 记录创建的时间戳（毫秒）|
|updatedAt|integer(int64)|false|none||更新时间<br /> 记录最后更新的时间戳（毫秒）|
|tag|string|false|none||标签<br /> 某些币种（如XRP、XLM等）需要的额外标识符|
|subUserId|integer(int64)|false|none||子用户ID<br /> 企业-子用户的ID，标识具体的子账号|

