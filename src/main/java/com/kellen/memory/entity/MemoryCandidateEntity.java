package com.kellen.memory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kellen.entity.EntityBase;
import com.kellen.memory.entity.enums.MemoryCandidateStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Agent 提交的候选记忆。
 */
@Getter
@Setter
@TableName("ai_memory_candidate")
public class MemoryCandidateEntity extends EntityBase {

    private String caller;

    private String purpose;

    private String traceId;

    private String userId;

    private String tenantId;

    private String sourceTenantId;

    private String patientId;

    private String conversationId;

    private String sourceType;

    private String sourceId;

    private String memoryType;

    private String memoryKey;

    private String title;

    private String content;

    private String factsJson;

    private Double confidence;

    private Boolean requiresHumanReview;

    private String sensitivityLevel;

    private String authorizationScope;

    private String reviewStatus;

    private String rejectionReason;

    private String promotedMemoryId;

    private LocalDateTime occurredAt;

    private LocalDateTime reviewedAt;

    public static MemoryCandidateEntity pending() {
        MemoryCandidateEntity entity = new MemoryCandidateEntity();
        entity.reviewStatus = MemoryCandidateStatusEnum.PENDING.code();
        return entity;
    }

    public void accept(String memoryId) {
        this.reviewStatus = MemoryCandidateStatusEnum.ACCEPTED.code();
        this.promotedMemoryId = memoryId;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.reviewStatus = MemoryCandidateStatusEnum.REJECTED.code();
        this.rejectionReason = reason;
        this.reviewedAt = LocalDateTime.now();
    }
}
