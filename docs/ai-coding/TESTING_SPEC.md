# AI 记忆服务测试规范

## 基础要求

- 当前项目统一使用 JUnit 5。
- 核心 Service 优先使用 Spring Boot 级别测试，验证事务、AOP、租户、Mapper 和策略组合。
- Controller 接口优先用真实 HTTP 集成测试覆盖统一响应、参数校验和权限。
- 纯对象测试只能覆盖小策略或不可变结果对象，不能代表核心链路已验证。

## 必测场景

- 短记忆窗口裁剪和滚动摘要。
- 长期记忆 upsert、归档、删除、脱敏。
- 候选记忆写入策略。
- 召回权限、租户、患者隔离。
- Redis 缓存命中、失效和删除同步。
- Qdrant 索引成功、失败降级、删除同步、重建。
- `ai-agent` 候选写入不能绕过策略。
- `rag` 公共知识不得写入患者记忆。

## 外部依赖

- 普通单元测试不得依赖真实 Nacos、MySQL、Redis、Qdrant、RabbitMQ 或外部模型。
- Qdrant/Redis 集成测试必须使用 fake、testcontainer、测试 profile 或显式开关。
- 依赖真实环境的验证必须在交付说明中标明。

## 验证命令

```bash
./gradlew test
./gradlew clean compileJava -x test
bash scripts/check-secrets.sh
```
