# Base Data

All endpoints below require signature authentication.

---

## 1.GET Get Server Timestamp

GET /t-api/openapi/v1/bc/baseConfig/serverTime

### Request Parameters

None

> Response Example

> 200 Response

```json
{"code":200,"msg":"success","data":"1733197200000"}
```

### Response Schema

Status code **200**

*Response body*

|Name|Type|Required|Constraint|Description|
|---|---|---|---|---|
|» code|integer(int32)|false|none|none|
|» msg|string|false|none|none|
|» data|string|false|none|Server timestamp (milliseconds)|

---

## 2.GET Get All Networks {#get-all-networks}
Returns a list of all supported blockchain networks.

GET /t-api/openapi/v1/bc/baseConfig/allNetWork

### Request Parameters

None

> Response Example

> 200 Response

```json
{"code":200,"msg":"success","data":[{"chainId":"TRON","chainName":"TRON","chainIcon":"https://example.com/tron.png"},{"chainId":"BNB","chainName":"BNB Smart Chain","chainIcon":"https://example.com/bnb.png"}]}
```

### Response Schema

Status code **200**

*Response body*

|Name|Type|Required|Constraint|Description|
|---|---|---|---|---|
|» code|integer(int32)|false|none|none|
|» msg|string|false|none|none|
|» data|[[Chain]]|false|none|[List of blockchain networks]|

### Chain Properties

|Name|Type|Required|Constraint|Description|
|---|---|---|---|---|
|chainId|string|false|none|Chain ID|
|chainName|string|false|none|Chain name|
|chainIcon|string|false|none|Chain icon URL|

---

## 3.GET Get Token Configuration for a Token {#get-token-config}

Returns configuration for a token across chains, including deposit/withdrawal limits and fees.

GET /t-api/openapi/v1/op/openapi/allToken

#### Request Parameters

| Name | Location | Type | Required | Description |
| ------ | ----- | ------ | -- | ------------ |
| tokenId | query | string | Yes | Token ID, e.g. USDT |
| chainId | query | string | No | Chain ID, e.g. TRON |

> Response Example

> 200 Response

```json
{"code":200,"msg":"success","data":[{"id":"1","tokenId":"USDT","tokenName":"USDT","tokenFullName":"Tether USD","chainId":"TRON","createdAt":"1626307200000","updatedAt":"1626307200000","icon":"https://example.com/usdt.png","protocolName":"TRC20","chainName":"TRON","chainIcon":"https://example.com/tron.png","tokenChainId":"TRON_USDT","status":1,"allowDeposit":1,"allowWithdraw":1,"depositMinQuantity":"1","withdrawMinQuantity":"10","withdrawMaxDayQuantity":"100000","depositCharges":"0.001","depositChargesMinAmount":"0.1","withdrawalChargesMinAmount":"1","withdrawChargeValue":"0.01"}]}
```

#### Response Schema

Status code **200**

_Response body_

| Name | Type | Required | Constraint | Description |
| ------ | -------------- | ----- | ---- | ------------ |
| » code | integer(int32) | false | none | none |
| » msg | string | false | none | none |
| » data | \[\[Token]] | false | none | \[Token configuration list] |

#### Token Properties

| Name | Type | Required | Constraint | Description |
| -------------------------- | -------------- | ----- | ---- | --------------------- |
| id | string | false | none | none |
| tokenId | string | false | none | Non-numeric identifier, e.g. "USDT" |
| tokenName | string | false | none | show name, can rename |
| tokenFullName | string | false | none | full name |
| chainId | string | false | none | Chain token ID, e.g. "TRON" |
| createdAt | string | false | none | none |
| updatedAt | string | false | none | none |
| icon | string | false | none | token icon |
| protocolName | string | false | none | Chain protocol name |
| chainName | string | false | none | Chain name |
| chainIcon | string | false | none | chain icon |
| status | integer(int32) | false | none | none |
| allowDeposit | integer(int32) | false | none | 1 = deposits allowed, 0 = disabled |
| allowWithdraw | integer(int32) | false | none | 1 = withdrawals allowed, 0 = disabled |
| depositMinQuantity | string | false | none | Minimum deposit amount; amounts below this are not credited |
| withdrawMinQuantity | string | false | none | Minimum withdrawal amount |
| withdrawMaxDayQuantity | string | false | none | Maximum daily withdrawal amount for this token |
| depositCharges | string | false | none | Deposit fee percentage — broker configuration |
| depositChargesMinAmount | string | false | none | Minimum deposit fee — broker configuration |
| withdrawalChargesMinAmount | string | false | none | Minimum withdrawal fee — broker configuration |
| withdrawChargeValue | string | false | none | Withdrawal fee percentage |

