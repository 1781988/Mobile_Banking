# 安全设计与生产加固清单

## 1. 当前已实现

### 身份认证

- BCrypt 密码摘要；
- 不透明随机 Token；
- Token 原文不入库；
- Token 过期与主动撤销；
- 登录失败限流；
- 统一的错误信息，避免用户名枚举；
- 不存在用户时仍执行一次 BCrypt 比较，减小响应时间侧信道。

### 授权

- `USER` 和 `ADMIN` 角色；
- `/api/v1/admin/**` 只允许管理员；
- 账户、流水和交易详情在服务层验证资源归属；
- 前端隐藏菜单不被视为授权措施。

### 输入与输出

- Bean Validation；
- 金额两位小数和正值校验；
- 幂等键字符白名单；
- 请求 ID 字符白名单；
- 账户号按场景脱敏；
- 异常不返回堆栈和 SQL；
- JSON 响应使用稳定错误码。

### 部署

- 配置从环境变量注入；
- `.env` 不进入版本库；
- Redis 设置密码；
- MySQL 和 Redis 默认只绑定本机端口；
- 应用容器使用非 root 用户；
- 只读根文件系统；
- `no-new-privileges`；
- 健康检查与优雅停机。

## 2. 反向代理与客户端 IP

应用默认使用 `SERVER_FORWARD_HEADERS_STRATEGY=none`，因此不会信任客户端可伪造的 `Forwarded` 或 `X-Forwarded-*`。部署在 Nginx、Ingress 或云负载均衡后时：

1. 只允许可信代理访问应用端口；
2. 在代理入口删除客户端自带的 `Forwarded`/`X-Forwarded-*`；
3. 由代理重新设置真实来源地址；
4. 仅在上述可信边界成立后设置 `SERVER_FORWARD_HEADERS_STRATEGY=framework`；
5. 验证 Spring 包装后的 `request.remoteAddr`。

## 3. Token 存储

Web 工作台使用 `sessionStorage`，关闭标签页后浏览器会话令牌消失。该方案避免长期持久化，但仍必须防止 XSS。

生产 Web 应优先评估：

- BFF + `HttpOnly`、`Secure`、`SameSite` Cookie；
- CSP、Trusted Types 和严格资源白名单；
- Token 轮换、设备绑定和并发会话控制；
- 高风险操作二次认证。

## 4. 数据库权限

生产数据库用户不应拥有全局权限。迁移账户和运行账户建议分离：

- Flyway 迁移账户：允许 DDL；
- 应用运行账户：仅允许目标表的必要 DML；
- 只读报表账户：只允许查询只读副本；
- 禁止应用使用 root。

## 5. 密钥和密码管理

不得在以下位置保存生产秘密：

- Git 历史；
- Dockerfile；
- 镜像层；
- `application.yml`；
- 构建日志；
- 前端 JavaScript。

推荐使用：

- Kubernetes Secret + KMS 加密；
- AWS Secrets Manager、Azure Key Vault、Google Secret Manager；
- 阿里云 KMS/Secrets Manager；
- Vault；
- 定期轮换与访问审计。

## 6. 生产必须补充

### 网络与传输

- TLS 1.2+；
- HSTS；
- WAF 和 API 网关；
- 数据库 TLS；
- 内网隔离和最小安全组；
- 管理端点不暴露公网。

### 客户与交易安全

- KYC/AML；
- MFA；
- 交易签名；
- 短信/Push 验证；
- 收款人白名单和冷静期；
- 设备指纹；
- IP、地理位置和行为风控；
- 高风险交易人工复核。

### 数据保护

- PII 字段加密或令牌化；
- 数据分类分级；
- 脱敏查询和最小化返回；
- 备份加密；
- 数据保留与删除策略；
- 隐私授权和访问审计。

### 安全工程

- SAST、SCA、容器扫描和 Secret Scan；
- DAST 与渗透测试；
- SBOM；
- 依赖升级策略；
- SIEM、告警和应急响应；
- 审计日志发送到不可变远程存储；
- 威胁建模和安全评审。

## 7. 威胁与缓解示例

| 威胁 | 当前缓解 | 生产补充 |
|---|---|---|
| 暴力破解 | 登录失败限流、统一错误 | 验证码、设备风控、IP 信誉 |
| 数据库泄露 | BCrypt、Token 摘要 | PII 加密、密钥分离、数据库审计 |
| 重放转账 | 幂等键、请求摘要 | 交易签名、时间戳、随机挑战 |
| 越权查询 | RBAC + 资源归属校验 | ABAC、数据权限策略、自动越权测试 |
| 重复扣款 | 唯一索引和订单状态 | 渠道级幂等、全局业务流水号 |
| XSS 窃取 Token | 页面不加载第三方脚本、使用 textContent | CSP、BFF/HttpOnly Cookie、前端安全扫描 |
| 伪造客户端 IP | 不直接信任 XFF | 可信代理链和网络隔离 |
| 手工改库 | 审计与日终对账 | 数据库审计、双人审批、不可变账本 |
