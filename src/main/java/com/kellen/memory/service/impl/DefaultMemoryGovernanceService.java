package com.kellen.memory.service.impl;

import com.kellen.memory.entity.MemoryEventEntity;
import com.kellen.memory.entity.MemoryLongFactEntity;
import com.kellen.memory.entity.MemoryVectorTaskEntity;
import com.kellen.memory.entity.enums.MemoryEventOperationEnum;
import com.kellen.memory.mapper.MemoryLongFactMapper;
import com.kellen.memory.mapper.MemoryVectorTaskMapper;
import com.kellen.memory.service.MemoryEventService;
import com.kellen.memory.service.MemoryGovernanceService;
import com.kellen.memory.service.MemoryVectorTaskService;
import com.kellen.memory.service.policy.MemoryAuthorizationPolicy;
import com.kellen.memory.service.policy.MemoryMaskingPolicy;
import com.kellen.memory.service.policy.MemoryTextPolicy;
import com.kellen.rpc.memory.MemoryGovernanceRpcDTO;
import com.kellen.rpc.memory.MemoryGovernanceRpcRequest;
import com.kellen.rpc.memory.MemoryVectorRebuildRpcRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 默认记忆治理服务。
 */
@Service
public class DefaultMemoryGovernanceService implements MemoryGovernanceService {

    private static final int REBUILD_BATCH_SIZE = 500;

    private final MemoryLongFactMapper longFactMapper;

    private final MemoryVectorTaskMapper vectorTaskMapper;

    private final MemoryVectorTaskService vectorTaskService;

    private final MemoryEventService eventService;

    private final MemoryAuthorizationPolicy authorizationPolicy;

    private final MemoryMaskingPolicy maskingPolicy;

    private final MemoryTextPolicy textPolicy;

    public DefaultMemoryGovernanceService(MemoryLongFactMapper longFactMapper,
                                          MemoryVectorTaskMapper vectorTaskMapper,
                                          MemoryVectorTaskService vectorTaskService,
                                          MemoryEventService eventService,
                                          MemoryAuthorizationPolicy authorizationPolicy,
                                          MemoryMaskingPolicy maskingPolicy,
                                          MemoryTextPolicy textPolicy) {
        this.longFactMapper = longFactMapper;
        this.vectorTaskMapper = vectorTaskMapper;
        this.vectorTaskService = vectorTaskService;
        this.eventService = eventService;
        this.authorizationPolicy = authorizationPolicy;
        this.maskingPolicy = maskingPolicy;
        this.textPolicy = textPolicy;
    }

    @Override
    @Transactional
    public MemoryGovernanceRpcDTO govern(MemoryGovernanceRpcRequest request) {
        authorizationPolicy.assertCanGovern(request);
        String operation = textPolicy.required(request.getOperation(), "operation").toUpperCase(Locale.ROOT);
        List<MemoryLongFactEntity> targets = targets(request);
        List<String> taskIds = new ArrayList<>();
        for (MemoryLongFactEntity memory : targets) {
            applyOperation(operation, memory);
            longFactMapper.save(memory);
            vectorTaskMapper.cancelPendingByMemory(memory.getId());
            if (requiresDeletePoint(operation)) {
                taskIds.add(vectorTaskService.enqueueDelete(memory).getId());
            } else if ("MASK".equals(operation)) {
                taskIds.add(vectorTaskService.enqueueUpsert(memory).getId());
            }
        }
        MemoryEventEntity event = event(operationEvent(operation), "LONG_FACT",
                targets.isEmpty() ? "" : targets.get(0).getId(), request, "SUCCESS", targets.size());
        eventService.record(event);
        return result(true, targets.size(), event.getId(), taskIds.isEmpty() ? "" : taskIds.get(0),
                operation + " applied");
    }

    @Override
    @Transactional
    public MemoryGovernanceRpcDTO rebuildVectorIndex(MemoryVectorRebuildRpcRequest request) {
        authorizationPolicy.assertCanRebuild(request);
        List<MemoryLongFactEntity> memories = longFactMapper.findActiveForRebuild(
                textPolicy.text(request.getPatientId()),
                textPolicy.text(request.getSourceTenantId()),
                textPolicy.text(request.getMemoryId()),
                REBUILD_BATCH_SIZE
        );
        String firstTaskId = "";
        for (MemoryLongFactEntity memory : memories) {
            MemoryVectorTaskEntity task = vectorTaskService.enqueueRebuildMarker(
                    memory.getPatientId(),
                    memory.getSourceTenantId(),
                    memory.getId(),
                    textPolicy.text(request.getReason())
            );
            if (!StringUtils.hasText(firstTaskId)) {
                firstTaskId = task.getId();
            }
        }
        MemoryEventEntity event = rebuildEvent(request, memories.size());
        eventService.record(event);
        return result(true, memories.size(), event.getId(), firstTaskId, "vector rebuild queued");
    }

    private List<MemoryLongFactEntity> targets(MemoryGovernanceRpcRequest request) {
        String patientId = textPolicy.required(request.getPatientId(), "patientId");
        String sourceTenantId = textPolicy.required(request.getSourceTenantId(), "sourceTenantId");
        if (StringUtils.hasText(request.getMemoryId())) {
            MemoryLongFactEntity memory = longFactMapper.findById(request.getMemoryId().trim())
                    .orElseThrow(() -> new IllegalArgumentException("memory not found"));
            assertScope(memory, patientId, sourceTenantId);
            return List.of(memory);
        }
        if (StringUtils.hasText(request.getMemoryKey())) {
            return longFactMapper.findCurrentByPatientAndKey(patientId, sourceTenantId, request.getMemoryKey().trim())
                    .map(List::of)
                    .orElseGet(List::of);
        }
        throw new IllegalArgumentException("memoryId or memoryKey must not be blank");
    }

    private void assertScope(MemoryLongFactEntity memory, String patientId, String sourceTenantId) {
        if (!patientId.equals(memory.getPatientId()) || !sourceTenantId.equals(memory.getSourceTenantId())) {
            throw new IllegalArgumentException("memory scope mismatch");
        }
    }

    private void applyOperation(String operation, MemoryLongFactEntity memory) {
        switch (operation) {
            case "ARCHIVE" -> memory.archive();
            case "DELETE", "PRIVACY_DELETE" -> memory.delete();
            case "REVOKE" -> memory.revoke();
            case "MASK" -> memory.mask(maskingPolicy.mask(memory.getContent()), maskingPolicy.mask(memory.getFactsJson()));
            default -> throw new IllegalArgumentException("unsupported memory operation: " + operation);
        }
    }

    private boolean requiresDeletePoint(String operation) {
        return "ARCHIVE".equals(operation)
                || "DELETE".equals(operation)
                || "PRIVACY_DELETE".equals(operation)
                || "REVOKE".equals(operation);
    }

    private String operationEvent(String operation) {
        return switch (operation) {
            case "ARCHIVE" -> MemoryEventOperationEnum.ARCHIVE.code();
            case "DELETE", "PRIVACY_DELETE" -> MemoryEventOperationEnum.DELETE.code();
            case "REVOKE" -> MemoryEventOperationEnum.REVOKE.code();
            case "MASK" -> MemoryEventOperationEnum.MASK.code();
            default -> operation;
        };
    }

    private MemoryEventEntity event(String operation,
                                    String targetType,
                                    String targetId,
                                    MemoryGovernanceRpcRequest request,
                                    String result,
                                    int affectedCount) {
        MemoryEventEntity event = new MemoryEventEntity();
        event.setOperation(operation);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setCaller(textPolicy.text(request.getCaller()));
        event.setPurpose(textPolicy.text(request.getPurpose()));
        event.setTraceId(textPolicy.text(request.getTraceId()));
        event.setUserId(textPolicy.text(request.getUserId()));
        event.setTenantId(textPolicy.text(request.getTenantId()));
        event.setSourceTenantId(textPolicy.text(request.getSourceTenantId()));
        event.setPatientId(textPolicy.text(request.getPatientId()));
        event.setReason(textPolicy.text(request.getReason()));
        event.setResult(result);
        event.setDetailJson("{\"affectedCount\":" + affectedCount + "}");
        return event;
    }

    private MemoryEventEntity rebuildEvent(MemoryVectorRebuildRpcRequest request, int affectedCount) {
        MemoryEventEntity event = new MemoryEventEntity();
        event.setOperation(MemoryEventOperationEnum.REBUILD_INDEX.code());
        event.setTargetType("VECTOR_INDEX");
        event.setTargetId(textPolicy.text(request.getMemoryId()));
        event.setCaller(textPolicy.text(request.getCaller()));
        event.setTraceId(textPolicy.text(request.getTraceId()));
        event.setUserId(textPolicy.text(request.getUserId()));
        event.setTenantId(textPolicy.text(request.getTenantId()));
        event.setSourceTenantId(textPolicy.text(request.getSourceTenantId()));
        event.setPatientId(textPolicy.text(request.getPatientId()));
        event.setReason(textPolicy.text(request.getReason()));
        event.setResult("SUCCESS");
        event.setDetailJson("{\"affectedCount\":" + affectedCount + "}");
        return event;
    }

    private MemoryGovernanceRpcDTO result(boolean success,
                                          int affectedCount,
                                          String eventId,
                                          String taskId,
                                          String message) {
        MemoryGovernanceRpcDTO result = new MemoryGovernanceRpcDTO();
        result.setSuccess(success);
        result.setAffectedCount(affectedCount);
        result.setEventId(eventId);
        result.setTaskId(taskId);
        result.setMessage(message);
        return result;
    }
}
