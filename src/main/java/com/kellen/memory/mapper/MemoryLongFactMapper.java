package com.kellen.memory.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kellen.memory.entity.MemoryLongFactEntity;
import com.kellen.memory.entity.enums.MemoryStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 长期患者事实 Mapper。
 */
@Mapper
public interface MemoryLongFactMapper extends MemoryBaseMapper<MemoryLongFactEntity> {

    default Optional<MemoryLongFactEntity> findCurrentByPatientAndKey(String patientId,
                                                                      String sourceTenantId,
                                                                      String memoryKey) {
        return Optional.ofNullable(selectOne(new QueryWrapper<MemoryLongFactEntity>()
                .eq("patient_id", patientId)
                .eq("source_tenant_id", normalize(sourceTenantId))
                .eq("memory_key", memoryKey)
                .ne("status", MemoryStatusEnum.DELETED.code())
                .ne("status", MemoryStatusEnum.REVOKED.code())));
    }

    default List<MemoryLongFactEntity> findRecallable(String patientId,
                                                      String sourceTenantId,
                                                      String query,
                                                      boolean includeArchived,
                                                      int limit) {
        QueryWrapper<MemoryLongFactEntity> wrapper = new QueryWrapper<MemoryLongFactEntity>()
                .eq("patient_id", patientId)
                .eq("source_tenant_id", normalize(sourceTenantId))
                .ne("status", MemoryStatusEnum.DELETED.code())
                .ne("status", MemoryStatusEnum.REVOKED.code())
                .orderByDesc("occurred_at");
        if (!includeArchived) {
            wrapper.ne("status", MemoryStatusEnum.ARCHIVED.code());
        }
        if (StringUtils.hasText(query)) {
            String like = query.trim();
            wrapper.and(item -> item.like("title", like).or().like("content", like).or().like("memory_type", like));
        }
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }

    default List<MemoryLongFactEntity> findActiveForRebuild(String patientId,
                                                            String sourceTenantId,
                                                            String memoryId,
                                                            int limit) {
        QueryWrapper<MemoryLongFactEntity> wrapper = new QueryWrapper<MemoryLongFactEntity>()
                .eq("status", MemoryStatusEnum.ACTIVE.code())
                .orderByAsc("id");
        if (StringUtils.hasText(patientId)) {
            wrapper.eq("patient_id", patientId.trim());
        }
        if (StringUtils.hasText(sourceTenantId)) {
            wrapper.eq("source_tenant_id", sourceTenantId.trim());
        }
        if (StringUtils.hasText(memoryId)) {
            wrapper.eq("id", memoryId.trim());
        }
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }

    private static String normalize(String sourceTenantId) {
        return sourceTenantId == null ? "" : sourceTenantId.trim();
    }
}
