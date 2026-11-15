-- 审计列按实体基类改名：create_date_time→create_time、modify_date_time→update_time、create_name→creator、
-- modify_name→updater、is_delete→is_deleted。列注释、默认值与索引随列保留，数据不变。

-- AI 候选记忆写入。
ALTER TABLE ai_memory_candidate RENAME COLUMN create_date_time TO create_time;
ALTER TABLE ai_memory_candidate RENAME COLUMN modify_date_time TO update_time;
ALTER TABLE ai_memory_candidate RENAME COLUMN create_name TO creator;
ALTER TABLE ai_memory_candidate RENAME COLUMN modify_name TO updater;
ALTER TABLE ai_memory_candidate RENAME COLUMN is_delete TO is_deleted;

-- AI 记忆审计事件。
ALTER TABLE ai_memory_event RENAME COLUMN create_date_time TO create_time;
ALTER TABLE ai_memory_event RENAME COLUMN modify_date_time TO update_time;
ALTER TABLE ai_memory_event RENAME COLUMN create_name TO creator;
ALTER TABLE ai_memory_event RENAME COLUMN modify_name TO updater;
ALTER TABLE ai_memory_event RENAME COLUMN is_delete TO is_deleted;

-- AI 患者长期记忆权威事实。
ALTER TABLE ai_memory_long_fact RENAME COLUMN create_date_time TO create_time;
ALTER TABLE ai_memory_long_fact RENAME COLUMN modify_date_time TO update_time;
ALTER TABLE ai_memory_long_fact RENAME COLUMN create_name TO creator;
ALTER TABLE ai_memory_long_fact RENAME COLUMN modify_name TO updater;
ALTER TABLE ai_memory_long_fact RENAME COLUMN is_delete TO is_deleted;

-- AI 记忆召回评估记录。
ALTER TABLE ai_memory_retrieval_eval RENAME COLUMN create_date_time TO create_time;
ALTER TABLE ai_memory_retrieval_eval RENAME COLUMN modify_date_time TO update_time;
ALTER TABLE ai_memory_retrieval_eval RENAME COLUMN create_name TO creator;
ALTER TABLE ai_memory_retrieval_eval RENAME COLUMN modify_name TO updater;
ALTER TABLE ai_memory_retrieval_eval RENAME COLUMN is_delete TO is_deleted;

-- AI 短期会话记忆窗口。
ALTER TABLE ai_memory_short_message RENAME COLUMN create_date_time TO create_time;
ALTER TABLE ai_memory_short_message RENAME COLUMN modify_date_time TO update_time;
ALTER TABLE ai_memory_short_message RENAME COLUMN create_name TO creator;
ALTER TABLE ai_memory_short_message RENAME COLUMN modify_name TO updater;
ALTER TABLE ai_memory_short_message RENAME COLUMN is_delete TO is_deleted;

-- AI 短期会话滚动摘要。
ALTER TABLE ai_memory_short_summary RENAME COLUMN create_date_time TO create_time;
ALTER TABLE ai_memory_short_summary RENAME COLUMN modify_date_time TO update_time;
ALTER TABLE ai_memory_short_summary RENAME COLUMN create_name TO creator;
ALTER TABLE ai_memory_short_summary RENAME COLUMN modify_name TO updater;
ALTER TABLE ai_memory_short_summary RENAME COLUMN is_delete TO is_deleted;

-- AI 记忆 Qdrant 派生索引任务。
ALTER TABLE ai_memory_vector_task RENAME COLUMN create_date_time TO create_time;
ALTER TABLE ai_memory_vector_task RENAME COLUMN modify_date_time TO update_time;
ALTER TABLE ai_memory_vector_task RENAME COLUMN create_name TO creator;
ALTER TABLE ai_memory_vector_task RENAME COLUMN modify_name TO updater;
ALTER TABLE ai_memory_vector_task RENAME COLUMN is_delete TO is_deleted;
