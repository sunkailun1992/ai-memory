package com.kellen.memory.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kellen.memory.entity.MemoryShortSummaryEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 短期会话摘要 Mapper。
 */
@Mapper
public interface MemoryShortSummaryMapper extends MemoryBaseMapper<MemoryShortSummaryEntity> {

    default Optional<MemoryShortSummaryEntity> findByConversation(String conversationId) {
        return Optional.ofNullable(selectOne(new QueryWrapper<MemoryShortSummaryEntity>()
                .eq("conversation_id", conversationId)));
    }

    default void deleteByConversation(String conversationId) {
        delete(new QueryWrapper<MemoryShortSummaryEntity>().eq("conversation_id", conversationId));
    }
}
