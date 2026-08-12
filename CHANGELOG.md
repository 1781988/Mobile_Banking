# Changelog

## 1.0.1 - 2026-08-12

### Changed

- 统一项目名称、源码包名和公开元信息；
- Java 包命名空间调整为 `com.mobilebanking.platform`；
- README 重新整理为功能、架构、配置、使用和运维说明；
- 增加关键技术决策文档；
- 精简与当前项目维护无关的内部说明。

## 1.0.0 - 2026-08-12

### Added

- Java 21 / Spring Boot 3.5 核心架构；
- BCrypt 登录、不透明 Token、RBAC 与登录限流；
- 客户账户查询、账户流水和行内转账；
- 数据库行锁、本地事务、幂等订单与双边账本；
- 单笔和日累计限额、风险事件；
- 账户冻结、解冻与审计日志；
- 手工和定时日终对账；
- 中文响应式 Web 工作台；
- Flyway、Docker Compose、Actuator、Prometheus；
- 单元测试、API 测试和 MySQL 迁移 CI；
- 中文 README、架构、安全、API 和设计决策文档。

### Security hardening

- 本地初始化数据默认关闭，仅由本地配置显式启用；
- 默认不信任客户端转发头，仅允许在可信反向代理边界内启用；
- 未声明路由默认拒绝访问；
- 禁用无关的 Redis Repository 扫描和框架默认安全用户；
- 更新 GitHub Actions 工具链并使用 Flyway 12.10.0 验证 MySQL 8.4 迁移。
