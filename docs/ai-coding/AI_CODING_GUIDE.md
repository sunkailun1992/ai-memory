# AI 编码执行指南

## 执行顺序

当用户要求新增或修改 `ai-memory` 功能时，AI 应按以下顺序工作：

1. 阅读 `AGENTS.md`、`README.md` 和 `docs/ai-coding/MEMORY_BOUNDARY_SPEC.md`。
2. 判断需求是否属于 `ai-memory`。如果属于 `ai`、`ai-agent` 或 `rag`，先说明边界，不要把代码硬写到本服务。
3. 阅读当前业务模块已有代码。
4. 如涉及跨服务契约，先检查同级 `../rpc-api`，缺失时先设计契约，再改 provider/consumer。
5. 如涉及公共工具、认证上下文、多租户、错误码或中间件封装，先检查同级 `../utils`。
6. 新增 Controller 时使用 RESTful 资源路径，补齐 `@Tag`、`@Operation` 和请求/响应 `@Schema`。
7. 新增 DDL 前检查 `MysqlDdl#getSqlFiles()` 和目标库 `ddl_history`；已执行或无法确认执行状态的脚本禁止原地修改。
8. 新增记忆读写逻辑时同步补测试，至少覆盖授权、租户隔离、患者隔离、删除/归档状态和失败降级。
9. 更新 README 或 AI 规范，确保后续 AI 能识别新边界。
10. 编译、测试、密钥扫描，并在交付说明中列出验证命令和未验证项。

## 编码原则

- MySQL 是记忆权威库；Qdrant 是长期记忆派生索引；Redis 是缓存和锁。
- 短期记忆采用会话窗口 + 滚动摘要，不能用 Qdrant 替代。
- 长期记忆按 `patientId + sourceTenantId` 隔离。
- `ai-agent` 提交的是候选记忆，最终是否入库由 `ai-memory` 策略决定。
- 召回结果必须来源可追溯、权限可解释、token 可控。
- 删除、归档、脱敏必须同步影响召回、缓存和索引。
- 不把公共知识库内容写入患者长期记忆。
- 不把患者个人记忆写入 `rag`。

## 推荐模块顺序

正式实现记忆能力时按以下顺序推进：

1. `entity/enums`：记忆类型、状态、来源、索引状态、候选状态。
2. `entity`：短记忆摘要、长期事实、候选记忆、记忆事件、索引任务。
3. `mapper`：MyBatis-Plus Mapper。
4. `service/policy`：写入策略、召回策略、脱敏策略、授权策略。
5. `service/query`：召回查询和复杂读模型。
6. `service/results`：召回结果、候选写入结果、治理结果。
7. `service/impl`：领域编排。
8. `controller`：HTTP 管理和调试入口。
9. `rpc`：Dubbo provider，契约必须来自 `../rpc-api`。
10. `job`：Qdrant 同步、缓存清理、归档任务。

## 禁止事项

- 禁止绕过 `ai-memory` 让 `ai-agent` 直连 MySQL/Qdrant。
- 禁止把 Qdrant 当作唯一事实存储。
- 禁止用 Qdrant 存短记忆主上下文。
- 禁止把患者长期记忆混入 `rag` 公共知识库。
- 禁止在日志、异常、审计明细中输出完整患者隐私。
- 禁止把公共工具类、RPC 契约复制到本服务。

## 验证命令

```bash
./gradlew clean compileJava test
bash scripts/check-secrets.sh
```

如果依赖 `utils` 或 `rpc-api` 有调整，先在对应同级项目执行：

```bash
./gradlew publishToMavenLocal
```
