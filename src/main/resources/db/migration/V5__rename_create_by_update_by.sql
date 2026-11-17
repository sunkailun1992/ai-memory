-- 审计列改为主流项目的多数命名（RuoYi-Vue、RuoYi-Vue-Plus、JeecgBoot）：creator→create_by、updater→update_by。
-- 只改列名，列注释、默认值与数据随列保留。

-- AI 候选记忆写入。
ALTER TABLE ai_memory_candidate RENAME COLUMN creator TO create_by;
ALTER TABLE ai_memory_candidate RENAME COLUMN updater TO update_by;

-- AI 记忆审计事件。
ALTER TABLE ai_memory_event RENAME COLUMN creator TO create_by;
ALTER TABLE ai_memory_event RENAME COLUMN updater TO update_by;

-- AI 患者长期记忆权威事实。
ALTER TABLE ai_memory_long_fact RENAME COLUMN creator TO create_by;
ALTER TABLE ai_memory_long_fact RENAME COLUMN updater TO update_by;

-- AI 记忆召回评估记录。
ALTER TABLE ai_memory_retrieval_eval RENAME COLUMN creator TO create_by;
ALTER TABLE ai_memory_retrieval_eval RENAME COLUMN updater TO update_by;

-- AI 短期会话记忆窗口。
ALTER TABLE ai_memory_short_message RENAME COLUMN creator TO create_by;
ALTER TABLE ai_memory_short_message RENAME COLUMN updater TO update_by;

-- AI 短期会话滚动摘要。
ALTER TABLE ai_memory_short_summary RENAME COLUMN creator TO create_by;
ALTER TABLE ai_memory_short_summary RENAME COLUMN updater TO update_by;

-- AI 记忆 Qdrant 派生索引任务。
ALTER TABLE ai_memory_vector_task RENAME COLUMN creator TO create_by;
ALTER TABLE ai_memory_vector_task RENAME COLUMN updater TO update_by;
