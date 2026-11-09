package com.kellen.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.kellen.memory.mapper.MemoryShortMessageMapper;
import com.kellen.memory.mapper.MemoryShortSummaryMapper;
import com.kellen.memory.mapper.MemoryVectorTaskMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import static org.assertj.core.api.Assertions.assertThat;

/** PostgreSQL 实际执行、重复建表及布尔/时间/字符比较回归，不连接真实业务库。 */
@EnabledIfEnvironmentVariable(named = "FLEET_PG_TEST_URL", matches = ".+")
class PostgresqlSchemaLiveTest extends PostgresqlSchemaFixture {

    @Test
    void bulkSoftDeleteAndCancellationMaintainTimestampsWithoutDatabaseTriggers() throws Exception {
        script("db/postgresql/ai-memory-schema.sql");
        execute("INSERT INTO ai_memory_vector_task(id, task_type, memory_id, modify_date_time) VALUES ('fixture-task', 'UPSERT', 'fixture-memory', TIMESTAMP '2000-01-01 00:00:00')");
        execute("INSERT INTO ai_memory_short_message(id, conversation_id, caller, message_role, content, modify_date_time) VALUES ('fixture-message', 'fixture-conversation', 'fixture', 'user', 'synthetic', TIMESTAMP '2000-01-01 00:00:00')");
        execute("INSERT INTO ai_memory_short_summary(id, conversation_id, summary_text, modify_date_time) VALUES ('fixture-summary', 'fixture-conversation', 'synthetic', TIMESTAMP '2000-01-01 00:00:00')");
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(MemoryVectorTaskMapper.class);
        configuration.addMapper(MemoryShortMessageMapper.class);
        configuration.addMapper(MemoryShortSummaryMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setConfiguration(configuration);
        factory.setDataSource(new SingleConnectionDataSource(connection, true));
        try (var session = factory.getObject().openSession(true)) {
            session.getMapper(MemoryVectorTaskMapper.class).cancelPendingByMemory("fixture-memory");
            session.getMapper(MemoryShortMessageMapper.class).deleteByConversation("fixture-conversation");
            session.getMapper(MemoryShortSummaryMapper.class).deleteByConversation("fixture-conversation");
        }
        assertThat(number("SELECT count(*) FROM ai_memory_vector_task WHERE id = 'fixture-task' AND status = 'CANCELLED' AND modify_date_time > TIMESTAMP '2020-01-01 00:00:00'"))
                .isEqualTo(1);
        assertThat(number("SELECT count(*) FROM ai_memory_short_message WHERE id = 'fixture-message' AND is_delete = true AND modify_date_time > TIMESTAMP '2020-01-01 00:00:00'"))
                .isEqualTo(1);
        assertThat(number("SELECT count(*) FROM ai_memory_short_summary WHERE id = 'fixture-summary' AND is_delete = true AND modify_date_time > TIMESTAMP '2020-01-01 00:00:00'"))
                .isEqualTo(1);
    }
    @Test
    void createsTheCurrentBusinessSchemaTwiceWithoutMysqlSyntax() throws Exception {
        script("db/postgresql/ai-memory-schema.sql");
        script("db/postgresql/ai-memory-schema.sql");
        assertThat(number("SELECT count(*) FROM information_schema.tables WHERE table_schema = '" + schema + "' AND table_type = 'BASE TABLE'"))
                .isEqualTo(7);
        assertThat(number("SELECT count(*) FROM information_schema.columns WHERE table_schema = '" + schema + "' AND column_name = 'is_delete' AND data_type = 'boolean'"))
                .isEqualTo(7);
        assertThat(number("SELECT count(*) FROM information_schema.columns WHERE table_schema = '" + schema + "' AND column_name = 'modify_date_time' AND data_type = 'timestamp without time zone'"))
                .isEqualTo(7);
        assertThat(number("SELECT CASE WHEN 'Café' COLLATE public.fleet_general_ci = 'CAFE' THEN 1 ELSE 0 END")).isEqualTo(1);
        assertThat(number("SELECT CASE WHEN 'Café' COLLATE public.fleet_general_ci LIKE 'caf%' THEN 1 ELSE 0 END")).isEqualTo(1);
    }
}
