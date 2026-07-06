package com.kellen.memory.mapper;

import com.kellen.memory.entity.MemoryEventEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记忆事件 Mapper。
 */
@Mapper
public interface MemoryEventMapper extends MemoryBaseMapper<MemoryEventEntity> {
}
