package com.kellen.memory.service.impl;

import com.kellen.memory.entity.MemoryCandidateEntity;
import com.kellen.memory.entity.MemoryEventEntity;
import com.kellen.memory.entity.MemoryLongFactEntity;
import com.kellen.memory.entity.enums.MemoryEventOperationEnum;
import com.kellen.memory.mapper.MemoryCandidateMapper;
import com.kellen.memory.mapper.MemoryLongFactMapper;
import com.kellen.memory.service.MemoryCandidateService;
import com.kellen.memory.service.MemoryEventService;
import com.kellen.memory.service.MemoryVectorTaskService;
import com.kellen.memory.service.policy.MemoryAuthorizationPolicy;
import com.kellen.memory.service.policy.MemoryMaskingPolicy;
import com.kellen.memory.service.policy.MemoryTextPolicy;
import com.kellen.memory.service.policy.MemoryWritePolicy;
import com.kellen.rpc.memory.MemoryCandidateRpcRequest;
import com.kellen.rpc.memory.MemoryWriteRpcDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认候选记忆写入服务。
 */
@Service
public class DefaultMemoryCandidateService implements MemoryCandidateService {

    private final MemoryCandidateMapper candidateMapper;

    private final MemoryLongFactMapper longFactMapper;

    private final MemoryAuthorizationPolicy authorizationPolicy;

    private final MemoryWritePolicy writePolicy;

    private final MemoryMaskingPolicy maskingPolicy;

    private final MemoryTextPolicy textPolicy;

    private final MemoryVectorTaskService vectorTaskService;

    private final MemoryEventService eventService;

    public DefaultMemoryCandidateService(MemoryCandidateMapper candidateMapper,
                                         MemoryLongFactMapper longFactMapper,
                                         MemoryAuthorizationPolicy authorizationPolicy,
                                         MemoryWritePolicy writePolicy,
                                         MemoryMaskingPolicy maskingPolicy,
                                         MemoryTextPolicy textPolicy,
                                         MemoryVectorTaskService vectorTaskService,
                                         MemoryEventService eventService) {
        this.candidateMapper = candidateMapper;
        this.longFactMapper = longFactMapper;
        this.authorizationPolicy = authorizationPolicy;
        this.writePolicy = writePolicy;
        this.maskingPolicy = maskingPolicy;
        this.textPolicy = textPolicy;
        this.vectorTaskService = vectorTaskService;
        this.eventService = eventService;
    }

    @Override
    @Transactional
    public MemoryWriteRpcDTO submitCandidate(MemoryCandidateRpcRequest request) {
        authorizationPolicy.assertCanWriteCandidate(request);
        String sensitivityLevel = maskingPolicy.sensitivityLevel(request.getContent(), request.getFactsJson());
        MemoryCandidateEntity candidate = candidate(request, sensitivityLevel);
        candidateMapper.save(candidate);
        eventService.record(event(MemoryEventOperationEnum.WRITE_CANDIDATE.code(), "CANDIDATE",
                candidate.getId(), request, "SUCCESS", ""));
        if (writePolicy.shouldReject(request)) {
            candidate.reject("confidence below acceptable threshold");
            candidateMapper.save(candidate);
            return writeResult(false, "REJECTED", candidate.getId(), null, null,
                    "candidate rejected by confidence policy");
        }
        if (!writePolicy.shouldAutoPromote(request)) {
            return writeResult(true, "PENDING", candidate.getId(), null, null,
                    "candidate saved for confirmation");
        }
        MemoryLongFactEntity memory = upsertLongFact(request, sensitivityLevel);
        candidate.accept(memory.getId());
        candidateMapper.save(candidate);
        vectorTaskService.enqueueUpsert(memory);
        MemoryEventEntity event = eventService.record(event(MemoryEventOperationEnum.PROMOTE_CANDIDATE.code(), "LONG_FACT",
                memory.getId(), request, "SUCCESS", candidate.getId()));
        return writeResult(true, "ACCEPTED", candidate.getId(), memory.getId(), event.getId(),
                "candidate promoted to long memory");
    }

    private MemoryCandidateEntity candidate(MemoryCandidateRpcRequest request, String sensitivityLevel) {
        MemoryCandidateEntity candidate = MemoryCandidateEntity.pending();
        candidate.setCaller(textPolicy.required(request.getCaller(), "caller"));
        candidate.setPurpose(textPolicy.text(request.getPurpose()));
        candidate.setTraceId(textPolicy.text(request.getTraceId()));
        candidate.setUserId(textPolicy.text(request.getUserId()));
        candidate.setTenantId(textPolicy.text(request.getTenantId()));
        candidate.setSourceTenantId(textPolicy.required(request.getSourceTenantId(), "sourceTenantId"));
        candidate.setPatientId(textPolicy.required(request.getPatientId(), "patientId"));
        candidate.setConversationId(textPolicy.text(request.getConversationId()));
        candidate.setSourceType(textPolicy.text(request.getSourceType()));
        candidate.setSourceId(textPolicy.text(request.getSourceId()));
        candidate.setMemoryType(textPolicy.required(request.getMemoryType(), "memoryType"));
        candidate.setMemoryKey(textPolicy.required(request.getMemoryKey(), "memoryKey"));
        candidate.setTitle(textPolicy.text(request.getTitle()));
        candidate.setContent(textPolicy.required(request.getContent(), "content"));
        candidate.setFactsJson(textPolicy.text(request.getFactsJson()));
        candidate.setConfidence(writePolicy.confidence(request.getConfidence()));
        candidate.setRequiresHumanReview(Boolean.TRUE.equals(request.getRequiresHumanReview()));
        candidate.setSensitivityLevel(sensitivityLevel);
        candidate.setAuthorizationScope(textPolicy.text(request.getAuthorizationScope()));
        candidate.setOccurredAt(request.getOccurredAt() == null ? java.time.LocalDateTime.now() : request.getOccurredAt());
        return candidate;
    }

    private MemoryLongFactEntity upsertLongFact(MemoryCandidateRpcRequest request, String sensitivityLevel) {
        String patientId = textPolicy.required(request.getPatientId(), "patientId");
        String sourceTenantId = textPolicy.required(request.getSourceTenantId(), "sourceTenantId");
        String memoryKey = textPolicy.required(request.getMemoryKey(), "memoryKey");
        return longFactMapper.findCurrentByPatientAndKey(patientId, sourceTenantId, memoryKey)
                .map(existing -> {
                    existing.refresh(
                            effectiveTitle(request),
                            textPolicy.required(request.getContent(), "content"),
                            textPolicy.text(request.getFactsJson()),
                            textPolicy.text(request.getSourceType()),
                            textPolicy.text(request.getSourceId()),
                            writePolicy.confidence(request.getConfidence()),
                            sensitivityLevel,
                            textPolicy.text(request.getAuthorizationScope()),
                            request.getOccurredAt()
                    );
                    longFactMapper.save(existing);
                    return existing;
                })
                .orElseGet(() -> {
                    MemoryLongFactEntity created = MemoryLongFactEntity.create(
                            patientId,
                            sourceTenantId,
                            textPolicy.required(request.getMemoryType(), "memoryType"),
                            memoryKey,
                            effectiveTitle(request),
                            textPolicy.required(request.getContent(), "content"),
                            textPolicy.text(request.getFactsJson()),
                            textPolicy.text(request.getSourceType()),
                            textPolicy.text(request.getSourceId()),
                            writePolicy.confidence(request.getConfidence()),
                            sensitivityLevel,
                            textPolicy.text(request.getAuthorizationScope()),
                            request.getOccurredAt()
                    );
                    longFactMapper.save(created);
                    return created;
                });
    }

    private String effectiveTitle(MemoryCandidateRpcRequest request) {
        String title = textPolicy.text(request.getTitle());
        return title.isBlank() ? textPolicy.required(request.getMemoryKey(), "memoryKey") : title;
    }

    private MemoryEventEntity event(String operation,
                                    String targetType,
                                    String targetId,
                                    MemoryCandidateRpcRequest request,
                                    String result,
                                    String detail) {
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
        event.setResult(result);
        event.setDetailJson(detail);
        return event;
    }

    private MemoryWriteRpcDTO writeResult(boolean success,
                                          String status,
                                          String candidateId,
                                          String memoryId,
                                          String eventId,
                                          String message) {
        MemoryWriteRpcDTO result = new MemoryWriteRpcDTO();
        result.setSuccess(success);
        result.setStatus(status);
        result.setCandidateId(candidateId);
        result.setMemoryId(memoryId);
        result.setEventId(eventId);
        result.setMessage(message);
        return result;
    }
}
