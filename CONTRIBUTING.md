# 贡献指南

## 开发流程

1. 从 `main` 创建功能分支；
2. 修改代码并补充测试；
3. 执行 `mvn clean verify`；
4. 确认没有提交 `.env`、密码、Token、数据库备份或日志；
5. 提交 Pull Request，说明业务背景、事务边界、数据变化和回滚方案。

## 代码要求

- 金额必须使用 `BigDecimal`；
- 资金写操作必须明确事务边界；
- 新增重试接口必须设计幂等；
- 新增管理员操作必须记录审计日志；
- 客户资源必须验证归属，不能只依赖前端；
- 不在日志中记录密码、Token、完整证件号和无必要的完整账户号；
- 数据库结构变化必须新增 Flyway 迁移，禁止修改已发布迁移；
- README 只描述已实现和可验证的功能。

## Commit 建议

使用清晰的命令式描述，例如：

```text
feat: add beneficiary cooling period
fix: serialize concurrent debits by account lock
 test: cover idempotent transfer replay
 docs: document reconciliation operations
```
