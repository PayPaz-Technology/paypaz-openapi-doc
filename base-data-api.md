# 基础数据 - 不需要签名认证

<a id="opIdserverTime"></a>

## 1.GET 获取服务器时间戳

GET /t-api/openapi/v1/bc/baseConfig/serverTime

获取服务器时间戳

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|无|||||

> 返回示例

> 200 Response

```json
{"code":200,"msg":"success","data":"1733197200000"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|服务器时间戳（毫秒）|[RLong](#schemarlong)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|string|false|none||服务器时间戳（毫秒）|


<a id="opIdallNetWork"></a>

## 2.GET 获取所有的网络

GET /t-api/openapi/v1/bc/baseConfig/allNetWork

获取所有支持的区块链网络列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|无|||||

> 返回示例

> 200 Response

```json
{"code":200,"msg":"success","data":[{"chainId":"TRON","chainName":"TRON","chainIcon":"https://example.com/tron.png"},{"chainId":"BNB","chainName":"BNB Smart Chain","chainIcon":"https://example.com/bnb.png"}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|网络列表|[RListChain](#schemarlistchain)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|[[Chain](#schemachain)]|false|none||[区块链网络列表]|



# 数据模型

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

<h2 id="tocS_RListChain">RListChain</h2>

<a id="schemarlistchain"></a>
<a id="schema_RListChain"></a>
<a id="tocSrlistchain"></a>
<a id="tocsrlistchain"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "chainId": "string",
      "chainName": "string",
      "chainIcon": "string"
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
|data|[[Chain](#schemachain)]|false|none||[区块链网络]|

<h2 id="tocS_Chain">Chain</h2>

<a id="schemachain"></a>
<a id="schema_Chain"></a>
<a id="tocSchain"></a>
<a id="tocschain"></a>

```json
{
  "chainId": "string",
  "chainName": "string",
  "chainIcon": "string"
}

```

区块链网络

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|chainId|string|false|none||链ID|
|chainName|string|false|none||链名称|
|chainIcon|string|false|none||链图标URL|

