package com.kellen.memory.service;

import com.kellen.memory.entity.MemoryLongFactEntity;
import com.kellen.memory.entity.MemoryVectorTaskEntity;

/**
 * Qdrant 派生索引任务服务。
 */
public interface MemoryVectorTaskService {

    MemoryVectorTaskEntity enqueueUpsert(MemoryLongFactEntity memory);

    MemoryVectorTaskEntity enqueueDelete(MemoryLongFactEntity memory);

    MemoryVectorTaskEntity enqueueRebuildMarker(String patientId, String sourceTenantId, String memoryId, String reason);
}
