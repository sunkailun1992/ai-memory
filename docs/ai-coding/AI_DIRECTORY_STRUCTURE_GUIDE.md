# AI 记忆服务目录规范

## 基础目录

```text
src/main/java/com/kellen/
  ApiApplication.java
  bean/
  memory/
    controller/
    entity/
    entity/bo/
    entity/query/
    entity/vo/
    entity/enums/
    mapper/
    rpc/
    service/
    service/impl/
    service/policy/
    service/query/
    service/results/
  job/

src/main/resources/
  application.yml
  application-dev.yml
  application-test.yml
  application-prod.yml
  db/

src/test/java/com/kellen/
  memory/
```

## 分层职责

| 目录 | 职责 |
|---|---|
| `memory/controller` | HTTP 管理、调试、OpenAPI 入口 |
| `memory/rpc` | Dubbo provider/consumer，契约必须来自 `../rpc-api` |
| `memory/entity` | MySQL 权威实体 |
| `memory/entity/bo` | 写入请求对象 |
| `memory/entity/query` | 查询请求对象 |
| `memory/entity/vo` | HTTP 响应视图对象 |
| `memory/entity/enums` | 记忆状态、类型、来源、索引状态枚举 |
| `memory/mapper` | MyBatis-Plus Mapper |
| `memory/service` | 领域服务接口 |
| `memory/service/impl` | 领域服务实现 |
| `memory/service/policy` | 写入、召回、脱敏、授权、归档策略 |
| `memory/service/query` | 复杂查询模型 |
| `memory/service/results` | Service 层结果对象 |
| `job` | Qdrant 同步、缓存清理、归档和重建任务 |

## 边界规则

- 不新建 `rag`、`agent`、`ai` 业务包承接其它服务职责。
- 不在本仓库复制 `rpc-api` DTO 或接口。
- 不在本仓库复制 `utils` 公共源码。
- 记忆领域按技术分层组织；当某类能力持续膨胀且跨层改动频繁时，再评估按 feature 分包。
- 新增 Qdrant、Redis、RabbitMQ 适配代码时优先放在明确的 adapter/policy/job 边界内，不散落在 Controller。
