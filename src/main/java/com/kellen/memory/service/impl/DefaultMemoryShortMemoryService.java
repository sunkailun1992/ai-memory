package com.kellen.memory.service.impl;

import com.kellen.memory.entity.MemoryEventEntity;
import com.kellen.memory.entity.MemoryShortMessageEntity;
import com.kellen.memory.entity.MemoryShortSummaryEntity;
import com.kellen.memory.entity.enums.MemoryEventOperationEnum;
import com.kellen.memory.mapper.MemoryShortMessageMapper;
import com.kellen.memory.mapper.MemoryShortSummaryMapper;
import com.kellen.memory.service.MemoryEventService;
import com.kellen.memory.service.MemoryShortMemoryService;
import com.kellen.memory.service.policy.MemoryAuthorizationPolicy;
import com.kellen.memory.service.policy.MemoryTextPolicy;
import com.kellen.rpc.memory.MemoryShortMessageRpcRequest;
import com.kellen.rpc.memory.MemoryShortSummaryRpcRequest;
import com.kellen.rpc.memory.MemoryWriteRpcDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认短期会话记忆服务。
 */
@Service
public class DefaultMemoryShortMemoryService implements MemoryShortMemoryService {

    private static final int MAX_SHORT_MESSAGES = 40;

    private static final int COMPACT_BATCH = 10;

    private static final int MAX_SUMMARY_CHARS = 4000;

    private final MemoryShortMessageMapper messageMapper;

    private final MemoryShortSummaryMapper summaryMapper;

    private final MemoryAuthorizationPolicy authorizationPolicy;

    private final MemoryTextPolicy textPolicy;

    private final MemoryEventService eventService;

    public DefaultMemoryShortMemoryService(MemoryShortMessageMapper messageMapper,
                                           MemoryShortSummaryMapper summaryMapper,
                                           MemoryAuthorizationPolicy authorizationPolicy,
                                           MemoryTextPolicy textPolicy,
                                           MemoryEventService eventService) {
        this.messageMapper = messageMapper;
        this.summaryMapper = summaryMapper;
        this.authorizationPolicy = authorizationPolicy;
        this.textPolicy = textPolicy;
        this.eventService = eventService;
    }

    @Override
    @Transactional
    public MemoryWriteRpcDTO appendMessage(MemoryShortMessageRpcRequest request) {
        authorizationPolicy.assertCanAppendShortMessage(request);
        String conversationId = textPolicy.required(request.getConversationId(), "conversationId");
        MemoryShortMessageEntity entity = MemoryShortMessageEntity.create(
                conversationId,
                textPolicy.text(request.getSourceTenantId()),
                textPolicy.text(request.getPatientId()),
                textPolicy.text(request.getUserId()),
                textPolicy.required(request.getCaller(), "caller"),
                textPolicy.required(request.getMessageRole(), "messageRole").toLowerCase(),
                textPolicy.required(request.getContent(), "content"),
                request.getTokenEstimate(),
                request.getOccurredAt()
        );
        messageMapper.save(entity);
        compactIfNecessary(conversationId);
        MemoryEventEntity event = event(MemoryEventOperationEnum.APPEND_SHORT_MESSAGE.code(), "SHORT_MESSAGE",
                entity.getId(), request.getCaller(), request.getTraceId(), request.getUserId(),
                request.getTenantId(), request.getSourceTenantId(), request.getPatientId());
        eventService.record(event);
        return writeResult(true, "APPENDED", entity.getId(), event.getId(), "short memory appended");
    }

    @Override
    @Transactional
    public MemoryWriteRpcDTO upsertSummary(MemoryShortSummaryRpcRequest request) {
        authorizationPolicy.assertCanUpsertSummary(request);
        String conversationId = textPolicy.required(request.getConversationId(), "conversationId");
        String summaryText = trimSummary(textPolicy.required(request.getSummaryText(), "summaryText"));
        MemoryShortSummaryEntity summary = summaryMapper.findByConversation(conversationId)
                .map(existing -> {
                    existing.merge(summaryText);
                    return existing;
                })
                .orElseGet(() -> MemoryShortSummaryEntity.create(
                        conversationId,
                        textPolicy.text(request.getSourceTenantId()),
                        textPolicy.text(request.getPatientId()),
                        summaryText
                ));
        summaryMapper.save(summary);
        MemoryEventEntity event = event(MemoryEventOperationEnum.UPSERT_SHORT_SUMMARY.code(), "SHORT_SUMMARY",
                summary.getId(), request.getCaller(), request.getTraceId(), null,
                request.getTenantId(), request.getSourceTenantId(), request.getPatientId());
        eventService.record(event);
        return writeResult(true, "SUMMARY_UPSERTED", summary.getId(), event.getId(), "short summary upserted");
    }

    private void compactIfNecessary(String conversationId) {
        Long count = messageMapper.countByConversation(conversationId);
        if (count == null || count <= MAX_SHORT_MESSAGES) {
            return;
        }
        List<MemoryShortMessageEntity> oldest = messageMapper.findOldest(conversationId, COMPACT_BATCH);
        if (oldest.isEmpty()) {
            return;
        }
        String addition = oldest.stream()
                .map(item -> item.getMessageRole() + ": " + item.getContent())
                .collect(Collectors.joining("\n"));
        String existing = summaryMapper.findByConversation(conversationId)
                .map(MemoryShortSummaryEntity::getSummaryText)
                .orElse("");
        String merged = trimSummary((existing.isBlank() ? "" : existing + "\n") + addition);
        MemoryShortSummaryEntity summary = summaryMapper.findByConversation(conversationId)
                .map(existingSummary -> {
                    existingSummary.merge(merged);
                    return existingSummary;
                })
                .orElseGet(() -> MemoryShortSummaryEntity.create(conversationId,
                        oldest.get(0).getSourceTenantId(), oldest.get(0).getPatientId(), merged));
        summaryMapper.save(summary);
        List<String> ids = oldest.stream().map(MemoryShortMessageEntity::getId).toList();
        messageMapper.deleteBatchIds(ids);
    }

    private String trimSummary(String value) {
        if (value.length() <= MAX_SUMMARY_CHARS) {
            return value;
        }
        return value.substring(value.length() - MAX_SUMMARY_CHARS);
    }

    private MemoryEventEntity event(String operation,
                                    String targetType,
                                    String targetId,
                                    String caller,
                                    String traceId,
                                    String userId,
                                    String tenantId,
                                    String sourceTenantId,
                                    String patientId) {
        MemoryEventEntity event = new MemoryEventEntity();
        event.setOperation(operation);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setCaller(textPolicy.text(caller));
        event.setTraceId(textPolicy.text(traceId));
        event.setUserId(textPolicy.text(userId));
        event.setTenantId(textPolicy.text(tenantId));
        event.setSourceTenantId(textPolicy.text(sourceTenantId));
        event.setPatientId(textPolicy.text(patientId));
        event.setResult("SUCCESS");
        return event;
    }

    private MemoryWriteRpcDTO writeResult(boolean success,
                                          String status,
                                          String memoryId,
                                          String eventId,
                                          String message) {
        MemoryWriteRpcDTO result = new MemoryWriteRpcDTO();
        result.setSuccess(success);
        result.setStatus(status);
        result.setMemoryId(memoryId);
        result.setEventId(eventId);
        result.setMessage(message);
        return result;
    }
}
