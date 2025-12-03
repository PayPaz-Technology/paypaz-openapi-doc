# PayPaz Webhook 集成指南

## 1. 概述

PayPaz Webhook 系统允许您接收实时通知，了解与您的账户相关的事件，如交易成功、提现状态更新等。本指南将帮助您配置和集成 PayPaz Webhook。

## 2. 支持的 Webhook 事件

系统支持以下事件类型：

- `TRANSACTION_DEPOSIT_SUCCEEDED`: 充值成功
- `TRANSACTION_WITHDRAWAL_SUCCEEDED`: 提现成功
- `TRANSACTION_WITHDRAWAL_FAILED`: 提现失败

## 3. Webhook 配置

### 3.1 Webhook 配置参数

在配置 Webhook 时，需要提供以下信息：

- **URL**: 接收 Webhook 通知的 HTTPS 端点
- **事件类型**: 选择要订阅的事件类型
- **状态**: 启用/禁用 Webhook

### 3.2 安全配置

每个 Webhook 配置可以关联一个密钥对，用于验证请求的合法性：

- **公钥 (PubSecretKey)**: 用于验证签名的密钥
- **私钥 (PrivSecretKey)**: 用于生成签名的密钥（仅在 PayPaz 系统中使用）

## 4. Webhook 请求格式

### 4.1 请求头

每个 Webhook 请求将包含以下头部信息：

- `Content-Type: application/json`
- `PAYPAZ-WEBHOOK-SIGN`: HMAC-SHA256 签名
- `PAYPAZ-WEBHOOK-TIMESTAMP`: 时间戳（毫秒）

### 4.2 签名验证

签名生成规则：
1. 将请求体(payload)和时间戳(timestamp)用竖线(|)连接：`payload|timestamp`
2. 使用 HMAC-SHA256 算法和您的私钥对上述字符串进行签名
3. 对签名结果进行 Base64 编码

验证签名示例代码（Java）：
```java
private boolean verifySignature(String payload, String timestamp, String signature, String secret) {
    try {
        String message = payload + "|" + timestamp;
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKey);
        byte[] hash = sha256HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
        String computedSignature = Base64.getEncoder().encodeToString(hash);
        return computedSignature.equals(signature);
    } catch (Exception e) {
        return false;
    }
}
```

### 4.3 请求体示例

#### 充值成功事件
```json
{
  "eventType": "transaction.deposit.succeeded",
  "data": {
    "id": "1972615389021605888",
    "fee": "0.00019999",
    "txId": "L5faafcf23774bbe5f0603c93679b545",
    "status": 1,
    "userId": "449267154",
    "orderNo": "6af6340e-553b-428e-9d3b-16398aab5cf0",
    "tokenId": "USDC",
    "quantity": "0.019999",
    "createdAt": "1759143132812",
    "netAmount": "0.01979901",
    "subUserId": "449267154253404897",
    "updatedAt": null,
    "fromAddress": "",
    "chainId": "BNB",
    "walletAddress": "0xc7b530889238fbba211683bb5632b56d60c51270"
  }
}
```

#### 提现成功事件
```json
{
  "eventType": "transaction.withdrawal.succeeded",
  "data": {
    "id": "1980540667704803328",
    "txId": "Lcb5fd92ed2c7427acfe4b22596d9935",
    "userId": "449267154",
    "address": "0x2c90a96735d851c6728fb6949264b88198b5dc6c",
    "tokenId": "USDC",
    "createdAt": "1761032666380",
    "subUserId": "449267154692704658",
    "updatedAt": "1761032677792",
    "arriveTime": "1761032677796",
    "statusInfo": 2,
    "chainId": "BNB",
    "totalQuantity": "0.0001",
    "transactionId": "c17976f5-e865-4924-8ed8-c47f089f54ee",
    "arriveQuantity": "0.000099",
    "clientWithdrawalId": "1212121212122222222"
  }
}
```

## 5. 重试机制

### 5.1 重试策略

- 系统会在发送失败后自动重试，最多重试 7 次
- 重试间隔采用指数退避策略
- 每次重试后，`retryCount` 会递增

### 5.2 响应要求

您的服务器应返回以下 HTTP 状态码：
- `200` 或 `201`: 表示成功接收并处理了 Webhook
- 其他状态码: 表示处理失败，系统将根据重试策略进行重试

## 6. 最佳实践

1. **验证签名**: 始终验证请求签名以确保请求来自 PayPaz
2. **幂等性处理**: 实现幂等性处理逻辑，避免重复处理相同的事件
3. **超时设置**: 建议在 5 秒内响应 Webhook 请求
4. **日志记录**: 记录所有接收到的 Webhook 请求和处理结果
5. **错误处理**: 实现适当的错误处理逻辑，确保系统稳定性

## 7. 测试与调试

1. 使用测试环境进行集成测试
2. 检查 Webhook 事件日志以了解发送状态
3. 监控重试次数和失败事件

## 8. 故障排除

### 常见问题

1. **签名验证失败**
    - 检查密钥是否正确配置
    - 验证签名算法实现是否正确

2. **Webhook 未收到**
    - 检查网络连接和防火墙设置
    - 验证 Webhook URL 是否正确且可访问

3. **处理超时**
    - 确保您的服务能在 5 秒内响应
    - 考虑异步处理耗时操作

## 9. 联系支持

如遇问题，请联系 PayPaz 支持团队并提供以下信息：
- 事件ID (eventLogId)
- 时间戳
- 相关错误信息
- 您的服务器日志（如适用）
