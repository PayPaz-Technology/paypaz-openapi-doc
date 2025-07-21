# 🔧 数据模型 Schemas

## 🧩 RegisterRequest
```json
{
  "email": "string (email)",
  "password": "string (minLength: 6)"
}
```

## 🧩 LoginRequest
```json
{
  "email": "string",
  "password": "string"
}
```

## 🧩 AuthResponse
```json
{
  "token": "string"
}
```

## 🧩 User
```json
{
  "id": "string",
  "email": "string",
  "createdAt": "string (date-time)"
}
```
