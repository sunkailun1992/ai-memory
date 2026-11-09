# ai-memory

`ai-memory` 是 Kellen AI 体系的独立记忆服务，负责统一管理短期记忆、长期记忆、候选记忆写入、召回、删除、归档、脱敏、授权和审计边界。

个人公共包固定为 `com:utils:1.6.0`，跨服务契约固定为 `com:rpc-api:1.0.7`。个人与公司共用私有 Maven 地址，utils 分别使用 `com` 与 `com.kellen` group 隔离；Java 包名保持 `com.kellen.*`。

当前仓库从现有 Java 微服务骨架复制初始化，已保留 Spring Boot、Nacos、Dubbo、MyBatis-Plus、Gradle、CI、脚本和完整 AI 编程规范。当前版本已提供短期记忆、长期候选写入、召回、治理、Qdrant 派生索引任务和 Dubbo/HTTP 调用入口。

代码分支使用 `main`（正式主干）、`test`（整合测试）、`preprod`（候选预发）与短功能分支。分支不自动改变运行 profile；完整功能进入远端 main 并核对后，按本任务授权清理本地与远程功能分支，保护未合、其他 worktree 占用及长期分支。细则见 [分支管理规范](docs/ai-coding/BRANCHING_SPEC.md)。

## 启动入口

主应用类为 `com.kellen.AiMemoryApplication`，IDE 可直接运行该类。Gradle `bootRun` 与可执行 JAR 的入口由 `springBoot.mainClass` 统一指定；仍使用 `./gradlew bootRun` 或 `java -jar build/libs/ai-memory.jar`，工作目录保持为当前服务仓库根目录。

## 本地 dev 启动

本地默认使用 `dev` profile。IDE 的 working directory、`./gradlew bootRun` 和 `java -jar build/libs/<服务名>.jar` 的当前目录必须是本服务仓库根目录，Spring 才会自动读取 `config/application-dev.properties`。该私有文件由本机入口生成、权限为 `0600`，仅保存 Nacos 连接的五个引导键；不放模型或数据库密钥，不提交 Git，也不修改 `application.yml`。从其它目录启动时，应显式设置 `spring.config.additional-location`，不能假设自动搜索会定位到服务仓库。

同名 OS 环境变量优先于私有文件；dev 地址按 `GITHUB_NACOS_DEV_SERVER_ADDR`、`GITHUB_NACOS_SERVER_ADDR` 依次读取，两者都未提供时拒绝缺失地址，不再回落到 `127.0.0.1:8848`。控制台使用 `18080`，客户端/SDK 使用 `8848`，SDK gRPC 连接使用 `9848`，不能把控制台端口当客户端地址。个人 namespace ID 保持 `github-dev`；远端业务配置仍从 Nacos 读取。单独激活 `test` 或 `prod` 不会读取这个 dev 私有文件，需提供其各自的连接配置。

## 服务边界

`ai-memory` 负责：

- 短期记忆：会话摘要、最近原文窗口、裁剪和清理策略。
- 长期记忆：患者级事实、问卷结果、重要健康线索、用户偏好。
- 写入策略：只接受受控来源或候选记忆，做幂等、置信度、敏感字段和授权校验后入库。
- 召回策略：按患者、来源租户、会话、用途和权限返回可注入模型的少量上下文。
- 治理能力：删除、归档、脱敏、撤销、重建索引、审计。
- 存储分层：PostgreSQL 是权威库，Redis 是缓存/锁，Qdrant 是长期记忆的派生语义索引。

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
  -> PostgreSQL authoritative memory
  -> Redis cache/locks
  -> Qdrant derived long-memory semantic index
```

## 存储原则

- PostgreSQL 保存所有权威记忆、候选写入、来源、授权范围和审计字段。
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

PostgreSQL 使用 `postgresql.*` / `postgresql-gray.*` 连接键与 `org.postgresql.Driver`，不回退到旧 MySQL 键。字符字段使用 ICU `fleet_general_ci` 保留常用大小写、重音不敏感的等值、唯一键和 `LIKE` 查询；这不等于完整复刻 MySQL Unicode 权重或尾空格规则，迁移前必须检查唯一键碰撞和尾空格。PG 18 的 nondeterministic collation 支持 `LIKE`，不要将现有查询改成不支持该排序规则的 `ILIKE` 或正则匹配。

真实 PG 结构回归通过受控进程环境提供 `FLEET_PG_TEST_URL`、`FLEET_PG_TEST_USERNAME`、`FLEET_PG_TEST_PASSWORD`；只允许 `fleet_pg_test_` 前缀的独立测试库，测试只创建并清理随机 schema。未提供时明确跳过，H2 回归不能代替 PostgreSQL 验收。凭据不得写进命令文本、仓库或测试报告。

当前 `PostgresqlDdl#getSqlFiles()` 会自动执行：

```text
db/postgresql/common-infra-schema.sql
db/postgresql/ai-memory-schema.sql
```

该脚本包含：

- `ai_memory_short_summary`：短期会话摘要。
- `ai_memory_short_message`：短期原文窗口。
- `ai_memory_long_fact`：长期患者事实权威表。
- `ai_memory_candidate`：候选记忆写入表。
- `ai_memory_event`：删除、归档、脱敏、召回审计事件。
- `ai_memory_vector_task`：Qdrant 索引同步任务。
- `ai_memory_retrieval_eval`：召回评估记录。

空业务库必须在首次启动前先执行 `../utils/src/main/resources/db/postgresql/common-infra-schema.sql`，再执行业务结构。历史 MySQL 初始化脚本保留，不在 PostgreSQL 上执行。

## 验证命令

```bash
./gradlew clean compileJava -x test
./gradlew test
bash scripts/check-secrets.sh
```

如果依赖 `utils` 或 `rpc-api` 有调整，先在同级项目执行 `./gradlew publishToMavenLocal`，再回到本项目编译。

通用安全、隔离与本批验证边界见 [安全与架构说明](docs/security-architecture-sync.md)。

## 本批可观测性接入

当前服务版本为 `1.1.0`，公共能力精确消费 `com:utils:1.6.0`，并由公共包传递引入轻量 `com:fleet-observability:0.1.1` 的安全 HTTP 终态事件。公共制品先完成 Maven Local 验证，再按对应 main 确定提交发布远端；目标环境的实际采集与全链读回单独验收。九服务共享 `actuator.yaml` 暴露 health/info/prometheus；除 health 外仍要求 `actuatorReq` 运维令牌，空值或错误值失败关闭。`logging.yml` 使用 ECS 固定字段白名单，未知 MDC、原始消息、正文和异常不进入采集结果；OTel agent 标准 `trace_id/span_id` 与业务会话 ID 分开。应用指标带固定 application/environment 标签，禁止动态业务 ID。

Admin/Zipkin 模板与三 profile 的 imports 已退役。服务 JVM 的可选 OTel agent 由统一启动入口负责，不通过全局 JAVA_TOOL_OPTIONS 注入 Gradle 或测试。部署时 Java 指标只由 Micrometer Prometheus 提供，OTEL_METRICS_EXPORTER 与 OTEL_LOGS_EXPORTER 均为 none；监控组件、配置发布和实际采集仍须独立验证。回滚须恢复匹配的制品与配置，先恢复旧 dataId 再回退旧消费者，不能用已删除的配置项假装完成回滚。

个人业务 PostgreSQL 迁移对应服务版本为 `1.1.0`，公共库精确消费 `com:utils:1.6.0`（传递安全采集库 `com:fleet-observability:0.1.1`）。版本和本机构建通过不代表目标环境已切库，正式迁移与运行态验收另行记录。

个人 Java 服务已移除 XXL-JOB 依赖、执行器配置与演示任务，不再读取 `xxl-job.yaml`。Conductor OSS 作为 NAS 独立服务通过 REST 接入；当前未接入业务 Java 调度接口，不添加 Java 21 SDK 或虚构任务开关。其他环境仍使用的 NAS 容器不由本次个人源码退役处理。
