package com.kellen.memory.mapper;

import com.kellen.memory.entity.MemoryCandidateEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 候选记忆 Mapper。
 */
@Mapper
public interface MemoryCandidateMapper extends MemoryBaseMapper<MemoryCandidateEntity> {
}
