# 基础数据

以下业务接口，都需要通过签名认证

---

## 1.GET 获取服务器时间戳

GET /t-api/openapi/v1/bc/baseConfig/serverTime

### 请求参数

无

> 返回示例

> 200 Response

```json
{"code":200,"msg":"success","data":"1733197200000"}
```

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|string|false|none||服务器时间戳（毫秒）|

---

<a id="get-all-networks"></a>

## 2.GET 获取所有的网络
获取所有支持的区块链网络列表

GET /t-api/openapi/v1/bc/baseConfig/allNetWork

### 请求参数

无

> 返回示例

> 200 Response

```json
{"code":200,"msg":"success","data":[{"chainId":"TRON","chainName":"TRON","chainIcon":"https://example.com/tron.png"},{"chainId":"BNB","chainName":"BNB Smart Chain","chainIcon":"https://example.com/bnb.png"}]}
```

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[[Chain]]|false|none||[区块链网络列表]|

### Chain 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|chainId|string|false|none||链ID|
|chainName|string|false|none||链名称|
|chainIcon|string|false|none||链图标URL|

---
<a id="get-token-config"></a>

### 3.GET 获取该币种下所有的token配置

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


