# AI 记忆服务设计模式规范

## 原则

- 升级、选型和架构设计先查证主流做法：对照 GitHub 上多个同类主流项目的源码与文档，以多数做法为准，不以单个项目为准；同时查官方文档、RFC 与标准、NIST / OWASP 等权威指南和相关论文，在设计或说明里写明依据与来源。不为缩小改动范围或省事采用非主流的简化做法；确需缩小范围时，写清主流做法与简化做法的差别和代价，由维护者决定。
- 设计模式服务于记忆边界：写入策略、召回策略、授权、脱敏、索引同步、缓存失效和审计。
- 普通 CRUD 不硬套 Factory、Manager、Abstract 层。
- 稳定扩展点出现后再抽 Strategy、Policy、Adapter、Pipeline。

## 推荐模式

- Policy：写入策略、召回策略、脱敏策略、授权策略。
- Adapter：Qdrant、Redis、RocketMQ、外部 RPC 适配。
- Pipeline：候选记忆从提交到入库的校验流水线。
- State：候选记忆、长期记忆、索引任务状态转换。
- Observer/Event：删除、归档、脱敏后触发缓存清理和索引同步。

## 禁止

- 禁止把 `ai`、`ai-agent`、`rag` 的职责抽象进 `ai-memory`。
- 禁止让 Controller 直接操作 Qdrant/Redis/Mapper。
- 禁止用全局静态可变状态保存记忆上下文。
