package com.kellen.memory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kellen.entity.EntityBase;
import com.kellen.memory.entity.enums.MemorySensitivityLevelEnum;
import com.kellen.memory.entity.enums.MemoryStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 患者长期记忆权威事实。
 */
@Getter
@Setter
@TableName("ai_memory_long_fact")
public class MemoryLongFactEntity extends EntityBase {

    private String patientId;

    private String sourceTenantId;

    private String memoryType;

    private String memoryKey;

    private String title;

    private String content;

    private String factsJson;

    private String sourceType;

    private String sourceId;

    private Double confidence;

    private String status;

    private String sensitivityLevel;

    private String authorizationScope;

    private String embeddingStatus;

    private String vectorCollection;

    private String vectorId;

    private LocalDateTime indexedAt;

    private LocalDateTime occurredAt;

    private LocalDateTime archivedAt;

    private LocalDateTime maskedAt;

    private LocalDateTime deletedAt;

    public static MemoryLongFactEntity create(String patientId,
                                              String sourceTenantId,
                                              String memoryType,
                                              String memoryKey,
                                              String title,
                                              String content,
                                              String factsJson,
                                              String sourceType,
                                              String sourceId,
                                              Double confidence,
                                              String sensitivityLevel,
                                              String authorizationScope,
                                              LocalDateTime occurredAt) {
        MemoryLongFactEntity entity = new MemoryLongFactEntity();
        entity.patientId = patientId;
        entity.sourceTenantId = sourceTenantId;
        entity.memoryType = memoryType;
        entity.memoryKey = memoryKey;
        entity.title = title;
        entity.content = content;
        entity.factsJson = factsJson;
        entity.sourceType = sourceType;
        entity.sourceId = sourceId;
        entity.confidence = confidence;
        entity.status = MemoryStatusEnum.ACTIVE.code();
        entity.sensitivityLevel = sensitivityLevel == null ? MemorySensitivityLevelEnum.NORMAL.code() : sensitivityLevel;
        entity.authorizationScope = authorizationScope;
        entity.embeddingStatus = "PENDING";
        entity.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        return entity;
    }

    public void refresh(String title,
                        String content,
                        String factsJson,
                        String sourceType,
                        String sourceId,
                        Double confidence,
                        String sensitivityLevel,
                        String authorizationScope,
                        LocalDateTime occurredAt) {
        this.title = title;
        this.content = content;
        this.factsJson = factsJson;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.confidence = confidence;
        this.sensitivityLevel = sensitivityLevel;
        this.authorizationScope = authorizationScope;
        this.status = MemoryStatusEnum.ACTIVE.code();
        this.embeddingStatus = "PENDING";
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        this.archivedAt = null;
        this.maskedAt = null;
        this.deletedAt = null;
    }

    public void archive() {
        this.status = MemoryStatusEnum.ARCHIVED.code();
        this.archivedAt = LocalDateTime.now();
        this.embeddingStatus = "DELETE_PENDING";
    }

    public void mask(String maskedContent, String maskedFactsJson) {
        this.status = MemoryStatusEnum.MASKED.code();
        this.content = maskedContent;
        this.factsJson = maskedFactsJson;
        this.maskedAt = LocalDateTime.now();
        this.embeddingStatus = "PENDING";
    }

    public void delete() {
        this.status = MemoryStatusEnum.DELETED.code();
        this.deletedAt = LocalDateTime.now();
        this.embeddingStatus = "DELETE_PENDING";
    }

    public void revoke() {
        this.status = MemoryStatusEnum.REVOKED.code();
        this.deletedAt = LocalDateTime.now();
        this.embeddingStatus = "DELETE_PENDING";
    }
}
