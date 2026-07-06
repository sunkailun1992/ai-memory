package com.kellen.memory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kellen.entity.EntityBase;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * AI 会话短期原文窗口消息。
 */
@Getter
@Setter
@TableName("ai_memory_short_message")
public class MemoryShortMessageEntity extends EntityBase {

    private String conversationId;

    private String sourceTenantId;

    private String patientId;

    private String userId;

    private String caller;

    private String messageRole;

    private String content;

    private Integer tokenEstimate;

    private LocalDateTime occurredAt;

    public static MemoryShortMessageEntity create(String conversationId,
                                                  String sourceTenantId,
                                                  String patientId,
                                                  String userId,
                                                  String caller,
                                                  String messageRole,
                                                  String content,
                                                  Integer tokenEstimate,
                                                  LocalDateTime occurredAt) {
        MemoryShortMessageEntity entity = new MemoryShortMessageEntity();
        entity.conversationId = conversationId;
        entity.sourceTenantId = sourceTenantId;
        entity.patientId = patientId;
        entity.userId = userId;
        entity.caller = caller;
        entity.messageRole = messageRole;
        entity.content = content;
        entity.tokenEstimate = tokenEstimate;
        entity.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        return entity;
    }
}
