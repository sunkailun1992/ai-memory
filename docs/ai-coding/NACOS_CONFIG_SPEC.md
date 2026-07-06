# Nacos 配置中心规范

## dataId

`ai-memory` 使用 Spring Cloud Alibaba `spring.config.import` 导入远程配置。

本服务预留：

- `ai-memory.yaml`：记忆业务配置，例如窗口大小、召回条数、token 预算、候选写入策略、脱敏策略、索引开关。
- `ai-memory-spring.yaml`：datasource、profile、服务私有 Spring 配置。
- `qdrant.yaml`：长期记忆派生语义索引连接配置。
- `redis.yaml`：缓存和锁。
- `rabbitmq.yaml`：异步索引、删除同步和治理事件。
- `mybatis-plus.yaml`：数据库通用行为。
- `security-auth.yaml`：统一认证鉴权。
- `dubbo.yaml`：RPC provider/consumer。
- `a2a.yaml`：后续 MCP / Nacos AI Registry 工具注册共享契约。

## 禁止

- 禁止在仓库保存真实数据库密码、模型 Key、Qdrant 密钥、Nacos 密码。
- 禁止把同一个 `@ConfigurationProperties` 前缀拆到多个 dataId。
- 禁止在业务配置中散落裸 IP；基础设施地址引用共享变量。
- 禁止 `ai-agent` 通过配置直接拿到记忆 MySQL/Qdrant 连接并绕过 `ai-memory`。

## 新服务接入

如果 `ai-memory` 需要经网关访问或进入 Swagger UI 聚合，必须同步更新 `gateway-spring.yaml`，补充路由和 Swagger 聚合项，并读回验证。
