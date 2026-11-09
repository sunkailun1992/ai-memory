package com.kellen.memory.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kellen.memory.entity.MemoryShortSummaryEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;
import java.time.LocalDateTime;

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
        // 批量更新没有实体填充回调；显式维护原有软删和修改时间语义。
        update(null, new UpdateWrapper<MemoryShortSummaryEntity>()
                .eq("conversation_id", conversationId)
                .set("is_delete", true)
                .set("modify_date_time", LocalDateTime.now()));
    }
}
