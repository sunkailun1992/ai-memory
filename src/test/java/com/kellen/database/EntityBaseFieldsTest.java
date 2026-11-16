package com.kellen.database;

import com.kellen.entity.EntityBase;
import com.kellen.entity.TenantEntityBase;
import com.kellen.memory.entity.MemoryCandidateEntity;
import com.kellen.memory.entity.MemoryEventEntity;
import com.kellen.memory.entity.MemoryLongFactEntity;
import com.kellen.memory.entity.MemoryRetrievalEvalEntity;
import com.kellen.memory.entity.MemoryShortMessageEntity;
import com.kellen.memory.entity.MemoryShortSummaryEntity;
import com.kellen.memory.entity.MemoryVectorTaskEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** 7 个记忆实体直接继承租户基类，基类已有的公共字段（含 id、version、code、description、label、sorting）不在实体里重复声明。 */
class EntityBaseFieldsTest {

    static final List<Class<? extends TenantEntityBase>> ENTITIES = List.of(MemoryCandidateEntity.class, MemoryEventEntity.class, MemoryLongFactEntity.class, MemoryRetrievalEvalEntity.class, MemoryShortMessageEntity.class, MemoryShortSummaryEntity.class, MemoryVectorTaskEntity.class);

    @Test
    void entitiesExtendTenantBaseDirectlyWithoutRedeclaringBaseFields() {
        Set<String> base = Stream.of(EntityBase.class, TenantEntityBase.class)
                .flatMap(type -> Arrays.stream(type.getDeclaredFields()))
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(Field::getName)
                .collect(Collectors.toSet());
        for (Class<?> entity : ENTITIES) {
            assertThat(entity.getSuperclass()).as(entity.getSimpleName()).isEqualTo(TenantEntityBase.class);
            assertThat(Arrays.stream(entity.getDeclaredFields()).map(Field::getName))
                    .as(entity.getSimpleName()).doesNotContainAnyElementsOf(base);
        }
    }
}
