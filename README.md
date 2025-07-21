# toocans-broker-api-docs

本 API 旨在提供给我们的代理商进行链上资产对接的能力

---

## 基础信息

### 开放api访问域名列表：

| 环境       | 说明         | 域名地址                              |
|------------|--------------|----------------------------------------|
| 预发布环境 | 用于测试联调 | [`https://pre-api-broker.toocans.com`](https://pre-api-broker.toocans.com) |
| 生产环境   | 实际上线使用 | [`https://api-broker.toocans.com`](https://api-broker.toocans.com)       |

---
## 全局请求参数说明：

**以下参数在全部接口中通用，除非特殊说明。**

| 参数名         | 类型     | 必填 | 示例值                   | 说明                                                   |
|----------------|----------|------|--------------------------|--------------------------------------------------------|
| `timestamp`    | `int64`  | 是   | `1721182023000`          | 毫秒时间戳，客户端发起请求的时间，用于防止重放攻击      |
| `sign`         | `string` | 是   | `a93b0c5dd7f4ef...`       | 签名字符串，用于请求校验（见下方签名算法说明）         |
| `accessKey`    | `string` | 是   | `your-access-key`        | 分配给用户的身份标识                                    |
| `nonce`        | `string` | 否   | `d8e8fca2dc0f896fd7...`   | 随机字符串，建议每次请求都不同，提高安全性              |
| `lang`         | `string` | 否   | `zh-CN` / `en-US`        | 返回语言，默认 `zh-CN`                                  |
| `version`      | `string` | 否   | `v1`                     | API 版本号，默认使用当前主版本                          |
| `platform`     | `string` | 否   | `web` / `android` / `ios`| 请求来源平台，用于埋点分析                              |

---
## 请求签名说明（sign）
签名算法如下：

1. 将所有参与签名的参数按参数名 ASCII 升序排序；
2. 拼接成 `key=value` 的形式，并用 `&` 连接；
3. 在末尾拼接密钥：`&secretKey=你的私钥`；
4. 对最终字符串进行 SHA256 加密，转为小写，即为签名字符串。

---
## 接口导航

- [注册用户](auth/register.md)
- [用户登录](auth/login.md)
- [获取当前用户信息](user/me.md)
- [数据模型](models.md)
