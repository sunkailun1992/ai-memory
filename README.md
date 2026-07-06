# ai-memory

`ai-memory` 是 Kellen AI 体系的独立记忆服务，负责统一管理短期记忆、长期记忆、候选记忆写入、召回、删除、归档、脱敏、授权和审计边界。

当前仓库从现有 Java 微服务骨架复制初始化，已保留 Spring Boot、Nacos、Dubbo、MyBatis-Plus、Gradle、CI、脚本和完整 AI 编程规范。当前版本已提供短期记忆、长期候选写入、召回、治理、Qdrant 派生索引任务和 Dubbo/HTTP 调用入口。

## 服务边界

`ai-memory` 负责：

- 短期记忆：会话摘要、最近原文窗口、裁剪和清理策略。
- 长期记忆：患者级事实、问卷结果、重要健康线索、用户偏好。
- 写入策略：只接受受控来源或候选记忆，做幂等、置信度、敏感字段和授权校验后入库。
- 召回策略：按患者、来源租户、会话、用途和权限返回可注入模型的少量上下文。
- 治理能力：删除、归档、脱敏、撤销、重建索引、审计。
- 存储分层：MySQL 是权威库，Redis 是缓存/锁，Qdrant 是长期记忆的派生语义索引。

`ai-memory` 不负责：

- 客户端登录、患者选择、对话列表和 UI 展示历史，这些属于 `ai`。
- 问卷流程状态机、当前题推进和答案主表，这些属于 `ai`。
- 模型推理、Prompt 组织和结构化输出，这些属于 `ai-agent`。
- 公共知识库、医学指南、FAQ、文档切片检索，这些属于 `rag`。

## 与其它服务关系

```text
client apps
  -> ai
       -> ai-memory
       -> ai-agent

ai-agent
  -> ai-memory
  -> rag

rag
  -> Qdrant for public knowledge indexes

ai-memory
  -> MySQL authoritative memory
  -> Redis cache/locks
  -> Qdrant derived long-memory semantic index
```

## 存储原则

- MySQL 保存所有权威记忆、候选写入、来源、授权范围和审计字段。
- 短记忆不使用 Qdrant；短记忆需要严格时序，使用会话窗口 + 滚动摘要。
- 长记忆可以使用 Qdrant 做语义召回，但 Qdrant 只保存可重建索引，不保存唯一事实。
- Redis 不做权威库，只做热点读取缓存、幂等锁、异步索引锁和短 TTL 控制。

## 当前接口

```text
GET /api/ai-memory/capabilities
POST /api/ai-memory/short-messages
POST /api/ai-memory/short-summaries
POST /api/ai-memory/candidates
POST /api/ai-memory/retrievals
POST /api/ai-memory/governance
POST /api/ai-memory/vector-rebuild-tasks
```

HTTP 接口用于联调和管理后台，服务间调用优先使用 `rpc-api` 中的 Dubbo 契约：

- `MemoryShortMemoryRpcService`
- `MemoryCandidateRpcService`
- `MemoryRetrievalRpcService`
- `MemoryGovernanceRpcService`

## Nacos 配置

本地 profile 文件只保存连接 Nacos 的启动入口和 `spring.config.import` 列表。当前 `ai-memory` 预留：

```text
ai-memory.yaml
ai-memory-spring.yaml
qdrant.yaml
redis.yaml
rabbitmq.yaml
mybatis-plus.yaml
security-auth.yaml
dubbo.yaml
```

真实密钥、数据库密码、模型 Key、Qdrant 密钥只放 Nacos 或环境变量，不进入仓库。

## DDL

当前 `MysqlDdl#getSqlFiles()` 会自动执行：

```text
db/ai-memory-init.sql
```

该脚本包含：

- `ai_memory_short_summary`：短期会话摘要。
- `ai_memory_short_message`：短期原文窗口。
- `ai_memory_long_fact`：长期患者事实权威表。
- `ai_memory_candidate`：候选记忆写入表。
- `ai_memory_event`：删除、归档、脱敏、召回审计事件。
- `ai_memory_vector_task`：Qdrant 索引同步任务。
- `ai_memory_retrieval_eval`：召回评估记录。

空业务库仍建议先确认公共基础配置和数据源存在；脚本已包含 `ddl_history` 和 Seata `undo_log` 的 `IF NOT EXISTS`。

## 验证命令

```bash
./gradlew clean compileJava -x test
./gradlew test
bash scripts/check-secrets.sh
```

如果依赖 `utils` 或 `rpc-api` 有调整，先在同级项目执行 `./gradlew publishToMavenLocal`，再回到本项目编译。
