# 基础数据 - 不需要签名认证

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

