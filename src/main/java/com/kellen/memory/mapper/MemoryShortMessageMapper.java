package com.kellen.memory.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kellen.memory.entity.MemoryShortMessageEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.time.LocalDateTime;

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
        // 批量更新没有实体填充回调；显式维护原有软删和修改时间语义。
        update(null, new UpdateWrapper<MemoryShortMessageEntity>()
                .eq("conversation_id", conversationId)
                .set("is_delete", true)
                .set("modify_date_time", LocalDateTime.now()));
    }
}
