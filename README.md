# Paypaz OpenAPI 接入指南

本文档提供了 Paypaz  Broker OpenAPI 接口的详细信息。这些 API 允许外部系统与 Paypaz Broker 平台集成，用于管理子用户、支付订单，资产、充值和提币。

---

## 基础信息

### REST接口访问域名列表：

| 环境       | 说明         | 域名地址                                                        | 对外出口IP         |
|------------|--------------|-------------------------------------------------------------|--------------------|
| 预发布环境 | 用于测试联调 | [`https://open-api.paypaz.dev`](https://openapi.paypaz.dev) | 16.163.219.116     |
| 生产环境   | 实际上线使用 | [`https://open-api.paypaz.com`](https://openapi.paypaz.com) | 18.167.66.134                  |

---
## 关于鉴权的api密钥
Paypaz后台管理生成API的密钥对基于HMAC算法运作的，您将获得一对公钥和私钥，请务必妥善保管。
### 公共参数

所有需要签名认证的接口都需要包好以下http的请求头：

- `PAYPAZ-ACCESS-KEY`：字符串类型的 API Key
- `PAYPAZ-ACCESS-SIGN`：使用 HMAC SHA256 哈希函数获得的哈希值，再使用 Base-64 编码（详见下方签名说明）
- `PAYPAZ-ACCESS-TIMESTAMP`：发起请求的时间（UTC 时间，毫秒级时间戳）
- `PAYPAZ-ACCESS-RECV-WINDOW`：单位是毫秒，默认值为 20000，用于指定请求在多长时间内有效，同时用于防止重放攻击，最大不超过 60000 毫秒

所有请求都应包含 `Content-Type: application/json` 头部，并确保请求体为有效的 JSON 格式（如适用）。

### 签名生成方法

`PAYPAZ-ACCESS-SIGN` 的生成步骤如下：

1. 构造签名原文字符串：`timestamp + method + RECV-WINDOW + requestPath + body`
   - `timestamp`：与 `PAYPAZ-ACCESS-TIMESTAMP` 请求头相同的毫秒级时间戳
   - `method`：请求方法，字母全部大写（GET/POST）
   - `RECV-WINDOW`：接收窗口值，与 `PAYPAZ-ACCESS-RECV-WINDOW` 请求头相同
   - `requestPath`：请求接口路径，无需排序 如 `/t-api/broker-openapi/v1/op/openapi/withdrawalOrderInfo?clientWithdrawalId=d2d640dc-db20-43c3-967a-9aa3b5e55899`
   - `body`：请求主体的字符串，如果请求没有主体（通常为 GET 请求）则可省略，无需排序

2. 使用 API Secret Key 对签名原文进行 HMAC SHA256 加密

3. 将加密结果进行 Base-64 编码，得到最终签名

**签名PAYPAZ-ACCESS-SIGN,javascript示例GET：**
```javascript
const timestamp = Date.now().toString();
const method = 'GET';
const recvWindow = '10000';
const requestPath = '/t-api/openapi/v1/op/openapi/withdrawalOrderInfo?clientWithdrawalId=d2d640dc-db20-43c3-967a-9aa3b5e55899';
const body = ''; // GET 请求通常没有请求体，如果是post，json 的字符串

const signatureString = timestamp + method + recvWindow + requestPath + body;
const signature = CryptoJS.enc.Base64.stringify(
  CryptoJS.HmacSHA256(signatureString, secretKey)
);
```

**签名PAYPAZ-ACCESS-SIGN,java示例GET：**
```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;
private static String generateHmacSha256Signature(String data, String secret) throws Exception{

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

}
public static void main(String[] args) throws Exception {

   String timestamp = Long.toString(System.currentTimeMillis());
   String method = "GET";
   String recvWindow = "20000";
   String clientWithdrawalId = "WKxXXnkaD0luIGvnZVrglg7UALaYDPTLiQdEbYvUZjL9qI4ekEqW";
   String requestPath = "/t-api/openapi/v1/op/openapi/withdrawalOrderInfo?clientWithdrawalId=" + clientWithdrawalId;
   String bodyString = "";
   
   // Build signature string
   String signatureString = timestamp + method + recvWindow + requestPath + bodyString;
   System.out.println("signatureString: " + signatureString);
   
   // Assume secretKey is defined
   String secretKey = "your_secret_key_here"; // Replace with actual key
   System.out.println("secretKey: " + secretKey);
   
   // Generate signature
   String signature = generateHmacSha256Signature(signatureString, secretKey);
}
```



**签名PAYPAZ-ACCESS-SIGN,javascript示例POST：**
```javascript
const timestamp = Date.now().toString();
const method = 'POST';
const recvWindow = '10000';
const requestPath = '/t-api/openapi/v1/op/openapi/createWithdrawal';
const body = {
    "subUid": 123456789,
    "tokenId": "USDT",
    "chainId": "TRON",
    "address": "0x1234567890abcdef1234567890abcdef12345678",
    "amount": 0.01,
    "clientWithdrawalId": "client12345678901234"
};

// Convert body to JSON string for the signature
const bodyString = JSON.stringify(body);

const signatureString = timestamp + method + recvWindow + requestPath + bodyString;
const signature = CryptoJS.enc.Base64.stringify(
  CryptoJS.HmacSHA256(signatureString, secretKey)
);
```

**签名PAYPAZ-ACCESS-SIGN,java示例POST：**
```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
private static String generateHmacSha256Signature(String data, String secret) throws Exception{
   Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
   SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
   sha256_HMAC.init(secret_key);
    byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
   return Base64.getEncoder().encodeToString(hash);

}
public static void main(String[] args) throws Exception {

       String timestamp = Long.toString(System.currentTimeMillis());
        String method = "POST";
        String recvWindow = "10000";
        String requestPath = "/t-api/openapi/v1/op/openapi/createWithdrawal";
        
        // 直接构建JSON对象
        JSONObject body = new JSONObject();
        body.put("subUid", 123456789);
        body.put("tokenId", "USDT");
        body.put("chainId", "TRON");
        body.put("address", "0x1234567890abcdef1234567890abcdef12345678");
        body.put("amount", 0.01);
        body.put("clientWithdrawalId", "client12345678901234");
        
        // 获取JSON字符串
        String bodyString = body.toString();
        
        // 构建签名字符串
        String signatureString = timestamp + method + recvWindow + requestPath + bodyString;
        System.out.println("Signature String: " + signatureString);

        // 替换为你的实际密钥
        String secretKey = "your_secret_key_here"; 
        
        // 生成签名
        String signature = generateHmacSha256Signature(signatureString, secretKey);
    
}
```


## http请求示例
**示例GET：**
```javascript
GET /t-api/openapi/v1/op/openapi/withdrawalOrderInfo?clientWithdrawalId=d2d640dc-db20-43c3-967a-9aa3b5e5589 HTTP/1.1
Host: brokerapi.PAYPAZ.com
-H 'PAYPAZ-ACCESS-KEY: XXXXXXXXXX' \
-H 'PAYPAZ-ACCESS-SIGN: xxxxxxxxxxxxxxxxxx' \
-H 'PAYPAZ-ACCESS-TIMESTAMP: 1658384431891' \
-H 'PAYPAZ-ACCESS-RECV-WINDOW: 5000' \
-H 'Content-Type: application/json' 
```

**示例POST：**
```javascript
POST /t-api/openapi/v1/op/openapi/createWithdrawal HTTP/1.1
Host: brokerapi.paypaz.com
-H 'PAYPAZ-ACCESS-KEY: XXXXXXXXXX' \
-H 'PAYPAZ-ACCESS-SIGN: xxxxxxxxxxxxxxxxxx' \
-H 'PAYPAZ-ACCESS-TIMESTAMP: 1658384431891' \
-H 'PAYPAZ-ACCESS-RECV-WINDOW: 5000' \
-H 'Content-Type: application/json' \
-d '{
    "subUid": 123456789,
    "tokenId": "TBSC_BNB",
    "address": "0x1234567890abcdef1234567890abcdef12345678",
    "amount": 0.01,
    "clientWithdrawalId": "client12345678901234"
}'
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

### HTTP 状态码

| HTTP 状态码 | 描述 |
|------------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

当请求处理过程中发生错误时，响应中的 `code` 字段将包含特定的错误码，`msg` 字段将包含错误描述。

| 错误码 | 描述 |
|------|-------------|
| 429100000 | 请求频率超限 |
| 500105001 | 请求缺少必要的认证信息 |
| 500105002 | 无效的API Key |
| 500105003 | 签名验证失败 |
| 500105004 | 请求时间戳已过期 |
| 500105005 | 无效的时间戳格式 |
| 500105006 | 签名生成失败 |
| 500105007 | 子用户不存在 |
| 500105008 | 查询参数不能同时为空 |
| 500105009 | scope里权限项未配置正确 |
| 500105010 | 没有权限访问该API |
| 500105011 | IP不在白名单中 |
| 500105012 | 提币币种不存在 |
| 500105013 | 提币地址无效 |
| 500105014 | 提币币种配置不存在 |
| 500105015 | 不允许提币 |
| 500105016 | 提币金额低于最小值 |
| 500105017 | 用户不允许提币 |
| 500105018 | 余额不存在 |
| 500105019 | 余额不足 |
| 500105020 | 超过每日提币限额 |
| 500105021 | 提币金额过小 |
| 500105022 | 提币失败 |
| 500105023 | 提币地址不在白名单中 |
| 500105024 | openAPI系统错误 |
| 500105025 | 提币订单号已存在 |
| 500105026 | 充值币种不存在 |
| 500105027 | 充值币种配置不存在 |
| 500105028 | 不允许充值 |
| 500105029 | 创建余额记录失败 |
| 500105030 | 创建钱包地址失败 |
| 500105031 | 提币金额精度超过限制 |
| 500105033 | 支付订单号已存在 |
| 500105034 | 支付订单金额精度超过限制 |
| 500105035 | 支付金额不能小于最小充币金额 |

## 注意事项

1. 所有时间戳均为自 Unix 纪元以来的毫秒数。
2. 分页从 1 开始（第一页是 1）。
3. 最大页面大小为 100 项。
4. 对于提币操作，请确保 `clientWithdrawalId` 是唯一的，以保持幂等性。
5. 子用户操作需要验证子用户属于当前 API 用户。

## API 权限

API 中使用以下权限：
- `deposit`：充值和支付订单相关操作所需
- `withdraw`：提币相关操作所需

## API接口限流说明

以下接口实施了基于用户ID的限流策略：

| 接口路径 | 接口方法 | 限流时间窗口 | 最大请求次数 | 说明 |
|---------|---------|------------|--------|------|
| `/t-api/openapi/v1/op/openapi/depositAddress` | POST | 60秒 | 120次   | 获取或创建充值地址 |
| `/t-api/openapi/v1/op/openapi/createWithdrawal` | POST | 60秒 | 120次   | 创建提币订单 |
| `/t-api/openapi/v1/op/openapi/createPayInOrder` | POST | 60秒 | 120次   | 创建支付订单 |

**限流规则：**
- 限流类型：用户ID
- 时间窗口：60秒
- 最大请求次数：120次
- 超出限流后将返回限流错误响应

**注意事项：**
- 限流计数器按每个用户ID独立计算
- 建议客户端实现请求重试机制，并在收到限流错误时进行退避重试

