# 登录方法
  email登陆json 示例
  {
     "clientId": "24b5d2a7f4714409b4cc60bafc1dd2f6",
     "grantType": "email", 授权类型
     "code": null,
     "uuid": null,
     "channel": null, 渠道
     "source": null, 来源
     "inputInviteCode": null, 输入邀请码
     "appInfo": null,

## OpenAPI Specification

```yaml
openapi: 3.0.1
info:
  title: ''
  description: ''
  version: 1.0.0
paths:
  /t-api/toocans-broker-api/v1/index/auth/login:
    post:
      summary: |-
        登录方法
          email登陆json 示例
          {
             "clientId": "24b5d2a7f4714409b4cc60bafc1dd2f6",
             "grantType": "email", 授权类型
             "code": null,
             "uuid": null,
             "channel": null, 渠道
             "source": null, 来源
             "inputInviteCode": null, 输入邀请码
             "appInfo": null,
      deprecated: false
      description: |-
        登录方法
          email登陆json 示例
          {
             "clientId": "24b5d2a7f4714409b4cc60bafc1dd2f6",
             "grantType": "email", 授权类型
             "code": null,
             "uuid": null,
             "channel": null, 渠道
             "source": null, 来源
             "inputInviteCode": null, 输入邀请码
             "appInfo": null, 移动端设备信息
             "email": "2@2.com",
             "emailCode": "123456"
         }
         短信 登陆json 示例
         {
             "clientId": "24b5d2a7f4714409b4cc60bafc1dd2f6",
             "grantType": "sms",
             "code": null,
             "uuid": null,
             "channel": null,
             "source": null,
             "inputInviteCode": null,
             "appInfo": null,
             "nationalCode": "86",
             "phonenumber": "111111110188",
             "smsCode": "123456"
         }
         密码 登陆json 示例 username 如果是手机号码，  区号加号码 组成字符串
         {
             "clientId": "24b5d2a7f4714409b4cc60bafc1dd2f6",
             "grantType": "password",
             "code": null,
             "uuid": null,
             "channel": null,
             "source": null,
             "inputInviteCode": null,
             "appInfo": null,
             "username": "8618258210188",
             "password": "123456"
         }
      operationId: login
      tags:
        - 认证
      parameters:
        - name: Authorization
          in: header
          description: ''
          example: >-
            Bearer
            xxxxxxxx
          schema:
            type: string
            default: >-
              Bearer
              xxxxxxxx
      requestBody:
        content:
          application/json:
            schema:
              type: string
      responses:
        '200':
          description: 结果
          content:
            '*/*':
              schema:
                $ref: '#/components/schemas/RLoginVo'
          headers: {}
          x-apifox-name: 成功
        '401':
          description: Unauthorized
          content:
            '*/*':
              schema:
                type: string
          headers: {}
          x-apifox-name: 没有权限
        '500':
          description: Internal Server Error
          content:
            '*/*':
              schema:
                oneOf:
                  - $ref: '#/components/schemas/RVoid'
                  - type: string
          headers: {}
          x-apifox-name: 服务器错误
      security: []
      x-apifox-folder: 认证
      x-apifox-status: released
      x-run-in-apifox: https://app.apifox.com/web/project/5683897/apis/api-323185187-run
components:
  schemas:
    RLoginVo:
      type: object
      description: 响应信息主体
      properties:
        code:
          type: integer
          format: int32
        msg:
          type: string
        data:
          $ref: '#/components/schemas/LoginVo'
      x-apifox-orders:
        - code
        - msg
        - data
      x-apifox-ignore-properties: []
      x-apifox-folder: ''
    LoginVo:
      type: object
      description: 登录验证信息
      properties:
        scope:
          type: string
          description: 令牌权限
        access_token:
          type: string
          description: 授权令牌
        refresh_token:
          type: string
          description: 刷新令牌
        expire_in:
          type: integer
          format: int64
          description: 授权令牌 access_token 的有效期
        refresh_expire_in:
          type: integer
          format: int64
          description: 刷新令牌 refresh_token 的有效期
        client_id:
          type: string
          description: 应用id
      x-apifox-orders:
        - scope
        - access_token
        - refresh_token
        - expire_in
        - refresh_expire_in
        - client_id
      x-apifox-ignore-properties: []
      x-apifox-folder: ''
    RVoid:
      type: object
      description: 响应信息主体
      properties:
        code:
          type: integer
          format: int32
        msg:
          type: string
        data:
          type: object
          x-apifox-orders: []
          properties: {}
          x-apifox-ignore-properties: []
      x-apifox-orders:
        - code
        - msg
        - data
      x-apifox-ignore-properties: []
      x-apifox-folder: ''
  securitySchemes: {}
servers:
  - url: https://dev-api.bdy.tech
    description: 开发环境
security: []

```