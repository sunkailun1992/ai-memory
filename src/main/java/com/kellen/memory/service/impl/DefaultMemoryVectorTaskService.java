package com.kellen.memory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kellen.memory.entity.MemoryLongFactEntity;
import com.kellen.memory.entity.MemoryVectorTaskEntity;
import com.kellen.memory.entity.enums.MemoryVectorTaskTypeEnum;
import com.kellen.memory.mapper.MemoryVectorTaskMapper;
import com.kellen.memory.service.MemoryVectorTaskService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认 Qdrant 派生索引任务服务。
 */
@Service
public class DefaultMemoryVectorTaskService implements MemoryVectorTaskService {

    private static final String DEFAULT_COLLECTION = "patient_long_memory";

    private final MemoryVectorTaskMapper taskMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DefaultMemoryVectorTaskService(MemoryVectorTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public MemoryVectorTaskEntity enqueueUpsert(MemoryLongFactEntity memory) {
        MemoryVectorTaskEntity task = MemoryVectorTaskEntity.create(
                MemoryVectorTaskTypeEnum.UPSERT.code(),
                memory.getId(),
                memory.getPatientId(),
                memory.getSourceTenantId(),
                collection(memory),
                vectorId(memory),
                payload(memory)
        );
        taskMapper.save(task);
        return task;
    }

    @Override
    public MemoryVectorTaskEntity enqueueDelete(MemoryLongFactEntity memory) {
        taskMapper.cancelPendingByMemory(memory.getId());
        MemoryVectorTaskEntity task = MemoryVectorTaskEntity.create(
                MemoryVectorTaskTypeEnum.DELETE.code(),
                memory.getId(),
                memory.getPatientId(),
                memory.getSourceTenantId(),
                collection(memory),
                vectorId(memory),
                payload(memory)
        );
        taskMapper.save(task);
        return task;
    }

    @Override
    public MemoryVectorTaskEntity enqueueRebuildMarker(String patientId, String sourceTenantId, String memoryId, String reason) {
        MemoryVectorTaskEntity task = MemoryVectorTaskEntity.create(
                MemoryVectorTaskTypeEnum.REBUILD.code(),
                memoryId,
                patientId,
                sourceTenantId,
                DEFAULT_COLLECTION,
                "",
                json(Map.of("reason", reason == null ? "" : reason))
        );
        taskMapper.save(task);
        return task;
    }

    private String collection(MemoryLongFactEntity memory) {
        return memory.getVectorCollection() == null || memory.getVectorCollection().isBlank()
                ? DEFAULT_COLLECTION
                : memory.getVectorCollection().trim();
    }

    private String vectorId(MemoryLongFactEntity memory) {
        return memory.getVectorId() == null || memory.getVectorId().isBlank()
                ? "memory-" + memory.getId()
                : memory.getVectorId().trim();
    }

    private String payload(MemoryLongFactEntity memory) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("memoryId", memory.getId());
        payload.put("patientId", memory.getPatientId());
        payload.put("sourceTenantId", memory.getSourceTenantId());
        payload.put("memoryType", memory.getMemoryType());
        payload.put("memoryKey", memory.getMemoryKey());
        payload.put("title", memory.getTitle());
        payload.put("content", memory.getContent());
        payload.put("status", memory.getStatus());
        payload.put("sensitivityLevel", memory.getSensitivityLevel());
        return json(payload);
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize memory vector task payload", exception);
        }
    }
}
