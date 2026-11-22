-- 排序列 sorting 改名 sort（第6阶段第三部分，对应 utils 2.0.9 的 EntityBase.sort）。
-- 只改列名，列注释、默认值与数据随列保留；本库没有名字或定义里带 sorting 的索引。
-- 表只由本服务读写、前后端同批发布，按《Refactoring Databases》的 Rename Column 直接改名，不留过渡列。

-- AI 候选记忆写入。
ALTER TABLE ai_memory_candidate RENAME COLUMN sorting TO sort;

-- AI 记忆审计事件。
ALTER TABLE ai_memory_event RENAME COLUMN sorting TO sort;

-- AI 患者长期记忆权威事实。
ALTER TABLE ai_memory_long_fact RENAME COLUMN sorting TO sort;

-- AI 记忆召回评估记录。
ALTER TABLE ai_memory_retrieval_eval RENAME COLUMN sorting TO sort;

-- AI 短期会话记忆窗口。
ALTER TABLE ai_memory_short_message RENAME COLUMN sorting TO sort;

-- AI 短期会话滚动摘要。
ALTER TABLE ai_memory_short_summary RENAME COLUMN sorting TO sort;

-- AI 记忆 Qdrant 派生索引任务。
ALTER TABLE ai_memory_vector_task RENAME COLUMN sorting TO sort;
