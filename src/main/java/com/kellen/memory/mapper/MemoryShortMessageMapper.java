package com.kellen.memory.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kellen.memory.entity.MemoryShortMessageEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 短期会话消息 Mapper。
 */
@Mapper
public interface MemoryShortMessageMapper extends MemoryBaseMapper<MemoryShortMessageEntity> {

    default List<MemoryShortMessageEntity> findRecent(String conversationId, int limit) {
        return selectList(new QueryWrapper<MemoryShortMessageEntity>()
                .eq("conversation_id", conversationId)
                .orderByDesc("occurred_at")
                .last("LIMIT " + Math.max(1, limit)));
    }

    default List<MemoryShortMessageEntity> findOldest(String conversationId, int limit) {
        return selectList(new QueryWrapper<MemoryShortMessageEntity>()
                .eq("conversation_id", conversationId)
                .orderByAsc("occurred_at")
                .last("LIMIT " + Math.max(1, limit)));
    }

    default Long countByConversation(String conversationId) {
        return selectCount(new QueryWrapper<MemoryShortMessageEntity>().eq("conversation_id", conversationId));
    }

    default void deleteByConversation(String conversationId) {
        delete(new QueryWrapper<MemoryShortMessageEntity>().eq("conversation_id", conversationId));
    }
}
