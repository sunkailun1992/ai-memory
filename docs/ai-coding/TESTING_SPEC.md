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

- 普通单元测试不得依赖真实 Nacos、PostgreSQL、Redis、Qdrant、RabbitMQ 或外部模型。
- Qdrant/Redis 集成测试必须使用 fake、testcontainer、测试 profile 或显式开关。
- 依赖真实环境的验证必须在交付说明中标明。

## 测试稳定性

- 测试结果只能由输入决定，不依赖真实时间、线程调度、连接复用、端口、执行顺序或本机遗留状态。需要时间的逻辑注入 `Clock`；等待异步结果用有总时限的条件等待，不用固定 `Thread.sleep` 赌时序。
- 本地 HTTP 桩服务每个测试独占随机端口，并在 `@AfterEach` 停止。同一测试对同一被测实例请求多次时，桩响应设 `Connection: close`，不依赖 JDK `HttpServer` 的 keep-alive 连接回收时序。更复杂的桩可评估 MockWebServer 或 WireMock，新增依赖按依赖变更处理。
- 连接池、事件循环、静态单例、系统属性等进程级共享状态，不能成为测试之间的隐含依赖；用到时写明隔离方式，不依赖测试类或测试方法的执行顺序。
- 新增或修改涉及网络、并发、超时、流式的测试，合并前本机连续三轮运行该测试类全部通过：`./gradlew test --rerun --tests '<测试类全名>'`。
- 偶发失败按缺陷处理：先查明原因再修，不用自动重试、放宽断言、`@Disabled` 或删除测试换取通过。确需临时禁用时，在注解里写明原因与恢复条件，并在交付说明中列出。
- 本机 macOS 通过不代表 CI 的 Linux 通过；只在一个平台验证过的结论要写明平台。

## 验证命令

```bash
./gradlew test
./gradlew clean compileJava -x test
bash scripts/check-secrets.sh
```
