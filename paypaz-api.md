
<a id="opIdqueryWithdrawalOrders"></a>

以下业务接口，都需要通过签名认证

## 1.POST 根据客户子用户Uid，subUID、地址、订单号查询提币订单
 分页查询指定条件下的提币订单列表

POST /t-api/openapi/v1/op/openapi/withdrawalOrders

根据subUID、地址、订单号查询提币订单
 分页查询指定条件下的提币订单列表
### 特殊说明
 预发布环境tokenId: USDT
 预发布环境chainId: TRON
### 
> Body 请求参数

```json
{
	"subUid": "468001460675025856",
    "clientSubUserId": "csub_abc123",
	"tokenId": "USDT",
	"chainId": "TRON",
	"address": "0xa8c5eea944c3af945203e18cf990905519a158ad",
	"txId": "0xd3ef6ed39ee456de7c897ffa9c677aa64dcb560d37a8abbb1478fe82630ae1da",
	"clientWithdrawalId": "client12345678",
	"startTime": "1754641181883",
	"endTime": "1754641181899",
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
{"code":0,"msg":"string","data":[{"id":"string","clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainTokenId":"string","userId":"string","subUserId":"string","address":"string","arriveQuantity":"string","statusInfo":0,"createdAt":"string","updatedAt":"string","arriveTime":"string","totalQuantity":"string","txId":"string"}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|提币订单分页列表|[RPageWithdrawalOrderOpenApiVO](#schemarpagewithdrawalorderopenapivo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[[WithdrawalOrderOpenApiVO](#schemawithdrawalorderopenapivo)]|false|none||none|


<a id="opIdqueryDepositOrders"></a>

## 2.POST 根据客户子UID，地址，订单号查询充值订单
 分页查询指定条件下的充值订单列表

POST /t-api/openapi/v1/op/openapi/depositOrders

根据UID或地址查询充值订单
 分页查询指定条件下的充值订单列表
### tokenId 特殊说明
预发布环境tokenId: USDT
预发布环境chainId: TRON

> Body 请求参数

```json
{
  "subUid": 123456789,
  "clientSubUserId": "csub_abc123",
  "walletAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "orderNo": "D202501010001",
  "tokenId": "USDT",
  "chainId": "TRON",
  "startTime": 1626307200000,
  "endTime": 1626393600000,
  "pageNo": 1,
  "pageSize": 10,
  "validSubUserIdentifier": true
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[QueryDepositOrderRequest](#schemaquerydepositorderrequest)| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":123456789,"orderNo":"D202501010001","tokenId":"TBSC_BNB","chainTokenId":"BNB","quantity":0.01,"fee":0,"netAmount":0.01,"fromAddress":"0x1234567890abcdef1234567890abcdef12345678","walletAddress":"0xabcdef1234567890abcdef1234567890abcdef12","txId":"0x9876543210abcdef9876543210abcdef98765432","status":1,"createdAt":1626307200000,"updatedAt":1626307260000,"userId":123456,"subUserId":789012}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|充值订单分页列表|[RPageDepositOrderOpenApiVO](#schemarpagedepositorderopenapivo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[[DepositOrderOpenApiVO](#schemadepositorderopenapivo)]|false|none||[充值订单OpenAPI响应VO]|
|

<a id="opIdgetDepositAddress"></a>

## 3.POST 根据子用户UID和tokenID获取充值地址
 为指定子用户和币种获取充值地址，如果不存在则创建新地址

POST /t-api/openapi/v1/op/openapi/depositAddress

根据子用户UID和tokenID获取充值地址
 为指定子用户和币种获取充值地址，如果不存在则创建新地址
### tokenId 特殊说明
预发布环境tokenId: USDT
预发布环境chainId: TRON

> Body 请求参数

```json
{
  "clientSubUserId": "csub_abc123",
  "tokenId": "USDT",
  "chainId": "TRON"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[GetDepositAddressRequest](#schemagetdepositaddressrequest)| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":"string","userId":"string","tokenId":"string","address":"string","createdAt":"string","updatedAt":"string","tag":"string","subUserId":"string"}}
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
|» data|[SubWalletAddressOpenApiVO](#schemasubwalletaddressopenapivo)|false|none||企业子用户钱包地址实体类<br /> |


<a id="opIdcreateWithdrawal"></a>

## 4.POST 根据UID发起提币
 为指定子用户创建提币订单

POST /t-api/openapi/v1/op/openapi/createWithdrawal

根据UID发起提币
 为指定子用户创建提币订单，这个子用户的 2FA 要由调用方自己来验证。

### tokenId 特殊说明
预发布环境tokenId: USDT
预发布环境chainId: TRON

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

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[CreateWithdrawalRequest](#schemacreatewithdrawalrequest)| 否 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":"string","clientWithdrawalId":"string","transactionId":"string","tokenId":"string","chainTokenId":"string","userId":"string","subUserId":"string","address":"string","arriveQuantity":"string","statusInfo":0,"createdAt":"string","updatedAt":"string","arriveTime":"string","totalQuantity":"string","txId":"string"}}
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


<a id="opIdcreateSubUser"></a>

## 5.GET 查询提币订单详情
 根据客户端提币订单ID查询提币订单详细信息

GET /t-api/openapi/v1/op/openapi/withdrawalOrderInfo

查询提币订单详情
 根据客户端提币订单ID查询提币订单详细信息

### tokenId 特殊说明
预发布环境tokenId: USDT
预发布环境chainId: TRON

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|clientWithdrawalId|query|string| 是 |客户端提币订单ID|

> 返回示例

> 200 Response

```
{"code":200,"msg":"success","data":{"id":"1954903600726814720","clientWithdrawalId":"WKxXXnkaD0luIGvnZVrglg7UALaYDPTLiQdEbYvUZjL9qI4ekEqW","transactionId":null,"tokenId":"TBSC_BNB","chainTokenId":"TBSC_BNB","userId":"468001460","subUserId":"468001460825249908","address":"0xa8c5eea944c3af945203e18cf990905519a158ad","arriveQuantity":"0.000000099","statusInfo":1,"createdAt":"1754920313415","updatedAt":"1754920313415","arriveTime":null,"totalQuantity":"0.0000001","txId":null}}
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


<a id="opIdqueryBrokerAssets"></a>

## 6.GET 根据tokenId查询该broker下所有资产
 查询当前OpenAPI用户下指定币种或所有币种的资产信息

GET /t-api/openapi/v1/op/openapi/assets

根据tokenId查询该broker下所有资产
 查询当前OpenAPI用户下指定币种或所有币种的资产信息
### tokenId 特殊说明
预发布环境tokenId: USDT
预发布环境chainId: TRON
### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|tokenId|query|string| 否 |币种ID|

> 返回示例

> 200 Response

```
{"code":200,"msg":"success","data":[{"id":"1953034545579294720","accountId":"1951221207996780544","tokenId":"USDT","userId":"468001460","total":"0.070013132010012112","assetTotal":"0.070013132010012112","availableAssetTotal":"0.070013132010012112","marketPrice":"0","locked":"0","available":"0.070013132010012112","indebted":"0","createdAt":"1754474695950","updatedAt":"1754559221966","accountType":1},{"id":"1953010034274480128","accountId":"1951221207996780544","tokenId":"BNB","userId":"468001460","total":"1","assetTotal":"799.31","availableAssetTotal":"798.51069","marketPrice":"799.31","locked":"0.001","available":"0.999","indebted":"0","createdAt":"1754468851999","updatedAt":"1754559160300","accountType":1},{"id":"1952934600546816000","accountId":"1951221207996780544","tokenId":"TBSC_BNB","userId":"468001460","total":"0.272193990000000101","assetTotal":"0.272193990000000101","availableAssetTotal":"0.272098890000000101","marketPrice":"0","locked":"0.0000951","available":"0.272098890000000101","indebted":"0","createdAt":"1754450867197","updatedAt":"1754920359479","accountType":1}]}
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


<a id="opIdallToken"></a>

## 7.GET 获取该币种下所有的token配置
 获取指定币种在不同链上的配置信息

GET /t-api/openapi/v1/op/openapi/allToken

获取该币种下所有的token配置
 获取指定币种在不同链上的配置信息，包括充提币限额、手续费等

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|tokenId|query|string| 是 |币种ID，例如：USDT|
|chainId|query|string| 否 |链ID，例如：TRON|

> 返回示例

> 200 Response

```json
{"code":200,"msg":"success","data":[{"id":"1","tokenId":"USDT","tokenName":"USDT","tokenFullName":"Tether USD","chainId":"TRON","createdAt":"1626307200000","updatedAt":"1626307200000","icon":"https://example.com/usdt.png","protocolName":"TRC20","chainName":"TRON","chainIcon":"https://example.com/tron.png","tokenChainId":"TRON_USDT","status":1,"allowDeposit":1,"allowWithdraw":1,"depositMinQuantity":"1","withdrawMinQuantity":"10","withdrawMaxDayQuantity":"100000","depositCharges":"0.001","depositChargesMinAmount":"0.1","withdrawalChargesMinAmount":"1","withdrawChargeValue":"0.01"}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|token配置列表|[RListToken](#schemarlisttoken)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[[Token](#schematoken)]|false|none||[token基本配置表]|


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
  "data": "0"
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|string|false|none||none|

<h2 id="tocS_QueryWithdrawalOrderRequest">QueryWithdrawalOrderRequest</h2>

<a id="schemaquerywithdrawalorderrequest"></a>
<a id="schema_QueryWithdrawalOrderRequest"></a>
<a id="tocSquerywithdrawalorderrequest"></a>
<a id="tocsquerywithdrawalorderrequest"></a>

```json
{
  "subUid": "123456789",
  "clientSubUserId": "csub_abc123",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "clientWithdrawalId": "client12345678",
  "tokenId": "TBSC_BNB",
  "txId": "0xabcd1234...",
  "startTime": "1626307200000",
  "endTime": "1626393600000",
  "pageNo": 1,
  "pageSize": 10
}

```

查询提币订单请求DTO

### 属性

|名称|类型|必选|约束|中文名| 说明            |
|---|---|---|---|---|---------------|
|subUid|string|true|none|| 子用户ID         |
|clientSubUserId|string|true|none|| 客户子用户ID       |
|address|string|false|none|| 提币地址          |
|clientWithdrawalId|string|false|none|| 客户端提币订单ID     |
|tokenId|string|false|none|| 币种ID          |
|chainId|string|false|none|| 链ID           |
|txId|string|false|none|| 交易ID（区块链交易ID） |
|startTime|string|false|none|| 开始时间（毫秒时间戳）   |
|endTime|string|false|none|| 结束时间（毫秒时间戳）   |
|pageNo|integer(int32)|false|none|| 页码，从1开始       |
|pageSize|integer(int32)|false|none|| 每页大小，范围1-100  |

<h2 id="tocS_CreateWithdrawalRequest">CreateWithdrawalRequest</h2>

<a id="schemacreatewithdrawalrequest"></a>
<a id="schema_CreateWithdrawalRequest"></a>
<a id="tocScreatewithdrawalrequest"></a>
<a id="tocscreatewithdrawalrequest"></a>

```json
{
  "clientSubUserId": "csub_abc123",
  "tokenId": "TBSC_BNB",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "amount": 0.01,
  "clientWithdrawalId": "client12345678901234"
}

```

创建提币请求DTO

### 属性

|名称|类型|必选|约束|中文名| 说明               |
|---|---|---|---|---|------------------|
|clientSubUserId|string|true|none|| 客户子用户ID          |
|tokenId|string|true|none|| 币种ID             |
|chainId|string|true|none|| 链ID              |
|address|string|true|none|| 提币地址             |
|amount|number|true|none|| 提币数量             |
|twoFactorAuthentication|boolean|true|none|| 2fa标识            |
|clientWithdrawalId|string|true|none|| 客户端订单ID（用于幂等性控制） |

<h2 id="tocS_QueryDepositOrderRequest">QueryDepositOrderRequest</h2>

<a id="schemaquerydepositorderrequest"></a>
<a id="schema_QueryDepositOrderRequest"></a>
<a id="tocSquerydepositorderrequest"></a>
<a id="tocsquerydepositorderrequest"></a>

```json
{
  "clientSubUserId": "123456789",
  "walletAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "tokenId": "TBSC_BNB",
  "startTime": "1626307200000",
  "endTime": "1626393600000",
  "pageNo": 1,
  "pageSize": 10
}

```

查询充值订单请求DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|subUid|string|true|none||子用户ID|
|clientSubUserId|string|true|none||子用户ID|
|walletAddress|string|false|none||钱包地址|
|orderNo|string|false|none||订单号|
|tokenId|string|false|none||币种ID|
|chainId|string|false|none||链ID|
|startTime|string|false|none||开始时间（毫秒时间戳）|
|endTime|string|false|none||结束时间（毫秒时间戳）|
|pageNo|integer(int32)|false|none||页码，从1开始|
|pageSize|integer(int32)|false|none||每页大小，范围1-100|

<h2 id="tocS_GetDepositAddressRequest">GetDepositAddressRequest</h2>

<a id="schemagetdepositaddressrequest"></a>
<a id="schema_GetDepositAddressRequest"></a>
<a id="tocSgetdepositaddressrequest"></a>
<a id="tocsgetdepositaddressrequest"></a>

```json
{
  "subUid": "123456789",
  "tokenId": "TBSC_BNB"
}

```

获取子用户充值地址请求DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|clientSubUserId|string|true|none||子用户ID|
|tokenId|string|true|none||币种ID|
|chainId|string|true|none||链ID|

<h2 id="tocS_WithdrawalOrderOpenApiVO">WithdrawalOrderOpenApiVO</h2>

<a id="schemawithdrawalorderopenapivo"></a>
<a id="schema_WithdrawalOrderOpenApiVO"></a>
<a id="tocSwithdrawalorderopenapivo"></a>
<a id="tocswithdrawalorderopenapivo"></a>

```json
{
  "id": "0",
  "clientWithdrawalId": "string",
  "transactionId": "string",
  "tokenId": "string",
  "chainTokenId": "string",
  "userId": "0",
  "subUserId": "0",
  "address": "string",
  "arriveQuantity": 0,
  "statusInfo": 0,
  "createdAt": "0",
  "updatedAt": "0",
  "arriveTime": "0",
  "totalQuantity": 0,
  "txId": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名| 说明                   |
|---|---|---|---|---|----------------------|
|id|string|false|none|| 平台生成                 |
|clientWithdrawalId|string|false|none|| 客户端订单ID              |
|transactionId|string|false|none|| 订单ID                 |
|tokenId|string|false|none|| tokenId              |
|chainId|string|false|none|| 链ID                  |
|userId|string|false|none|| none                 |
|subUserId|string|false|none|| 企业-子用户id             |
|address|string|false|none|| 提现地址 用户的             |
|arriveQuantity|number|false|none|| 实际提币数量               |
|platformFee|number|false|none|| 手续费                  |
|statusInfo|integer(int32)|false|none|| 1,提币处理中，2，提币成功，3提币失败 |
|createdAt|string|false|none|| 创建时间                 |
|updatedAt|string|false|none|| 更新时间                 |
|arriveTime|string|false|none|| 提现到账时间               |
|totalQuantity|number|false|none|| 用户输入的提现数量            |
|txId|string|false|none|| transaction hash     |

<h2 id="tocS_DepositOrderOpenApiVO">DepositOrderOpenApiVO</h2>

<a id="schemadepositorderopenapivo"></a>
<a id="schema_DepositOrderOpenApiVO"></a>
<a id="tocSdepositorderopenapivo"></a>
<a id="tocsdepositorderopenapivo"></a>

```json
{
  "id": "123456789",
  "orderNo": "d9df7282-7ab5-4143-98aa-3d10762ab5ef",
  "tokenId": "TBSC_BNB",
  "chainTokenId": "BNB",
  "quantity": 0.01,
  "fee": "0.01",
  "netAmount": "0.0",
  "fromAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "walletAddress": "0xabcdef1234567890abcdef1234567890abcdef12",
  "txId": "0x9876543210abcdef9876543210abcdef98765432",
  "status": 1,
  "createdAt": "1626307200000",
  "updatedAt": "1626307260000",
  "userId": "123456",
  "subUserId": "789012"
}

```

充值订单OpenAPI响应VO

### 属性

|名称|类型|必选|约束|中文名| 说明                                              |
|---|---|---|---|---|-------------------------------------------------|
|id|string|false|none|| 充值订单ID                                          |
|orderNo|string|false|none|| 充值订单号                                           |
|tokenId|string|false|none|| 币种ID                                            |
|chainId|string|false|none|| 链ID                                             |
|quantity|number|false|none|| 充值数量                                            |
|fee|number|false|none|| 手续费                                             |
|netAmount|number|false|none|| 到账金额                                            |
|fromAddress|string|false|none|| 来源地址                                            |
|walletAddress|string|false|none|| 钱包地址                                            |
|txId|string|false|none|| 交易ID（区块链交易ID）                                   |
|status|integer(int32)|false|none|| 充值状态： 0: 初始化 INIT; 1: 完成 已到账; 2: 处理中 充值中; 3: 失败 |
|createdAt|string|false|none|| 创建时间（毫秒时间戳）                                     |
|updatedAt|string|false|none|| 更新时间（毫秒时间戳）                                     |
|userId|string|false|none|| 用户ID（主账号）                                       |
|subUserId|string|false|none|| 子用户ID                                           |

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
    "id": "0",
    "clientWithdrawalId": "string",
    "transactionId": "string",
    "tokenId": "string",
    "chainTokenId": "string",
    "userId": "0",
    "subUserId": "0",
    "address": "string",
    "arriveQuantity": 0,
    "platformFee": "0",
    "statusInfo": 0,
    "createdAt": "0",
    "updatedAt": "0",
    "arriveTime": "0",
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
  "id": "0",
  "accountId": "0",
  "tokenId": "string",
  "userId": "0",
  "total": 0,
  "assetTotal": 0,
  "availableAssetTotal": 0,
  "marketPrice": 0,
  "locked": 0,
  "available": 0,
  "indebted": 0,
  "createdAt": "0",
  "updatedAt": "0",
  "accountType": 0
}

```

现金资产表

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|false|none||none|
|accountId|string|false|none||交易账号|
|tokenId|string|false|none||none|
|userId|string|false|none||none|
|total|number|false|none||持有该token的数量|
|assetTotal|number|false|none||以usdt计价，token的价值|
|availableAssetTotal|number|false|none||以usdt计价，可用token的价值|
|marketPrice|number|false|none||实时价格|
|locked|number|false|none||已冻结的token的数量。|
|available|number|false|none||可用的该token的数量|
|indebted|number|false|none||负债资产|
|createdAt|string|false|none||创建时间|
|updatedAt|string|false|none||更新时间|
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
      "id": "0",
      "accountId": "0",
      "tokenId": "string",
      "userId": "0",
      "total": 0,
      "assetTotal": 0,
      "availableAssetTotal": 0,
      "marketPrice": 0,
      "locked": 0,
      "available": 0,
      "indebted": 0,
      "createdAt": "0",
      "updatedAt": "0",
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
      "id": "0",
      "clientWithdrawalId": "string",
      "transactionId": "string",
      "tokenId": "string",
      "chainTokenId": "string",
      "userId": "0",
      "subUserId": "0",
      "address": "string",
      "arriveQuantity": 0,
      "statusInfo": 0,
      "createdAt": "0",
      "updatedAt": "0",
      "arriveTime": "0",
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
      "id": "123456789",
      "tokenId": "TBSC_BNB",
      "chainTokenId": "BNB",
      "quantity": 0.01,
      "platformFee": "0.0",
      "fromAddress": "0x1234567890abcdef1234567890abcdef12345678",
      "walletAddress": "0xabcdef1234567890abcdef1234567890abcdef12",
      "txId": "0x9876543210abcdef9876543210abcdef98765432",
      "statusInfo": 1,
      "createdAt": "1626307200000",
      "updatedAt": "1626307260000",
      "userId": "123456",
      "subUserId": "789012"
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
    "id": "0",
    "userId": "0",
    "tokenId": "string",
    "address": "string",
    "createdAt": "0",
    "updatedAt": "0",
    "tag": "string",
    "subUserId": "0"
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||none|
|msg|string|false|none||none|
|data|[SubWalletAddressOpenApiVO](#schemasubwalletaddressopenapivo)|false|none||企业子用户钱包地址实体类<br /> 用于存储和管理子用户的钱包地址信息<br /> |

<h2 id="tocS_SubWalletAddressOpenApiVO">SubWalletAddressOpenApiVO</h2>

<a id="schemasubwalletaddressopenapivo"></a>
<a id="schema_SubWalletAddressOpenApiVO"></a>
<a id="tocSsubwalletaddressopenapivo"></a>
<a id="tocssubwalletaddressopenapivo"></a>

```json
{
  "id": "0",
  "userId": "0",
  "tokenId": "string",
  "address": "string",
  "createdAt": "0",
  "updatedAt": "0",
  "tag": "string",
  "subUserId": "0"
}

```

企业子用户钱包地址实体类
 用于存储和管理子用户的钱包地址信息


### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|false|none||主键ID|
|userId|string|false|none||用户ID<br /> 主账号用户的标识符|
|tokenId|string|false|none||币种ID<br /> 例如：USDT、USDC等|
|chainId|string|false|none||链ID<br /> 例如：TRON、BNB等|
|address|string|false|none||钱包地址<br /> 区块链上的钱包地址，用于接收充值|
|createdAt|string|false|none||创建时间<br /> 记录创建的时间戳（毫秒）|
|updatedAt|string|false|none||更新时间<br /> 记录最后更新的时间戳（毫秒）|
|tag|string|false|none||标签<br /> 某些币种（如XRP、XLM等）需要的额外标识符|
|subUserId|string|false|none||子用户ID<br /> 企业-子用户的ID，标识具体的子账号|

<h2 id="tocS_RListToken">RListToken</h2>

<a id="schemarlisttoken"></a>
<a id="schema_RListToken"></a>
<a id="tocSrlisttoken"></a>
<a id="tocsrlisttoken"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "id": "0",
      "tokenId": "string",
      "tokenName": "string",
      "tokenFullName": "string",
      "chainId": "string",
      "createdAt": "0",
      "updatedAt": "0",
      "icon": "string",
      "protocolName": "string",
      "chainName": "string",
      "chainIcon": "string",
      "tokenChainId": "string",
      "status": 0,
      "allowDeposit": 0,
      "allowWithdraw": 0,
      "depositMinQuantity": "string",
      "withdrawMinQuantity": "string",
      "withdrawMaxDayQuantity": "string",
      "depositCharges": "string",
      "depositChargesMinAmount": "string",
      "withdrawalChargesMinAmount": "string",
      "withdrawChargeValue": "string"
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
|data|[[Token](#schematoken)]|false|none||[token基本配置表]|

<h2 id="tocS_Token">Token</h2>

<a id="schematoken"></a>
<a id="schema_Token"></a>
<a id="tocStoken"></a>
<a id="tocstoken"></a>

```json
{
  "id": "0",
  "tokenId": "string",
  "tokenName": "string",
  "tokenFullName": "string",
  "chainId": "string",
  "createdAt": "0",
  "updatedAt": "0",
  "icon": "string",
  "protocolName": "string",
  "chainName": "string",
  "chainIcon": "string",
  "tokenChainId": "string",
  "status": 0,
  "allowDeposit": 0,
  "allowWithdraw": 0,
  "depositMinQuantity": "string",
  "withdrawMinQuantity": "string",
  "withdrawMaxDayQuantity": "string",
  "depositCharges": "string",
  "depositChargesMinAmount": "string",
  "withdrawalChargesMinAmount": "string",
  "withdrawChargeValue": "string"
}

```

token基本配置表

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|false|none||主键ID|
|tokenId|string|false|none||币种ID，例如："USDT"|
|tokenName|string|false|none||显示名称，可重命名|
|tokenFullName|string|false|none||完整名称|
|chainId|string|false|none||链ID，例如："TRON"|
|createdAt|string|false|none||创建时间|
|updatedAt|string|false|none||更新时间|
|icon|string|false|none||token图标URL|
|protocolName|string|false|none||链协议名称|
|chainName|string|false|none||链名称|
|chainIcon|string|false|none||链图标URL|
|tokenChainId|string|false|none||token链ID组合|
|status|integer(int32)|false|none||状态|
|allowDeposit|integer(int32)|false|none||1允许充币，0禁止|
|allowWithdraw|integer(int32)|false|none||1允许提现，0禁止|
|depositMinQuantity|string|false|none||充币最小额度。小于这个额度，不入账|
|withdrawMinQuantity|string|false|none||提币的最小额度|
|withdrawMaxDayQuantity|string|false|none||该币种提币的最大额度|
|depositCharges|string|false|none||充币手续费百分比|
|depositChargesMinAmount|string|false|none||充币的最小手续费|
|withdrawalChargesMinAmount|string|false|none||提币的最小手续费|
|withdrawChargeValue|string|false|none||提币百分比手续费|
