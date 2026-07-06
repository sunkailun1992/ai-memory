package com.kellen.memory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kellen.entity.EntityBase;
import com.kellen.memory.entity.enums.MemoryVectorTaskStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Qdrant 派生索引异步任务。
 */
@Getter
@Setter
@TableName("ai_memory_vector_task")
public class MemoryVectorTaskEntity extends EntityBase {

    private String taskType;

    private String memoryId;

    private String patientId;

    private String sourceTenantId;

    private String collectionName;

    private String vectorId;

    private String payloadJson;

    private String status;

    private Integer attempts;

    private LocalDateTime nextRetryAt;

    private String errorMessage;

    public static MemoryVectorTaskEntity create(String taskType,
                                                String memoryId,
                                                String patientId,
                                                String sourceTenantId,
                                                String collectionName,
                                                String vectorId,
                                                String payloadJson) {
        MemoryVectorTaskEntity entity = new MemoryVectorTaskEntity();
        entity.taskType = taskType;
        entity.memoryId = memoryId;
        entity.patientId = patientId;
        entity.sourceTenantId = sourceTenantId;
        entity.collectionName = collectionName;
        entity.vectorId = vectorId;
        entity.payloadJson = payloadJson;
        entity.status = MemoryVectorTaskStatusEnum.PENDING.code();
        entity.attempts = 0;
        return entity;
    }
}
