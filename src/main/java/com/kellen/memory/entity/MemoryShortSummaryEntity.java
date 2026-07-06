package com.kellen.memory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kellen.entity.EntityBase;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * AI 会话短期滚动摘要。
 */
@Getter
@Setter
@TableName("ai_memory_short_summary")
public class MemoryShortSummaryEntity extends EntityBase {

    private String conversationId;

    private String sourceTenantId;

    private String patientId;

    private String summaryText;

    private Integer summaryVersion;

    private LocalDateTime summarizedAt;

    public static MemoryShortSummaryEntity create(String conversationId,
                                                  String sourceTenantId,
                                                  String patientId,
                                                  String summaryText) {
        MemoryShortSummaryEntity entity = new MemoryShortSummaryEntity();
        entity.conversationId = conversationId;
        entity.sourceTenantId = sourceTenantId;
        entity.patientId = patientId;
        entity.summaryText = summaryText;
        entity.summaryVersion = 1;
        entity.summarizedAt = LocalDateTime.now();
        return entity;
    }

    public void merge(String summaryText) {
        this.summaryText = summaryText;
        this.summaryVersion = this.summaryVersion == null ? 1 : this.summaryVersion + 1;
        this.summarizedAt = LocalDateTime.now();
    }
}
