# Table of contents

* [🧠 用户认证 API 文档](README.md)
* [🔧 数据模型 Schemas](models.md)
* [OpenAPI Test](openapi-test/README.md)
  * ```yaml
    type: builtin:openapi
    props:
      models: true
    dependencies:
      spec:
        ref:
          kind: openapi
          spec: app-version-config
    ```
* [auth](auth/README.md)
  * [📌 POST /auth/login](auth/login.md)
  * [📌 POST /auth/register](auth/register.md)
* [user](user/README.md)
  * [📌 GET /user/me](user/me.md)
