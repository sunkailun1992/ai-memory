-- 删除没人读写的列：7 张表的 code、description、label、sorting，服务代码、手写 SQL、接口、rpc-api 与 admin-web 都不用它们。
-- 删之前逐列核对：整列只有 NULL 或列默认值才删；只要有一行别的值就报错，整版回滚，服务停止启动，留给人工判断。
DO $$
DECLARE
    target record;
    column_default text;
    remaining bigint;
BEGIN
    FOR target IN
        SELECT * FROM (VALUES
            ('ai_memory_candidate', 'code'),
            ('ai_memory_candidate', 'description'),
            ('ai_memory_candidate', 'label'),
            ('ai_memory_candidate', 'sorting'),
            ('ai_memory_event', 'code'),
            ('ai_memory_event', 'description'),
            ('ai_memory_event', 'label'),
            ('ai_memory_event', 'sorting'),
            ('ai_memory_long_fact', 'code'),
            ('ai_memory_long_fact', 'description'),
            ('ai_memory_long_fact', 'label'),
            ('ai_memory_long_fact', 'sorting'),
            ('ai_memory_retrieval_eval', 'code'),
            ('ai_memory_retrieval_eval', 'description'),
            ('ai_memory_retrieval_eval', 'label'),
            ('ai_memory_retrieval_eval', 'sorting'),
            ('ai_memory_short_message', 'code'),
            ('ai_memory_short_message', 'description'),
            ('ai_memory_short_message', 'label'),
            ('ai_memory_short_message', 'sorting'),
            ('ai_memory_short_summary', 'code'),
            ('ai_memory_short_summary', 'description'),
            ('ai_memory_short_summary', 'label'),
            ('ai_memory_short_summary', 'sorting'),
            ('ai_memory_vector_task', 'code'),
            ('ai_memory_vector_task', 'description'),
            ('ai_memory_vector_task', 'label'),
            ('ai_memory_vector_task', 'sorting')
        ) AS drops(table_name, column_name)
    LOOP
        SELECT c.column_default INTO column_default
        FROM information_schema.columns c
        WHERE c.table_schema = current_schema() AND c.table_name = target.table_name AND c.column_name = target.column_name;
        IF NOT FOUND THEN
            CONTINUE;
        END IF;
        EXECUTE format('SELECT count(*) FROM %I WHERE %I IS NOT NULL%s', target.table_name, target.column_name,
                CASE WHEN column_default IS NULL THEN ''
                     ELSE format(' AND %I IS DISTINCT FROM (%s)', target.column_name, column_default) END)
            INTO remaining;
        IF remaining > 0 THEN
            RAISE EXCEPTION '%.% 还有 % 行数据，不能删除', target.table_name, target.column_name, remaining;
        END IF;
        EXECUTE format('ALTER TABLE %I DROP COLUMN %I', target.table_name, target.column_name);
    END LOOP;
END
$$;
