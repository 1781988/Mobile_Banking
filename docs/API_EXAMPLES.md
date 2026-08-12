# API 调用示例

以下示例假设应用运行在 `http://localhost:8080`。

## 1. 统一响应

成功响应：

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {},
  "requestId": "f9a4a0d8-5a31-4e4d-a8af-79cfd55a4dbc",
  "timestamp": "2026-08-12T08:00:00Z"
}
```

错误响应：

```json
{
  "success": false,
  "code": "INSUFFICIENT_BALANCE",
  "message": "账户可用余额不足",
  "requestId": "f9a4a0d8-5a31-4e4d-a8af-79cfd55a4dbc",
  "path": "/api/v1/transfers",
  "fieldErrors": {},
  "timestamp": "2026-08-12T08:00:00Z"
}
```

排障时使用响应头或响应体中的 `requestId` 搜索应用日志和审计日志。

## 2. Bash 完整流程

需要安装 `jq`：

```bash
BASE_URL=http://localhost:8080

TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"Bank@12345"}' \
  | jq -r '.data.accessToken')

echo "$TOKEN"

curl -s "$BASE_URL/api/v1/accounts" \
  -H "Authorization: Bearer $TOKEN" | jq

IDEMPOTENCY_KEY="cli-$(date +%s)-0001"
curl -s -X POST "$BASE_URL/api/v1/transfers" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -d '{
        "payerAccountNumber":"6222026000000001",
        "payeeAccountNumber":"6222026000000002",
        "amount":12.34,
        "remark":"CLI integration"
      }' | jq

curl -s "$BASE_URL/api/v1/transfers?size=20" \
  -H "Authorization: Bearer $TOKEN" | jq

curl -s "$BASE_URL/api/v1/accounts/6222026000000001/statement?size=20" \
  -H "Authorization: Bearer $TOKEN" | jq

curl -s -X POST "$BASE_URL/api/v1/auth/logout" \
  -H "Authorization: Bearer $TOKEN" | jq
```

## 3. PowerShell 完整流程

```powershell
$BaseUrl = "http://localhost:8080"
$Login = Invoke-RestMethod -Method Post `
  -Uri "$BaseUrl/api/v1/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"alice","password":"Bank@12345"}'

$Token = $Login.data.accessToken
$Headers = @{ Authorization = "Bearer $Token" }

Invoke-RestMethod -Uri "$BaseUrl/api/v1/accounts" -Headers $Headers

$TransferHeaders = @{
  Authorization = "Bearer $Token"
  "Idempotency-Key" = "powershell-$([guid]::NewGuid())"
}

$Body = @{
  payerAccountNumber = "6222026000000001"
  payeeAccountNumber = "6222026000000002"
  amount = 66.88
  remark = "PowerShell integration"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "$BaseUrl/api/v1/transfers" `
  -Headers $TransferHeaders `
  -ContentType "application/json" `
  -Body $Body
```

## 4. 账户冻结与解冻

管理员登录：

```bash
ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@12345"}' \
  | jq -r '.data.accessToken')
```

冻结：

```bash
curl -s -X PUT "$BASE_URL/api/v1/admin/accounts/6222026000000001/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"status":"FROZEN","reason":"可疑交易人工复核"}' | jq
```

解冻：

```bash
curl -s -X PUT "$BASE_URL/api/v1/admin/accounts/6222026000000001/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"status":"ACTIVE","reason":"复核通过"}' | jq
```

## 5. 风控、审计与对账

```bash
curl -s "$BASE_URL/api/v1/admin/risk-events?size=50" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq

curl -s "$BASE_URL/api/v1/admin/audit-logs?size=50" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq

curl -s -X POST "$BASE_URL/api/v1/admin/reconciliations" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq

curl -s "$BASE_URL/api/v1/admin/reconciliations/latest" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq
```

## 6. 典型错误码

| 错误码 | HTTP | 含义 |
|---|---:|---|
| `INVALID_CREDENTIALS` | 401 | 用户名或密码错误 |
| `TOO_MANY_LOGIN_ATTEMPTS` | 429 | 登录失败次数过多 |
| `UNAUTHORIZED` | 401 | 缺少或无效 Token |
| `FORBIDDEN` | 403 | 角色或资源权限不足 |
| `ACCOUNT_NOT_OWNED` | 403 | 付款账户不属于当前用户 |
| `ACCOUNT_NOT_ACTIVE` | 409 | 账户冻结或关闭 |
| `INSUFFICIENT_BALANCE` | 409 | 可用余额不足 |
| `SINGLE_LIMIT_EXCEEDED` | 409 | 超过单笔限额 |
| `DAILY_LIMIT_EXCEEDED` | 409 | 超过日累计限额 |
| `IDEMPOTENCY_KEY_REQUIRED` | 400 | 缺少幂等键 |
| `IDEMPOTENCY_CONFLICT` | 409 | 同一幂等键对应不同请求 |
| `DATA_CONFLICT` | 409 | 数据库约束或并发状态冲突 |
