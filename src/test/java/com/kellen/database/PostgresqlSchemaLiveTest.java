package com.kellen.database;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.kellen.memory.mapper.MemoryShortMessageMapper;
import com.kellen.memory.mapper.MemoryShortSummaryMapper;
import com.kellen.memory.mapper.MemoryVectorTaskMapper;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 服务自己的 Flyway 迁移在独占 schema 上实际执行，并回归布尔、时间与大小写不敏感比较；不连接真实业务库。 */
@EnabledIfEnvironmentVariable(named = "FLEET_PG_TEST_URL", matches = ".+")
class PostgresqlSchemaLiveTest extends PostgresqlSchemaFixture {

    private static final List<String> TABLES = List.of("ai_memory_candidate", "ai_memory_event", "ai_memory_long_fact",
            "ai_memory_retrieval_eval", "ai_memory_short_message", "ai_memory_short_summary", "ai_memory_vector_task");

    @Test
    void migratesEmptySchemaToLatestVersion() throws Exception {
        assertThat(migrate(null).success).isTrue();
        assertThat(appliedVersions()).containsExactly("1", "2", "3", "4");
        assertThat(strings("SELECT table_name FROM information_schema.tables WHERE table_schema = '" + schema
                + "' AND table_type = 'BASE TABLE' AND table_name NOT IN ('undo_log', 'flyway_schema_history') ORDER BY 1"))
                .containsExactlyElementsOf(TABLES);
        for (String table : TABLES) {
            assertThat(columns(table)).as(table)
                    .contains("id", "create_time", "update_time", "creator", "updater", "is_deleted", "tenant_id", "version")
                    .doesNotContain("create_date_time", "modify_date_time", "create_name", "modify_name", "is_delete",
                            "code", "description", "label", "sorting");
        }
        assertThat(number("SELECT count(*) FROM information_schema.tables WHERE table_schema = '" + schema
                + "' AND table_name = 'ddl_history'")).isZero();
        assertThat(number("SELECT count(*) FROM information_schema.tables WHERE table_schema = '" + schema
                + "' AND table_name = 'undo_log'")).isEqualTo(1);
    }

    @Test
    void dropUnusedColumnsRefusesWhenAColumnStillHasData() throws Exception {
        migrate("3");
        execute("INSERT INTO ai_memory_short_summary(id, conversation_id, summary_text, label) VALUES ('fixture-labelled', 'fixture-conversation', 'synthetic', 'kept')");
        assertThatThrownBy(() -> migrate(null)).isInstanceOf(FlywayException.class);
        assertThat(appliedVersions()).containsExactly("1", "2", "3");
        assertThat(columns("ai_memory_short_summary")).contains("label", "code", "description", "sorting");
    }

    @Test
    void bulkSoftDeleteAndCancellationMaintainTimestampsWithoutDatabaseTriggers() throws Exception {
        migrate(null);
        execute("INSERT INTO ai_memory_vector_task(id, task_type, memory_id, update_time) VALUES ('fixture-task', 'UPSERT', 'fixture-memory', TIMESTAMP '2000-01-01 00:00:00')");
        execute("INSERT INTO ai_memory_short_message(id, conversation_id, caller, message_role, content, update_time) VALUES ('fixture-message', 'fixture-conversation', 'fixture', 'user', 'synthetic', TIMESTAMP '2000-01-01 00:00:00')");
        execute("INSERT INTO ai_memory_short_summary(id, conversation_id, summary_text, update_time) VALUES ('fixture-summary', 'fixture-conversation', 'synthetic', TIMESTAMP '2000-01-01 00:00:00')");
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
        assertThat(number("SELECT count(*) FROM ai_memory_vector_task WHERE id = 'fixture-task' AND status = 'CANCELLED' AND update_time > TIMESTAMP '2020-01-01 00:00:00'"))
                .isEqualTo(1);
        assertThat(number("SELECT count(*) FROM ai_memory_short_message WHERE id = 'fixture-message' AND is_deleted = true AND update_time > TIMESTAMP '2020-01-01 00:00:00'"))
                .isEqualTo(1);
        assertThat(number("SELECT count(*) FROM ai_memory_short_summary WHERE id = 'fixture-summary' AND is_deleted = true AND update_time > TIMESTAMP '2020-01-01 00:00:00'"))
                .isEqualTo(1);
    }

    @Test
    void secondMigrationRunIsANoOpAndKeepsPostgresqlTypes() throws Exception {
        migrate(null);
        assertThat(migrate(null).migrationsExecuted).isZero();
        assertThat(number("SELECT count(*) FROM information_schema.columns WHERE table_schema = '" + schema + "' AND column_name = 'is_deleted' AND data_type = 'boolean'"))
                .isEqualTo(7);
        assertThat(number("SELECT count(*) FROM information_schema.columns WHERE table_schema = '" + schema + "' AND column_name = 'update_time' AND data_type = 'timestamp without time zone'"))
                .isEqualTo(7);
        assertThat(number("SELECT CASE WHEN 'Café' COLLATE fleet_general_ci = 'CAFE' THEN 1 ELSE 0 END")).isEqualTo(1);
        assertThat(number("SELECT CASE WHEN 'Café' COLLATE fleet_general_ci LIKE 'caf%' THEN 1 ELSE 0 END")).isEqualTo(1);
    }
}
