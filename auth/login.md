# 📌 POST /auth/login

**描述**：用户登录，获取认证 Token

## ✅ 请求体 `application/json`
```json
{
  "email": "user@example.com",
  "password": "your_password"
}
```

- `email` (string, required): 用户邮箱地址
- `password` (string, required): 用户密码

## 🔁 响应 `200 OK`
```json
{
  "token": "your_jwt_token"
}
```

- `token` (string): 登录成功返回的 JWT Token

## ⛔ 错误响应
- `401 Unauthorized`: 登录失败，用户名或密码错误
