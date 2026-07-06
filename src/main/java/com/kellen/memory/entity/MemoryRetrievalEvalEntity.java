package com.kellen.memory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kellen.entity.EntityBase;
import lombok.Getter;
import lombok.Setter;

/**
 * 记忆召回评估记录。
 */
@Getter
@Setter
@TableName("ai_memory_retrieval_eval")
public class MemoryRetrievalEvalEntity extends EntityBase {

    private String traceId;

    private String caller;

    private String purpose;

    private String sourceTenantId;

    private String patientId;

    private String conversationId;

    private String queryText;

    private Integer maxItems;

    private Integer maxTokens;

    private Integer shortHitCount;

    private Integer sqlHitCount;

    private Integer vectorHitCount;

    private Integer returnedCount;

    private Long latencyMs;

    private String resultMessage;
}
