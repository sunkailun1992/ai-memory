# AGENTS.md

本文件是 `ai-memory` 服务的 AI 编码入口。AI 修改本项目代码前，必须先阅读本文件，再按任务风险阅读 `README.md` 和 `docs/ai-coding` 下的规范。

## Nacos 配置注释要求

- 新增或修改 Nacos 配置、模板、引导配置及渲染/发布工具前，必须阅读 [Nacos 注释与归类规范](docs/ai-coding/NACOS_COMMENT_SPEC.md)。
- 每个 dataId 写明职责、环境归属、消费者；每个可调参数写明用途、必要单位、格式、空值及开关影响。YAML 使用 `#` 注释，渲染结果必须保留说明。
- 注释整理保持键、类型、值、占位符和列表/import 顺序不变；发布前读回权威配置并备份，使用旧版本比对/CAS，发布后逐项读回。真实凭据不得进入 Git 或注释。
- 只处理明确授权的仓库及环境；Yudao 项目排除，不随本规范同步修改。

## 通用工程与 Git 交付约束

- 当前长期分支为 `main`、`test`、`preprod`，分别承担正式主干、整合测试和候选预发；它们不自动决定 profile/namespace。个人 dev 入口、`github-dev` 与公共库 `com` 坐标保持，不能复制公司环境或业务分支规则覆盖。
- 修改前先核仓库、HEAD、dirty、worktree 和其他会话，再 fetch；普通新目标从最新 origin/main 建短分支，同一任务续作和用户明确基线按现有授权处理。完整功能与必需跨仓配套一起验证交付。
- 本任务已授权合 main 并清理时，完整功能进入远端 main 且包含关系读回通过后清理本地和远程功能分支；只进 test/preprod、未合、被其他 worktree 使用或仍有唯一工作时保留。保护三个长期分支，不强推，不用强删绕过检查。细则见 `docs/ai-coding/BRANCHING_SPEC.md`。
- 高风险变更必须有独立风险验证，不能只依赖实现者的唯一自证测试；Review 可按需补充，不增加已授权提交/合并/推送的人工批准门禁。纯文档运行差异、路径和凭据检查，不机械重跑 Java 测试。
- 修改版本前读 `docs/ai-coding/VERSIONING_SPEC.md`：MINOR/PATCH 各为 0–9，`1.2.9 -> 1.3.0`、`1.9.9 -> 2.0.0`；先核远端、tag、在途分支和已发布制品占用，同批只升一次，纯规范不升业务版本，不覆盖旧 tag/同版制品。
- Git 集成、CI、远端制品、部署和实际验收分别取证；当前任务已给出的授权持续有效，未覆盖的外部发布动作仍需明确授权。

## 项目定位

- 项目名称：`ai-memory`
- 项目类型：AI 记忆服务后端，为 `ai`、`ai-agent` 和后续中台智能体统一提供短期记忆、长期记忆、召回、候选写入、删除、归档、脱敏、授权和审计能力。
- 技术栈：Java 17、Spring Boot 4、Spring Cloud Alibaba、Nacos、Dubbo、MyBatis-Plus、Gradle、`com:utils`、`com:rpc-api`。
- 后续中间件：PostgreSQL 权威存储、Redis 缓存/锁、Qdrant 长期记忆派生语义索引、RabbitMQ 异步索引；Conductor OSS 独立任务编排（业务 REST 接入另行验收）。
- Utils 制品隔离：本 GitHub 个人项目固定使用 `com:utils:1.6.0`；公司项目使用 `com.kellen:utils`。二者共用私有 Maven 地址，以 group 区分制品；Java 包名 `com.kellen.*` 不随 Maven group 改动。
- 同级依赖：`../rpc-api` 提供跨服务 RPC 接口和 DTO 契约；`../utils` 提供公共响应、认证上下文、租户、多数据源和基础工具；`../ai` 是业务入口；`../ai-agent` 是模型执行层；`../rag` 是公共知识检索服务。
- 当前状态：从现有微服务工程骨架复制初始化，已保留基础工程、配置入口、CI、脚本和 AI 编码规范；真实记忆表、RPC 契约、Qdrant 索引和 ai-agent 直连调用尚未实现。

## 必读顺序

1. `README.md`：确认当前 ai-memory 服务职责、接口范围、表结构和验证命令。
2. `docs/ai-coding/README.md`：确认 AI 编码入口和阅读顺序。
3. `docs/ai-coding/MEMORY_BOUNDARY_SPEC.md`：确认短记忆、长记忆、RAG、Qdrant、ai、ai-agent 的职责边界。
4. `docs/ai-coding/AI_CODING_GUIDE.md`：确认执行步骤、注释规则、测试和安全要求。
5. `docs/ai-coding/AI_DIRECTORY_STRUCTURE_GUIDE.md`：确认 Java 微服务目录、测试、资源、文档和跨项目边界。
6. `docs/ai-coding/RPC_API_CODING_SPEC.md`：涉及 Dubbo RPC provider、consumer、接口或 DTO 时必须阅读。
7. `docs/ai-coding/SECURITY_CODING_SPEC.md`：涉及记忆读写、删除、脱敏、授权、召回、日志、SQL 或测试安全时必须阅读。
8. `docs/ai-coding/TESTING_SPEC.md`：确认记忆链路测试、权限测试、索引任务测试和降级测试要求。
9. `docs/ai-coding/NACOS_CONFIG_SPEC.md`：修改 Nacos 配置中心、共享 dataId 或 `application.yml` import 前必读。
10. 目标 Controller、Service、Repository、RPC、Qdrant/Redis/消息配置和真实 Nacos 模板：确认真实调用链，不按文件名猜行为。

## 项目边界

- `ai-memory` 是记忆控制面和权威数据面；`ai`、`ai-agent` 不能绕过它直接写记忆表或 Qdrant。
- `ai-memory` 可以向 `ai-agent` 暴露记忆召回和候选记忆写入能力，但候选记忆必须经过本服务策略校验后才能成为长期记忆。
- `ai-memory` 不保存客户端 UI 展示历史；`ai_chat_conversation` 和 `ai_chat_conversation_message` 属于 `ai`。
- `ai-memory` 不推进问卷状态；`ai_questionnaire_session` 和 `ai_questionnaire_answer` 属于 `ai`。
- `ai-memory` 不做模型推理；模型调用、Prompt 组织和结构化输出属于 `ai-agent`。
- `ai-memory` 不做公共知识库 RAG；医学指南、FAQ、文档切片和知识库语义检索属于 `rag`。
- PostgreSQL 是记忆权威库；Qdrant 只是长期记忆派生索引；Redis 只是缓存和锁。
- 短记忆必须保持时序，采用会话窗口 + 滚动摘要，不用 Qdrant 做主存。
- 长记忆按 `patientId + sourceTenantId` 隔离；召回必须带用途、调用方、授权上下文和 token/条数上限。

## AI 工程门禁

- 记忆写入、召回、删除、归档、脱敏、授权、Qdrant 索引、Redis 缓存和跨服务 RPC 默认中高风险。
- 新增或修改功能前，必须确认该功能属于 `ai-memory`，而不是 `ai`、`ai-agent` 或 `rag`。
- 完成后必须说明：权限边界、租户隔离、患者隔离、脱敏策略、审计字段、失败降级、测试证据和回滚方式。
- 修改 DDL 前必须确认 `ddl_history`；已执行或无法确认执行状态的脚本禁止原地修改，必须追加新 SQL。

## 验证命令

```bash
./gradlew clean compileJava -x test
./gradlew test
bash scripts/check-secrets.sh
```

涉及 `rpc-api` 契约、Qdrant、Redis、RabbitMQ、MCP / AI Registry 注册、Nacos / 网关路由时，还需说明契约编译、索引验证、缓存验证、Nacos 注册验证或依赖外部环境的未验证项。

## 禁止事项

- 禁止 `ai-agent` 直连记忆 PostgreSQL 或 Qdrant 绕过 `ai-memory`。
- 禁止把 Qdrant 当作长期记忆唯一存储。
- 禁止用 Qdrant 存短期会话主记忆。
- 禁止把患者长期记忆放入 `rag` 公共知识库。
- 禁止把完整患者隐私、证件号、手机号、报告正文、完整对话直接写日志、异常、审计详情或模型请求调试输出。
- 禁止提交 `.DS_Store`、本机绝对路径、临时日志和无关构建产物。
