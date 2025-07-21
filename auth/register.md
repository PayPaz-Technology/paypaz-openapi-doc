# 📌 POST /auth/register

**描述**：注册新用户

## ✅ 请求体 `application/json`
```json
{
  "email": "user@example.com",
  "password": "your_password"
}
```

- `email` (string, required): 用户邮箱地址（格式：email）
- `password` (string, required): 用户密码（最少6位）

## 🔁 响应
- `201 Created`: 注册成功
- `400 Bad Request`: 参数错误或格式不合法
