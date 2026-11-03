# 召回评估租户边界

召回评估记录显式保存调用方 `tenantId`，与既有 `sourceTenantId` 分别保留，避免评估记录失去所属租户。请求授权、来源租户、患者作用域、脱敏和预算策略继续由现有策略 Bean 执行。

本批仅移植此通用租户修正；不迁移业务数据、候选记忆、历史对话或任何模型 Prompt。依赖固定为 `com.kellen:utils:1.4.9` 与 `com:rpc-api:1.0.7`。

验证：`./gradlew test --no-daemon`、`./gradlew bootJar --no-daemon`、`bash scripts/check-secrets.sh`。租户评估回归使用真实 Service / 策略 Bean 与 Mapper 桩，不表示外部 MySQL/Qdrant/Redis 已验收。

交付验证执行完整 `./gradlew clean build --rerun-tasks --no-daemon`、凭据扫描与差异检查。公共库固定为仓库声明的精确版本；GitHub CI 从相应公共库的 main 构建 Maven Local。Git 提交、远端 CI、制品发布与实际部署分别以各自回执为准。
