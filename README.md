# Toocans Broker OpenAPI 文档

本文档提供了 Toocans Broker OpenAPI 接口的详细信息。这些 API 允许外部系统与 Toocans Broker 平台集成，用于管理子用户、资产、充值和提币。

---

## 基础信息

### 开放api访问域名列表：

| 环境       | 说明         | 域名地址                              |
|------------|--------------|----------------------------------------|
| 预发布环境 | 用于测试联调 | [`https://pre-api-broker.toocans.com`](https://pre-api-broker.toocans.com) |
| 生产环境   | 实际上线使用 | [`https://api-broker.toocans.com`](https://api-broker.toocans.com)       |

---
## 认证

所有 REST 私有请求都必须包含以下请求头：

- `TOOCANS-ACCESS-KEY`：字符串类型的 API Key
- `TOOCANS-ACCESS-SIGN`：使用 HMAC SHA256 哈希函数获得的哈希值，再使用 Base-64 编码（详见下方签名说明）
- `TOOCANS-ACCESS-TIMESTAMP`：发起请求的时间（UTC 时间，毫秒级时间戳）
- `TOOCANS-ACCESS-RECV-WINDOW`：单位是毫秒，默认值为 20000，用于指定请求在多长时间内有效，同时用于防止重放攻击，最大不超过 60000 毫秒

所有请求都应包含 `Content-Type: application/json` 头部，并确保请求体为有效的 JSON 格式（如适用）。

### 签名生成方法

`TOOCANS-ACCESS-SIGN` 的生成步骤如下：

1. 构造签名原文字符串：`timestamp + method + RECV-WINDOW + requestPath + body`
   - `timestamp`：与 `TOOCANS-ACCESS-TIMESTAMP` 请求头相同的毫秒级时间戳
   - `method`：请求方法，字母全部大写（GET/POST）
   - `RECV-WINDOW`：接收窗口值，与 `TOOCANS-ACCESS-RECV-WINDOW` 请求头相同
   - `requestPath`：请求接口路径，如 `/v1/op/openapi/assets`
   - `body`：请求主体的字符串，如果请求没有主体（通常为 GET 请求）则可省略

2. 使用 API Secret Key 对签名原文进行 HMAC SHA256 加密

3. 将加密结果进行 Base-64 编码，得到最终签名

**示例：**
```javascript
const timestamp = Date.now().toString();
const method = 'GET';
const recvWindow = '10000';
const requestPath = '/v1/op/openapi/assets?tokenId=TBSC_BNB';
const body = ''; // GET 请求通常没有请求体

const signatureString = timestamp + method + recvWindow + requestPath + body;
const signature = CryptoJS.enc.Base64.stringify(
  CryptoJS.HmacSHA256(signatureString, secretKey)
);
```

## 响应格式

所有 API 响应都遵循标准格式：

```json
{
  "code": 200,       // 状态码，200 表示成功
  "msg": "success",  // 消息
  "data": {}         // 响应数据，结构因端点而异
}
```
## 接口导航

- [注册用户](auth/register.md)
- [用户登录](auth/login.md)
- [获取当前用户信息](user/me.md)
- [数据模型](models.md)
