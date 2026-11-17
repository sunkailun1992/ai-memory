package com.kellen.database;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.kellen.memory.mapper.MemoryShortMessageMapper;
import com.kellen.memory.mapper.MemoryShortSummaryMapper;
import com.kellen.memory.mapper.MemoryVectorTaskMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 服务自己的 Flyway 迁移在独占 schema 上实际执行，并回归布尔、时间与大小写不敏感比较；不连接真实业务库。 */
@EnabledIfEnvironmentVariable(named = "FLEET_PG_TEST_URL", matches = ".+")
class PostgresqlSchemaLiveTest extends PostgresqlSchemaFixture {

    private static final List<String> TABLES = List.of("ai_memory_candidate", "ai_memory_event", "ai_memory_long_fact",
            "ai_memory_retrieval_eval", "ai_memory_short_message", "ai_memory_short_summary", "ai_memory_vector_task");

    /** 每张表都必须有的全部基础字段；undo_log 与 Flyway 记录表是框架表，除外。 */
    private static final List<String> BASE_COLUMNS = List.of("id", "code", "description", "create_time", "create_by",
            "update_time", "update_by", "is_deleted", "label", "sorting", "version", "tenant_id", "create_dept");

    @Test
    void everyTableHasAllBaseColumns() throws Exception {
        migrate(null);
        List<String> tables = strings("SELECT table_name FROM information_schema.tables WHERE table_schema = '" + schema
                + "' AND table_type = 'BASE TABLE' AND table_name NOT IN ('undo_log', 'flyway_schema_history') ORDER BY 1");
        assertThat(tables).isNotEmpty();
        for (String table : tables) {
            assertThat(columns(table)).as(table).containsAll(BASE_COLUMNS)
                    .doesNotContain("creator", "updater", "create_name", "modify_name", "create_date_time", "modify_date_time");
        }
    }

    @Test
    void migratesEmptySchemaToLatestVersion() throws Exception {
        assertThat(migrate(null).success).isTrue();
        assertThat(appliedVersions()).containsExactly("1", "2", "3", "4", "5");
        assertThat(strings("SELECT table_name FROM information_schema.tables WHERE table_schema = '" + schema
                + "' AND table_type = 'BASE TABLE' AND table_name NOT IN ('undo_log', 'flyway_schema_history') ORDER BY 1"))
                .containsExactlyElementsOf(TABLES);
        for (String table : TABLES) {
            assertThat(columns(table)).as(table)
                    .contains("id", "code", "description", "create_time", "create_by", "update_time", "update_by", "is_deleted",
                            "label", "sorting", "version", "tenant_id", "create_dept")
                    .doesNotContain("create_date_time", "modify_date_time", "create_name", "modify_name", "is_delete", "creator", "updater");
        }
        assertThat(number("SELECT count(*) FROM information_schema.columns WHERE table_schema = '" + schema
                + "' AND column_name = 'create_dept' AND data_type = 'character varying' AND character_maximum_length = 64"
                + " AND is_nullable = 'YES'")).isEqualTo(TABLES.size());
        assertThat(number("SELECT count(*) FROM information_schema.tables WHERE table_schema = '" + schema
                + "' AND table_name = 'ddl_history'")).isZero();
        assertThat(number("SELECT count(*) FROM information_schema.tables WHERE table_schema = '" + schema
                + "' AND table_name = 'undo_log'")).isEqualTo(1);
    }

    @Test
    void createDeptIsAddedWithoutDroppingAnyColumnOrRow() throws Exception {
        migrate("3");
        execute("INSERT INTO ai_memory_short_summary(id, conversation_id, summary_text, label, sorting) VALUES ('fixture-kept', 'fixture-conversation', 'synthetic', 'kept', 7)");
        migrate(null);
        assertThat(appliedVersions()).containsExactly("1", "2", "3", "4", "5");
        assertThat(number("SELECT count(*) FROM ai_memory_short_summary WHERE id = 'fixture-kept' AND label = 'kept' AND sorting = 7"
                + " AND create_dept IS NULL")).isEqualTo(1);
        assertThat(strings("SELECT col_description(('" + schema + ".ai_memory_short_summary')::regclass, ordinal_position::int)"
                + " FROM information_schema.columns WHERE table_schema = '" + schema
                + "' AND table_name = 'ai_memory_short_summary' AND column_name = 'create_dept'")).containsExactly("创建部门ID");
    }

    @Test
    void everyEntityFieldMapsToAColumnOfItsTable() throws Exception {
        migrate(null);
        var assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "entity-columns");
        for (Class<?> entity : EntityBaseFieldsTest.ENTITIES) {
            TableInfo info = TableInfoHelper.initTableInfo(assistant, entity);
            List<String> mapped = new ArrayList<>(List.of(info.getKeyColumn()));
            info.getFieldList().forEach(field -> mapped.add(field.getColumn()));
            assertThat(columns(info.getTableName())).as(entity.getSimpleName()).containsAll(mapped);
        }
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
