# RPC API 协作规范

## 契约归属

- 跨服务 Dubbo RPC 接口、DTO、枚举和值对象统一维护在同级 `../rpc-api`。
- `ai-memory` 只实现或调用 `rpc-api` 中的契约，不在本仓库复制接口和 DTO。
- `utils` 只提供 Dubbo 上下文透传、公共配置、公共工具和中间件适配，不维护业务 RPC 契约。

## 预期契约

后续正式实现时，优先在 `../rpc-api` 设计以下契约：

- `MemoryRetrievalRpcService`：按患者、来源租户、会话、用途和 query 召回记忆上下文。
- `MemoryCandidateRpcService`：接收 `ai-agent` 提交的候选记忆。
- `MemoryGovernanceRpcService`：删除、归档、脱敏、撤销、重建索引。
- `MemoryEventRpcService`：写入记忆审计事件或查询治理事件。

## 上下文要求

RPC 入参必须显式或通过 Dubbo attachment 携带：

- 调用方服务名。
- 当前用户 ID。
- 当前租户 ID。
- 来源业务租户 ID。
- 患者 ID。
- 请求 traceId。
- 用途 purpose。

召回和写入接口不能只传自然语言 query。

## 禁止事项

- 禁止 `ai-agent` 直连记忆表或 Qdrant。
- 禁止在 `ai`、`ai-agent`、`rag` 内复制 ai-memory 的 RPC DTO。
- 禁止 RPC 返回未授权的完整隐私明文。

## 幂等声明（utils 2.1.1 起）

- utils 的 Dubbo 调用端统一重试只对提供方声明了幂等的方法，在请求发出后的连接类失败时重试；没声明的方法只在请求没发出时重试。
- 只在读实现确认不改业务状态（为执行本身写的日志、审计、评估记录不算）后，在实现类的 `@DubboService(methods = @Method(name = "<方法名>", parameters = {"idempotent", "true"}))` 上逐个方法声明；写方法不声明，拿不准就不声明。
- 不要写在服务级 `@DubboService(parameters = ...)` 上：服务级的 `idempotent` 对全部方法生效，包括以后新增的写方法。
- 本仓库已声明：`MemoryRetrievalRpcService.retrieve`（只写审计事件与检索评估两种执行记录）；`MemoryShortMemoryRpcService`、`MemoryGovernanceRpcService`、`MemoryCandidateRpcService` 含写操作，不声明。
- 本仓库已声明的方法与护栏由 `DubboIdempotentDeclarationTest` 锁定；规则见 utils `docs/ai-coding/RPC_API_CODING_SPEC.md`「调用端统一重试」。
