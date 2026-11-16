-- 实体租户基类新增创建部门：7 张记忆表加 create_dept，插入时由 utils 自动填当前登录用户的部门，没有登录用户（后台任务、服务调用）时为空。
-- 只加列，不删列、不改已有数据；已有行的创建部门未知，保持为空。数据权限按部门过滤仍按表在 security.data-permission 里开启。

-- AI 候选记忆写入。
ALTER TABLE ai_memory_candidate ADD COLUMN create_dept varchar(64) COLLATE fleet_general_ci;
COMMENT ON COLUMN ai_memory_candidate.create_dept IS '创建部门ID';

-- AI 记忆审计事件。
ALTER TABLE ai_memory_event ADD COLUMN create_dept varchar(64) COLLATE fleet_general_ci;
COMMENT ON COLUMN ai_memory_event.create_dept IS '创建部门ID';

-- AI 患者长期记忆权威事实。
ALTER TABLE ai_memory_long_fact ADD COLUMN create_dept varchar(64) COLLATE fleet_general_ci;
COMMENT ON COLUMN ai_memory_long_fact.create_dept IS '创建部门ID';

-- AI 记忆召回评估记录。
ALTER TABLE ai_memory_retrieval_eval ADD COLUMN create_dept varchar(64) COLLATE fleet_general_ci;
COMMENT ON COLUMN ai_memory_retrieval_eval.create_dept IS '创建部门ID';

-- AI 短期会话记忆窗口。
ALTER TABLE ai_memory_short_message ADD COLUMN create_dept varchar(64) COLLATE fleet_general_ci;
COMMENT ON COLUMN ai_memory_short_message.create_dept IS '创建部门ID';

-- AI 短期会话滚动摘要。
ALTER TABLE ai_memory_short_summary ADD COLUMN create_dept varchar(64) COLLATE fleet_general_ci;
COMMENT ON COLUMN ai_memory_short_summary.create_dept IS '创建部门ID';

-- AI 记忆 Qdrant 派生索引任务。
ALTER TABLE ai_memory_vector_task ADD COLUMN create_dept varchar(64) COLLATE fleet_general_ci;
COMMENT ON COLUMN ai_memory_vector_task.create_dept IS '创建部门ID';
