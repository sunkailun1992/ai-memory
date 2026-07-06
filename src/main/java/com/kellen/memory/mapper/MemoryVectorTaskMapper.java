package com.kellen.memory.mapper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kellen.memory.entity.MemoryVectorTaskEntity;
import com.kellen.memory.entity.enums.MemoryVectorTaskStatusEnum;
import org.apache.ibatis.annotations.Mapper;

/**
 * 向量索引任务 Mapper。
 */
@Mapper
public interface MemoryVectorTaskMapper extends MemoryBaseMapper<MemoryVectorTaskEntity> {

    default void cancelPendingByMemory(String memoryId) {
        update(null, new UpdateWrapper<MemoryVectorTaskEntity>()
                .eq("memory_id", memoryId)
                .eq("status", MemoryVectorTaskStatusEnum.PENDING.code())
                .set("status", MemoryVectorTaskStatusEnum.CANCELLED.code()));
    }
}
