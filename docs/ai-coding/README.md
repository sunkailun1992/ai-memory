# AI 编码规范入口

本目录是 `ai-memory` 服务的 AI 编码规范入口。AI 新增或修改代码时，先读主规范，再按当前服务边界实现；迁移资料和公共示例只作为参考。

## 快速阅读

1. 先读项目根目录 `AGENTS.md`。
2. 再读项目根目录 `README.md`。
3. 必读 `MEMORY_BOUNDARY_SPEC.md`，确认短记忆、长记忆、RAG、Qdrant、`ai`、`ai-agent` 的边界。
4. 再读 `AI_CODING_GUIDE.md`，确认执行步骤和禁止事项。
5. 再读 `AI_DIRECTORY_STRUCTURE_GUIDE.md`，确认 Java 微服务目录、测试、资源、文档和跨项目边界。
6. 再读 `SECURITY_CODING_SPEC.md`，确认记忆读写、召回、删除、脱敏和授权安全规则。
7. 涉及跨服务调用时读 `RPC_API_CODING_SPEC.md`。
8. 涉及 Nacos、profile、dataId、环境变量时读 `NACOS_CONFIG_SPEC.md`。
9. 涉及测试时读 `TESTING_SPEC.md`。
10. 涉及公共工具、错误码、数据库、乐观锁时读 `UTILS_PUBLIC_SPEC.md`。

## 必读结论

- `ai-memory` 是记忆控制面和权威数据面。
- `ai`、`ai-agent` 不能绕过 `ai-memory` 直接写记忆表或 Qdrant。
- 短记忆是会话窗口 + 滚动摘要，不使用 Qdrant 做主存。
- 长记忆是患者级事实，MySQL 是权威库，Qdrant 是可重建派生索引。
- Redis 只做缓存、锁和短 TTL 协调，不做权威库。
- `rag` 负责公共知识库，不保存患者个人记忆。
- `ai` 负责登录、患者选择、问卷状态机和展示历史。
- `ai-agent` 负责模型执行，可以调用 `ai-memory` 召回或提交候选记忆，但不能直接落最终事实。
- 所有记忆召回都必须有患者、来源租户、调用方、用途、权限和 token/条数边界。
- 所有删除、归档、脱敏都必须覆盖 MySQL、Qdrant 派生索引、Redis 缓存和未执行任务。

## 目录结构

```text
docs/ai-coding/
  README.md
  MEMORY_BOUNDARY_SPEC.md
  AI_CODING_GUIDE.md
  AI_DIRECTORY_STRUCTURE_GUIDE.md
  AI_DESIGN_PATTERN_GUIDE.md
  AI_AUTOMATION_WORKFLOW.md
  AI_ENGINEERING_GUARDRAILS.md
  BRANCHING_SPEC.md
  ENVIRONMENT_CONFIG_SPEC.md
  VERSIONING_SPEC.md
  RPC_API_CODING_SPEC.md
  TESTING_SPEC.md
  PROJECT_CODING_SPEC.md
  SECURITY_CODING_SPEC.md
  NACOS_CONFIG_SPEC.md
  UTILS_PUBLIC_SPEC.md
  examples/
```

`examples/` 是公共 Java 微服务示例模板的本地副本；服务专属规则以 `MEMORY_BOUNDARY_SPEC.md`、`SECURITY_CODING_SPEC.md` 和 `AI_DIRECTORY_STRUCTURE_GUIDE.md` 为准。
