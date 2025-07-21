# 📌 GET /user/me

**描述**：获取当前登录用户信息（需要认证）

## 🔐 鉴权
需要 Bearer Token 认证，格式：
```
Authorization: Bearer <your_jwt_token>
```

## 🔁 响应 `200 OK`
```json
{
  "id": "123456",
  "email": "user@example.com",
  "createdAt": "2025-07-14T12:00:00Z"
}
```

- `id` (string): 用户唯一标识
- `email` (string): 用户邮箱
- `createdAt` (string): 用户创建时间（ISO 8601）

## ⛔ 错误响应
- `401 Unauthorized`: 未登录或 token 失效
