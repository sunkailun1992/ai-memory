# AI 记忆服务设计模式规范

## 原则

- 设计模式服务于记忆边界：写入策略、召回策略、授权、脱敏、索引同步、缓存失效和审计。
- 普通 CRUD 不硬套 Factory、Manager、Abstract 层。
- 稳定扩展点出现后再抽 Strategy、Policy、Adapter、Pipeline。

## 推荐模式

- Policy：写入策略、召回策略、脱敏策略、授权策略。
- Adapter：Qdrant、Redis、RabbitMQ、外部 RPC 适配。
- Pipeline：候选记忆从提交到入库的校验流水线。
- State：候选记忆、长期记忆、索引任务状态转换。
- Observer/Event：删除、归档、脱敏后触发缓存清理和索引同步。

## 禁止

- 禁止把 `ai`、`ai-agent`、`rag` 的职责抽象进 `ai-memory`。
- 禁止让 Controller 直接操作 Qdrant/Redis/Mapper。
- 禁止用全局静态可变状态保存记忆上下文。
