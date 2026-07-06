package com.kellen.memory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kellen.entity.EntityBase;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 记忆治理与召回审计事件。
 */
@Getter
@Setter
@TableName("ai_memory_event")
public class MemoryEventEntity extends EntityBase {

    private String operation;

    private String targetType;

    private String targetId;

    private String caller;

    private String purpose;

    private String traceId;

    private String userId;

    private String tenantId;

    private String sourceTenantId;

    private String patientId;

    private String reason;

    private String result;

    private String detailJson;
}
