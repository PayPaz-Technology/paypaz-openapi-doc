# 基础数据

<a id="opIdserverTime"></a>

## GET 获取服务器时间撮

GET /t-api/toocans-broker-api/v1/bc/baseConfig/serverTime

获取服务器时间撮

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":"0"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RLong](#schemarlong)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Internal Server Error|Inline|

### 返回数据结构

状态码 **200**

*响应信息主体*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer(int32)|false|none||none|
|» msg|string|false|none||none|
|» data|string|false|none||none|

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
