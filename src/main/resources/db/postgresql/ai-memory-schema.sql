-- PostgreSQL 18 业务结构。历史 MySQL 脚本保留，不在 PostgreSQL 启动时执行。
-- 先以对应业务角色执行 utils 的 db/postgresql/common-infra-schema.sql，再执行本文件。
-- ICU 主级比较保留大小写、重音不敏感的常用匹配；迁移前另查唯一键碰撞与尾空格。
-- MySQL ON UPDATE 时间由应用显式更新；不创建 Seata AT 不支持的数据库触发器。
CREATE COLLATION IF NOT EXISTS public.fleet_general_ci (provider = icu, locale = 'und-u-ks-level1', deterministic = false);

CREATE TABLE IF NOT EXISTS ai_memory_candidate (
    id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    caller varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    purpose varchar(128) COLLATE public.fleet_general_ci,
    trace_id varchar(128) COLLATE public.fleet_general_ci,
    user_id varchar(64) COLLATE public.fleet_general_ci,
    tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL DEFAULT '100',
    source_tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    patient_id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    conversation_id varchar(128) COLLATE public.fleet_general_ci,
    source_type varchar(64) COLLATE public.fleet_general_ci,
    source_id varchar(128) COLLATE public.fleet_general_ci,
    memory_type varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    memory_key varchar(160) COLLATE public.fleet_general_ci NOT NULL,
    title varchar(255) COLLATE public.fleet_general_ci,
    content text COLLATE public.fleet_general_ci NOT NULL,
    facts_json text COLLATE public.fleet_general_ci,
    confidence double precision NOT NULL DEFAULT 0,
    requires_human_review boolean NOT NULL DEFAULT false,
    sensitivity_level varchar(32) COLLATE public.fleet_general_ci NOT NULL DEFAULT 'NORMAL',
    authorization_scope varchar(128) COLLATE public.fleet_general_ci,
    review_status varchar(32) COLLATE public.fleet_general_ci NOT NULL DEFAULT 'PENDING',
    rejection_reason varchar(500) COLLATE public.fleet_general_ci,
    promoted_memory_id varchar(64) COLLATE public.fleet_general_ci,
    occurred_at timestamp(0) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at timestamp(0) without time zone,
    code varchar(255) COLLATE public.fleet_general_ci,
    description varchar(255) COLLATE public.fleet_general_ci,
    create_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    create_name varchar(255) COLLATE public.fleet_general_ci,
    modify_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    modify_name varchar(255) COLLATE public.fleet_general_ci,
    is_delete boolean DEFAULT false,
    label varchar(255) COLLATE public.fleet_general_ci,
    sorting integer DEFAULT 0,
    version integer DEFAULT 1,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_ai_memory_candidate_patient_status ON ai_memory_candidate (patient_id, source_tenant_id, review_status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_ai_memory_candidate_source ON ai_memory_candidate (source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_ai_memory_candidate_trace ON ai_memory_candidate (trace_id);
COMMENT ON TABLE ai_memory_candidate IS 'AI 候选记忆写入';
COMMENT ON COLUMN ai_memory_candidate.id IS '主键ID';
COMMENT ON COLUMN ai_memory_candidate.caller IS '调用方';
COMMENT ON COLUMN ai_memory_candidate.purpose IS '写入目的';
COMMENT ON COLUMN ai_memory_candidate.trace_id IS '调用链追踪ID';
COMMENT ON COLUMN ai_memory_candidate.user_id IS '调用方用户ID';
COMMENT ON COLUMN ai_memory_candidate.tenant_id IS 'Kellen 内部租户ID';
COMMENT ON COLUMN ai_memory_candidate.source_tenant_id IS '来源业务租户ID';
COMMENT ON COLUMN ai_memory_candidate.patient_id IS '患者ID';
COMMENT ON COLUMN ai_memory_candidate.conversation_id IS '来源会话ID';
COMMENT ON COLUMN ai_memory_candidate.source_type IS '来源类型';
COMMENT ON COLUMN ai_memory_candidate.source_id IS '来源业务ID';
COMMENT ON COLUMN ai_memory_candidate.memory_type IS '记忆类型';
COMMENT ON COLUMN ai_memory_candidate.memory_key IS '稳定记忆键';
COMMENT ON COLUMN ai_memory_candidate.title IS '候选标题';
COMMENT ON COLUMN ai_memory_candidate.content IS '候选内容';
COMMENT ON COLUMN ai_memory_candidate.facts_json IS '结构化事实 JSON';
COMMENT ON COLUMN ai_memory_candidate.confidence IS '候选置信度';
COMMENT ON COLUMN ai_memory_candidate.requires_human_review IS '是否需要人工确认';
COMMENT ON COLUMN ai_memory_candidate.sensitivity_level IS '敏感级别';
COMMENT ON COLUMN ai_memory_candidate.authorization_scope IS '授权范围';
COMMENT ON COLUMN ai_memory_candidate.review_status IS '审核状态';
COMMENT ON COLUMN ai_memory_candidate.rejection_reason IS '拒绝原因';
COMMENT ON COLUMN ai_memory_candidate.promoted_memory_id IS '晋升后的长期记忆ID';
COMMENT ON COLUMN ai_memory_candidate.occurred_at IS '事实发生时间';
COMMENT ON COLUMN ai_memory_candidate.reviewed_at IS '审核时间';
COMMENT ON COLUMN ai_memory_candidate.code IS '编码';
COMMENT ON COLUMN ai_memory_candidate.description IS '说明';
COMMENT ON COLUMN ai_memory_candidate.create_date_time IS '创建时间';
COMMENT ON COLUMN ai_memory_candidate.create_name IS '创建人';
COMMENT ON COLUMN ai_memory_candidate.modify_date_time IS '修改时间';
COMMENT ON COLUMN ai_memory_candidate.modify_name IS '修改人';
COMMENT ON COLUMN ai_memory_candidate.is_delete IS '删除状态';
COMMENT ON COLUMN ai_memory_candidate.label IS '标签';
COMMENT ON COLUMN ai_memory_candidate.sorting IS '排序';
COMMENT ON COLUMN ai_memory_candidate.version IS '版本号';

CREATE TABLE IF NOT EXISTS ai_memory_event (
    id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    operation varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    target_type varchar(64) COLLATE public.fleet_general_ci,
    target_id varchar(128) COLLATE public.fleet_general_ci,
    caller varchar(64) COLLATE public.fleet_general_ci,
    purpose varchar(128) COLLATE public.fleet_general_ci,
    trace_id varchar(128) COLLATE public.fleet_general_ci,
    user_id varchar(64) COLLATE public.fleet_general_ci,
    tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL DEFAULT '100',
    source_tenant_id varchar(64) COLLATE public.fleet_general_ci,
    patient_id varchar(64) COLLATE public.fleet_general_ci,
    reason varchar(500) COLLATE public.fleet_general_ci,
    result varchar(64) COLLATE public.fleet_general_ci,
    detail_json text COLLATE public.fleet_general_ci,
    code varchar(255) COLLATE public.fleet_general_ci,
    description varchar(255) COLLATE public.fleet_general_ci,
    create_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    create_name varchar(255) COLLATE public.fleet_general_ci,
    modify_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    modify_name varchar(255) COLLATE public.fleet_general_ci,
    is_delete boolean DEFAULT false,
    label varchar(255) COLLATE public.fleet_general_ci,
    sorting integer DEFAULT 0,
    version integer DEFAULT 1,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_ai_memory_event_operation ON ai_memory_event (operation, create_date_time);
CREATE INDEX IF NOT EXISTS idx_ai_memory_event_patient_time ON ai_memory_event (patient_id, source_tenant_id, create_date_time);
CREATE INDEX IF NOT EXISTS idx_ai_memory_event_trace ON ai_memory_event (trace_id);
COMMENT ON TABLE ai_memory_event IS 'AI 记忆审计事件';
COMMENT ON COLUMN ai_memory_event.id IS '主键ID';
COMMENT ON COLUMN ai_memory_event.operation IS '操作类型';
COMMENT ON COLUMN ai_memory_event.target_type IS '目标类型';
COMMENT ON COLUMN ai_memory_event.target_id IS '目标ID';
COMMENT ON COLUMN ai_memory_event.caller IS '调用方';
COMMENT ON COLUMN ai_memory_event.purpose IS '调用目的';
COMMENT ON COLUMN ai_memory_event.trace_id IS '调用链追踪ID';
COMMENT ON COLUMN ai_memory_event.user_id IS '操作用户ID';
COMMENT ON COLUMN ai_memory_event.tenant_id IS 'Kellen 内部租户ID';
COMMENT ON COLUMN ai_memory_event.source_tenant_id IS '来源业务租户ID';
COMMENT ON COLUMN ai_memory_event.patient_id IS '患者ID';
COMMENT ON COLUMN ai_memory_event.reason IS '操作原因';
COMMENT ON COLUMN ai_memory_event.result IS '操作结果';
COMMENT ON COLUMN ai_memory_event.detail_json IS '审计详情 JSON';
COMMENT ON COLUMN ai_memory_event.code IS '编码';
COMMENT ON COLUMN ai_memory_event.description IS '说明';
COMMENT ON COLUMN ai_memory_event.create_date_time IS '创建时间';
COMMENT ON COLUMN ai_memory_event.create_name IS '创建人';
COMMENT ON COLUMN ai_memory_event.modify_date_time IS '修改时间';
COMMENT ON COLUMN ai_memory_event.modify_name IS '修改人';
COMMENT ON COLUMN ai_memory_event.is_delete IS '删除状态';
COMMENT ON COLUMN ai_memory_event.label IS '标签';
COMMENT ON COLUMN ai_memory_event.sorting IS '排序';
COMMENT ON COLUMN ai_memory_event.version IS '版本号';

CREATE TABLE IF NOT EXISTS ai_memory_long_fact (
    id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    patient_id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    source_tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    memory_type varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    memory_key varchar(160) COLLATE public.fleet_general_ci NOT NULL,
    title varchar(255) COLLATE public.fleet_general_ci NOT NULL,
    content text COLLATE public.fleet_general_ci NOT NULL,
    facts_json text COLLATE public.fleet_general_ci,
    source_type varchar(64) COLLATE public.fleet_general_ci,
    source_id varchar(128) COLLATE public.fleet_general_ci,
    confidence double precision NOT NULL DEFAULT 0,
    status varchar(32) COLLATE public.fleet_general_ci NOT NULL DEFAULT 'ACTIVE',
    sensitivity_level varchar(32) COLLATE public.fleet_general_ci NOT NULL DEFAULT 'NORMAL',
    authorization_scope varchar(128) COLLATE public.fleet_general_ci,
    embedding_status varchar(32) COLLATE public.fleet_general_ci NOT NULL DEFAULT 'PENDING',
    vector_collection varchar(128) COLLATE public.fleet_general_ci,
    vector_id varchar(128) COLLATE public.fleet_general_ci,
    indexed_at timestamp(0) without time zone,
    occurred_at timestamp(0) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived_at timestamp(0) without time zone,
    masked_at timestamp(0) without time zone,
    deleted_at timestamp(0) without time zone,
    code varchar(255) COLLATE public.fleet_general_ci,
    description varchar(255) COLLATE public.fleet_general_ci,
    create_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    create_name varchar(255) COLLATE public.fleet_general_ci,
    modify_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    modify_name varchar(255) COLLATE public.fleet_general_ci,
    is_delete boolean DEFAULT false,
    label varchar(255) COLLATE public.fleet_general_ci,
    sorting integer DEFAULT 0,
    version integer DEFAULT 1,
    tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL DEFAULT '100',
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_ai_memory_long_fact_embedding ON ai_memory_long_fact (embedding_status, modify_date_time);
CREATE INDEX IF NOT EXISTS idx_ai_memory_long_fact_patient_status ON ai_memory_long_fact (patient_id, source_tenant_id, status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_ai_memory_long_fact_source ON ai_memory_long_fact (source_type, source_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_memory_long_fact_key_status ON ai_memory_long_fact (patient_id, source_tenant_id, memory_key, status);
COMMENT ON TABLE ai_memory_long_fact IS 'AI 患者长期记忆权威事实';
COMMENT ON COLUMN ai_memory_long_fact.id IS '主键ID';
COMMENT ON COLUMN ai_memory_long_fact.patient_id IS '患者ID';
COMMENT ON COLUMN ai_memory_long_fact.source_tenant_id IS '来源业务租户ID';
COMMENT ON COLUMN ai_memory_long_fact.memory_type IS '记忆类型';
COMMENT ON COLUMN ai_memory_long_fact.memory_key IS '同患者同来源下稳定记忆键';
COMMENT ON COLUMN ai_memory_long_fact.title IS '记忆标题';
COMMENT ON COLUMN ai_memory_long_fact.content IS '可注入模型的事实文本';
COMMENT ON COLUMN ai_memory_long_fact.facts_json IS '结构化事实 JSON';
COMMENT ON COLUMN ai_memory_long_fact.source_type IS '来源类型';
COMMENT ON COLUMN ai_memory_long_fact.source_id IS '来源业务ID';
COMMENT ON COLUMN ai_memory_long_fact.confidence IS '事实置信度';
COMMENT ON COLUMN ai_memory_long_fact.status IS '状态 ACTIVE/ARCHIVED/MASKED/DELETED/REVOKED';
COMMENT ON COLUMN ai_memory_long_fact.sensitivity_level IS '敏感级别';
COMMENT ON COLUMN ai_memory_long_fact.authorization_scope IS '授权范围';
COMMENT ON COLUMN ai_memory_long_fact.embedding_status IS '向量同步状态';
COMMENT ON COLUMN ai_memory_long_fact.vector_collection IS 'Qdrant collection';
COMMENT ON COLUMN ai_memory_long_fact.vector_id IS 'Qdrant point ID';
COMMENT ON COLUMN ai_memory_long_fact.indexed_at IS '最近索引时间';
COMMENT ON COLUMN ai_memory_long_fact.occurred_at IS '事实发生时间';
COMMENT ON COLUMN ai_memory_long_fact.archived_at IS '归档时间';
COMMENT ON COLUMN ai_memory_long_fact.masked_at IS '脱敏时间';
COMMENT ON COLUMN ai_memory_long_fact.deleted_at IS '删除或撤销时间';
COMMENT ON COLUMN ai_memory_long_fact.code IS '编码';
COMMENT ON COLUMN ai_memory_long_fact.description IS '说明';
COMMENT ON COLUMN ai_memory_long_fact.create_date_time IS '创建时间';
COMMENT ON COLUMN ai_memory_long_fact.create_name IS '创建人';
COMMENT ON COLUMN ai_memory_long_fact.modify_date_time IS '修改时间';
COMMENT ON COLUMN ai_memory_long_fact.modify_name IS '修改人';
COMMENT ON COLUMN ai_memory_long_fact.is_delete IS '删除状态';
COMMENT ON COLUMN ai_memory_long_fact.label IS '标签';
COMMENT ON COLUMN ai_memory_long_fact.sorting IS '排序';
COMMENT ON COLUMN ai_memory_long_fact.version IS '版本号';
COMMENT ON COLUMN ai_memory_long_fact.tenant_id IS 'Kellen 内部租户ID';

CREATE TABLE IF NOT EXISTS ai_memory_retrieval_eval (
    id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    trace_id varchar(128) COLLATE public.fleet_general_ci,
    caller varchar(64) COLLATE public.fleet_general_ci,
    purpose varchar(128) COLLATE public.fleet_general_ci,
    source_tenant_id varchar(64) COLLATE public.fleet_general_ci,
    patient_id varchar(64) COLLATE public.fleet_general_ci,
    conversation_id varchar(128) COLLATE public.fleet_general_ci,
    query_text varchar(1000) COLLATE public.fleet_general_ci,
    max_items integer,
    max_tokens integer,
    short_hit_count integer NOT NULL DEFAULT 0,
    sql_hit_count integer NOT NULL DEFAULT 0,
    vector_hit_count integer NOT NULL DEFAULT 0,
    returned_count integer NOT NULL DEFAULT 0,
    latency_ms bigint NOT NULL DEFAULT 0,
    result_message varchar(1000) COLLATE public.fleet_general_ci,
    code varchar(255) COLLATE public.fleet_general_ci,
    description varchar(255) COLLATE public.fleet_general_ci,
    create_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    create_name varchar(255) COLLATE public.fleet_general_ci,
    modify_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    modify_name varchar(255) COLLATE public.fleet_general_ci,
    is_delete boolean DEFAULT false,
    label varchar(255) COLLATE public.fleet_general_ci,
    sorting integer DEFAULT 0,
    version integer DEFAULT 1,
    tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL DEFAULT '100',
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_ai_memory_retrieval_eval_caller ON ai_memory_retrieval_eval (caller, purpose, create_date_time);
CREATE INDEX IF NOT EXISTS idx_ai_memory_retrieval_eval_patient_time ON ai_memory_retrieval_eval (patient_id, source_tenant_id, create_date_time);
CREATE INDEX IF NOT EXISTS idx_ai_memory_retrieval_eval_trace ON ai_memory_retrieval_eval (trace_id);
COMMENT ON TABLE ai_memory_retrieval_eval IS 'AI 记忆召回评估记录';
COMMENT ON COLUMN ai_memory_retrieval_eval.id IS '主键ID';
COMMENT ON COLUMN ai_memory_retrieval_eval.trace_id IS '调用链追踪ID';
COMMENT ON COLUMN ai_memory_retrieval_eval.caller IS '调用方';
COMMENT ON COLUMN ai_memory_retrieval_eval.purpose IS '召回目的';
COMMENT ON COLUMN ai_memory_retrieval_eval.source_tenant_id IS '来源业务租户ID';
COMMENT ON COLUMN ai_memory_retrieval_eval.patient_id IS '患者ID';
COMMENT ON COLUMN ai_memory_retrieval_eval.conversation_id IS '会话ID';
COMMENT ON COLUMN ai_memory_retrieval_eval.query_text IS '召回查询文本';
COMMENT ON COLUMN ai_memory_retrieval_eval.max_items IS '最大条数';
COMMENT ON COLUMN ai_memory_retrieval_eval.max_tokens IS '最大 token 预算';
COMMENT ON COLUMN ai_memory_retrieval_eval.short_hit_count IS '短记忆命中数';
COMMENT ON COLUMN ai_memory_retrieval_eval.sql_hit_count IS 'MySQL 长记忆命中数';
COMMENT ON COLUMN ai_memory_retrieval_eval.vector_hit_count IS 'Qdrant 命中数';
COMMENT ON COLUMN ai_memory_retrieval_eval.returned_count IS '返回条数';
COMMENT ON COLUMN ai_memory_retrieval_eval.latency_ms IS '召回耗时毫秒';
COMMENT ON COLUMN ai_memory_retrieval_eval.result_message IS '召回结果摘要';
COMMENT ON COLUMN ai_memory_retrieval_eval.code IS '编码';
COMMENT ON COLUMN ai_memory_retrieval_eval.description IS '说明';
COMMENT ON COLUMN ai_memory_retrieval_eval.create_date_time IS '创建时间';
COMMENT ON COLUMN ai_memory_retrieval_eval.create_name IS '创建人';
COMMENT ON COLUMN ai_memory_retrieval_eval.modify_date_time IS '修改时间';
COMMENT ON COLUMN ai_memory_retrieval_eval.modify_name IS '修改人';
COMMENT ON COLUMN ai_memory_retrieval_eval.is_delete IS '删除状态';
COMMENT ON COLUMN ai_memory_retrieval_eval.label IS '标签';
COMMENT ON COLUMN ai_memory_retrieval_eval.sorting IS '排序';
COMMENT ON COLUMN ai_memory_retrieval_eval.version IS '版本号';
COMMENT ON COLUMN ai_memory_retrieval_eval.tenant_id IS 'Kellen 内部租户ID';

CREATE TABLE IF NOT EXISTS ai_memory_short_message (
    id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    conversation_id varchar(128) COLLATE public.fleet_general_ci NOT NULL,
    source_tenant_id varchar(64) COLLATE public.fleet_general_ci,
    patient_id varchar(64) COLLATE public.fleet_general_ci,
    user_id varchar(64) COLLATE public.fleet_general_ci,
    caller varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    message_role varchar(24) COLLATE public.fleet_general_ci NOT NULL,
    content text COLLATE public.fleet_general_ci NOT NULL,
    token_estimate integer,
    occurred_at timestamp(0) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    code varchar(255) COLLATE public.fleet_general_ci,
    description varchar(255) COLLATE public.fleet_general_ci,
    create_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    create_name varchar(255) COLLATE public.fleet_general_ci,
    modify_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    modify_name varchar(255) COLLATE public.fleet_general_ci,
    is_delete boolean DEFAULT false,
    label varchar(255) COLLATE public.fleet_general_ci,
    sorting integer DEFAULT 0,
    version integer DEFAULT 1,
    tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL DEFAULT '100',
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_ai_memory_short_message_conversation_time ON ai_memory_short_message (conversation_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_ai_memory_short_message_patient ON ai_memory_short_message (patient_id, source_tenant_id, occurred_at);
COMMENT ON TABLE ai_memory_short_message IS 'AI 短期会话记忆窗口';
COMMENT ON COLUMN ai_memory_short_message.id IS '主键ID';
COMMENT ON COLUMN ai_memory_short_message.conversation_id IS '会话ID';
COMMENT ON COLUMN ai_memory_short_message.source_tenant_id IS '来源业务租户ID';
COMMENT ON COLUMN ai_memory_short_message.patient_id IS '患者ID';
COMMENT ON COLUMN ai_memory_short_message.user_id IS '调用方用户ID';
COMMENT ON COLUMN ai_memory_short_message.caller IS '调用方';
COMMENT ON COLUMN ai_memory_short_message.message_role IS '消息角色 user/assistant/system';
COMMENT ON COLUMN ai_memory_short_message.content IS '短期记忆原文';
COMMENT ON COLUMN ai_memory_short_message.token_estimate IS '估算 token 数';
COMMENT ON COLUMN ai_memory_short_message.occurred_at IS '消息发生时间';
COMMENT ON COLUMN ai_memory_short_message.code IS '编码';
COMMENT ON COLUMN ai_memory_short_message.description IS '说明';
COMMENT ON COLUMN ai_memory_short_message.create_date_time IS '创建时间';
COMMENT ON COLUMN ai_memory_short_message.create_name IS '创建人';
COMMENT ON COLUMN ai_memory_short_message.modify_date_time IS '修改时间';
COMMENT ON COLUMN ai_memory_short_message.modify_name IS '修改人';
COMMENT ON COLUMN ai_memory_short_message.is_delete IS '删除状态';
COMMENT ON COLUMN ai_memory_short_message.label IS '标签';
COMMENT ON COLUMN ai_memory_short_message.sorting IS '排序';
COMMENT ON COLUMN ai_memory_short_message.version IS '版本号';
COMMENT ON COLUMN ai_memory_short_message.tenant_id IS 'Kellen 内部租户ID';

CREATE TABLE IF NOT EXISTS ai_memory_short_summary (
    id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    conversation_id varchar(128) COLLATE public.fleet_general_ci NOT NULL,
    source_tenant_id varchar(64) COLLATE public.fleet_general_ci,
    patient_id varchar(64) COLLATE public.fleet_general_ci,
    summary_text text COLLATE public.fleet_general_ci NOT NULL,
    summary_version integer NOT NULL DEFAULT 1,
    summarized_at timestamp(0) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    code varchar(255) COLLATE public.fleet_general_ci,
    description varchar(255) COLLATE public.fleet_general_ci,
    create_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    create_name varchar(255) COLLATE public.fleet_general_ci,
    modify_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    modify_name varchar(255) COLLATE public.fleet_general_ci,
    is_delete boolean DEFAULT false,
    label varchar(255) COLLATE public.fleet_general_ci,
    sorting integer DEFAULT 0,
    version integer DEFAULT 1,
    tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL DEFAULT '100',
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_ai_memory_short_summary_patient ON ai_memory_short_summary (patient_id, source_tenant_id, summarized_at);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_memory_short_summary_conversation ON ai_memory_short_summary (conversation_id);
COMMENT ON TABLE ai_memory_short_summary IS 'AI 短期会话滚动摘要';
COMMENT ON COLUMN ai_memory_short_summary.id IS '主键ID';
COMMENT ON COLUMN ai_memory_short_summary.conversation_id IS '会话ID';
COMMENT ON COLUMN ai_memory_short_summary.source_tenant_id IS '来源业务租户ID';
COMMENT ON COLUMN ai_memory_short_summary.patient_id IS '患者ID';
COMMENT ON COLUMN ai_memory_short_summary.summary_text IS '会话滚动摘要';
COMMENT ON COLUMN ai_memory_short_summary.summary_version IS '摘要版本';
COMMENT ON COLUMN ai_memory_short_summary.summarized_at IS '摘要更新时间';
COMMENT ON COLUMN ai_memory_short_summary.code IS '编码';
COMMENT ON COLUMN ai_memory_short_summary.description IS '说明';
COMMENT ON COLUMN ai_memory_short_summary.create_date_time IS '创建时间';
COMMENT ON COLUMN ai_memory_short_summary.create_name IS '创建人';
COMMENT ON COLUMN ai_memory_short_summary.modify_date_time IS '修改时间';
COMMENT ON COLUMN ai_memory_short_summary.modify_name IS '修改人';
COMMENT ON COLUMN ai_memory_short_summary.is_delete IS '删除状态';
COMMENT ON COLUMN ai_memory_short_summary.label IS '标签';
COMMENT ON COLUMN ai_memory_short_summary.sorting IS '排序';
COMMENT ON COLUMN ai_memory_short_summary.version IS '版本号';
COMMENT ON COLUMN ai_memory_short_summary.tenant_id IS 'Kellen 内部租户ID';

CREATE TABLE IF NOT EXISTS ai_memory_vector_task (
    id varchar(64) COLLATE public.fleet_general_ci NOT NULL,
    task_type varchar(32) COLLATE public.fleet_general_ci NOT NULL,
    memory_id varchar(64) COLLATE public.fleet_general_ci,
    patient_id varchar(64) COLLATE public.fleet_general_ci,
    source_tenant_id varchar(64) COLLATE public.fleet_general_ci,
    collection_name varchar(128) COLLATE public.fleet_general_ci,
    vector_id varchar(128) COLLATE public.fleet_general_ci,
    payload_json text COLLATE public.fleet_general_ci,
    status varchar(32) COLLATE public.fleet_general_ci NOT NULL DEFAULT 'PENDING',
    attempts integer NOT NULL DEFAULT 0,
    next_retry_at timestamp(0) without time zone,
    error_message varchar(1000) COLLATE public.fleet_general_ci,
    code varchar(255) COLLATE public.fleet_general_ci,
    description varchar(255) COLLATE public.fleet_general_ci,
    create_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    create_name varchar(255) COLLATE public.fleet_general_ci,
    modify_date_time timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    modify_name varchar(255) COLLATE public.fleet_general_ci,
    is_delete boolean DEFAULT false,
    label varchar(255) COLLATE public.fleet_general_ci,
    sorting integer DEFAULT 0,
    version integer DEFAULT 1,
    tenant_id varchar(64) COLLATE public.fleet_general_ci NOT NULL DEFAULT '100',
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_ai_memory_vector_task_memory ON ai_memory_vector_task (memory_id, status);
CREATE INDEX IF NOT EXISTS idx_ai_memory_vector_task_patient ON ai_memory_vector_task (patient_id, source_tenant_id, task_type);
CREATE INDEX IF NOT EXISTS idx_ai_memory_vector_task_status ON ai_memory_vector_task (status, next_retry_at, create_date_time);
COMMENT ON TABLE ai_memory_vector_task IS 'AI 记忆 Qdrant 派生索引任务';
COMMENT ON COLUMN ai_memory_vector_task.id IS '主键ID';
COMMENT ON COLUMN ai_memory_vector_task.task_type IS '任务类型 UPSERT/DELETE/REBUILD';
COMMENT ON COLUMN ai_memory_vector_task.memory_id IS '长期记忆ID';
COMMENT ON COLUMN ai_memory_vector_task.patient_id IS '患者ID';
COMMENT ON COLUMN ai_memory_vector_task.source_tenant_id IS '来源业务租户ID';
COMMENT ON COLUMN ai_memory_vector_task.collection_name IS 'Qdrant collection';
COMMENT ON COLUMN ai_memory_vector_task.vector_id IS 'Qdrant point ID';
COMMENT ON COLUMN ai_memory_vector_task.payload_json IS '派生索引 payload';
COMMENT ON COLUMN ai_memory_vector_task.status IS '任务状态';
COMMENT ON COLUMN ai_memory_vector_task.attempts IS '执行次数';
COMMENT ON COLUMN ai_memory_vector_task.next_retry_at IS '下次重试时间';
COMMENT ON COLUMN ai_memory_vector_task.error_message IS '错误信息';
COMMENT ON COLUMN ai_memory_vector_task.code IS '编码';
COMMENT ON COLUMN ai_memory_vector_task.description IS '说明';
COMMENT ON COLUMN ai_memory_vector_task.create_date_time IS '创建时间';
COMMENT ON COLUMN ai_memory_vector_task.create_name IS '创建人';
COMMENT ON COLUMN ai_memory_vector_task.modify_date_time IS '修改时间';
COMMENT ON COLUMN ai_memory_vector_task.modify_name IS '修改人';
COMMENT ON COLUMN ai_memory_vector_task.is_delete IS '删除状态';
COMMENT ON COLUMN ai_memory_vector_task.label IS '标签';
COMMENT ON COLUMN ai_memory_vector_task.sorting IS '排序';
COMMENT ON COLUMN ai_memory_vector_task.version IS '版本号';
COMMENT ON COLUMN ai_memory_vector_task.tenant_id IS 'Kellen 内部租户ID';
