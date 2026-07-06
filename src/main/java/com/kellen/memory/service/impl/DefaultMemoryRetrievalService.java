package com.kellen.memory.service.impl;

import com.kellen.memory.entity.MemoryEventEntity;
import com.kellen.memory.entity.MemoryLongFactEntity;
import com.kellen.memory.entity.MemoryRetrievalEvalEntity;
import com.kellen.memory.entity.MemoryShortMessageEntity;
import com.kellen.memory.entity.MemoryShortSummaryEntity;
import com.kellen.memory.entity.enums.MemoryEventOperationEnum;
import com.kellen.memory.mapper.MemoryLongFactMapper;
import com.kellen.memory.mapper.MemoryRetrievalEvalMapper;
import com.kellen.memory.mapper.MemoryShortMessageMapper;
import com.kellen.memory.mapper.MemoryShortSummaryMapper;
import com.kellen.memory.service.MemoryEventService;
import com.kellen.memory.service.MemoryRetrievalService;
import com.kellen.memory.service.policy.MemoryAuthorizationPolicy;
import com.kellen.memory.service.policy.MemoryMaskingPolicy;
import com.kellen.memory.service.policy.MemoryRetrievalPolicy;
import com.kellen.memory.service.policy.MemoryTextPolicy;
import com.kellen.rpc.memory.MemoryRetrievalItemRpcDTO;
import com.kellen.rpc.memory.MemoryRetrievalRpcDTO;
import com.kellen.rpc.memory.MemoryRetrievalRpcRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认记忆召回服务。
 */
@Service
public class DefaultMemoryRetrievalService implements MemoryRetrievalService {

    private static final int SHORT_MESSAGE_LIMIT = 12;

    private final MemoryShortSummaryMapper summaryMapper;

    private final MemoryShortMessageMapper messageMapper;

    private final MemoryLongFactMapper longFactMapper;

    private final MemoryRetrievalEvalMapper retrievalEvalMapper;

    private final MemoryAuthorizationPolicy authorizationPolicy;

    private final MemoryRetrievalPolicy retrievalPolicy;

    private final MemoryMaskingPolicy maskingPolicy;

    private final MemoryTextPolicy textPolicy;

    private final MemoryEventService eventService;

    public DefaultMemoryRetrievalService(MemoryShortSummaryMapper summaryMapper,
                                         MemoryShortMessageMapper messageMapper,
                                         MemoryLongFactMapper longFactMapper,
                                         MemoryRetrievalEvalMapper retrievalEvalMapper,
                                         MemoryAuthorizationPolicy authorizationPolicy,
                                         MemoryRetrievalPolicy retrievalPolicy,
                                         MemoryMaskingPolicy maskingPolicy,
                                         MemoryTextPolicy textPolicy,
                                         MemoryEventService eventService) {
        this.summaryMapper = summaryMapper;
        this.messageMapper = messageMapper;
        this.longFactMapper = longFactMapper;
        this.retrievalEvalMapper = retrievalEvalMapper;
        this.authorizationPolicy = authorizationPolicy;
        this.retrievalPolicy = retrievalPolicy;
        this.maskingPolicy = maskingPolicy;
        this.textPolicy = textPolicy;
        this.eventService = eventService;
    }

    @Override
    @Transactional
    public MemoryRetrievalRpcDTO retrieve(MemoryRetrievalRpcRequest request) {
        LocalDateTime startedAt = LocalDateTime.now();
        authorizationPolicy.assertCanRetrieve(request);
        int maxItems = retrievalPolicy.maxItems(request);
        int maxCharacters = retrievalPolicy.maxCharacters(request);
        boolean allowSensitive = retrievalPolicy.allowSensitive(request);
        List<String> hitSources = new ArrayList<>();
        StringBuilder context = new StringBuilder();

        int shortHitCount = appendShortMemory(context, hitSources, request, allowSensitive);
        List<MemoryLongFactEntity> memories = longFactMapper.findRecallable(
                textPolicy.required(request.getPatientId(), "patientId"),
                textPolicy.required(request.getSourceTenantId(), "sourceTenantId"),
                textPolicy.text(request.getQuery()),
                retrievalPolicy.includeArchived(request),
                maxItems
        );
        List<MemoryRetrievalItemRpcDTO> items = new ArrayList<>();
        if (!memories.isEmpty()) {
            hitSources.add("MYSQL_LONG_MEMORY");
            context.append("\n[长期患者记忆]\n");
            for (MemoryLongFactEntity memory : memories) {
                MemoryRetrievalItemRpcDTO item = toItem(memory, allowSensitive);
                items.add(item);
                appendLine(context, item);
            }
        }

        String contextText = retrievalPolicy.truncate(context.toString().trim(), maxCharacters);
        MemoryEventEntity event = event(request, contextText.isBlank() ? "MISS" : "SUCCESS", items.size());
        eventService.record(event);
        recordEval(request, startedAt, shortHitCount, items.size(), hitSources, contextText);

        MemoryRetrievalRpcDTO result = new MemoryRetrievalRpcDTO();
        result.setEnabled(true);
        result.setMatched(StringUtils.hasText(contextText));
        result.setContext(contextText);
        result.setSourceCount(hitSources.size());
        result.setHitSources(hitSources);
        result.setItems(items);
        result.setMessage(result.isMatched() ? "matched" : "no memory matched");
        return result;
    }

    private int appendShortMemory(StringBuilder context,
                                  List<String> hitSources,
                                  MemoryRetrievalRpcRequest request,
                                  boolean allowSensitive) {
        String conversationId = textPolicy.text(request.getConversationId());
        if (!StringUtils.hasText(conversationId)) {
            return 0;
        }
        int hitCount = 0;
        MemoryShortSummaryEntity summary = summaryMapper.findByConversation(conversationId).orElse(null);
        if (summary != null && StringUtils.hasText(summary.getSummaryText())) {
            hitSources.add("MYSQL_SHORT_SUMMARY");
            context.append("[会话滚动摘要]\n")
                    .append(maskingPolicy.maskForRecall(summary.getSummaryText(), allowSensitive))
                    .append("\n");
            hitCount++;
        }
        List<MemoryShortMessageEntity> recent = messageMapper.findRecent(conversationId, SHORT_MESSAGE_LIMIT);
        if (!recent.isEmpty()) {
            Collections.reverse(recent);
            hitSources.add("MYSQL_SHORT_WINDOW");
            context.append("\n[最近会话窗口]\n");
            for (MemoryShortMessageEntity message : recent) {
                context.append("- ")
                        .append(textPolicy.text(message.getMessageRole()))
                        .append(": ")
                        .append(maskingPolicy.maskForRecall(message.getContent(), allowSensitive))
                        .append("\n");
                hitCount++;
            }
        }
        return hitCount;
    }

    private MemoryRetrievalItemRpcDTO toItem(MemoryLongFactEntity memory, boolean allowSensitive) {
        MemoryRetrievalItemRpcDTO item = new MemoryRetrievalItemRpcDTO();
        item.setMemoryId(memory.getId());
        item.setMemoryType(memory.getMemoryType());
        item.setMemoryKey(memory.getMemoryKey());
        item.setTitle(maskingPolicy.maskForRecall(memory.getTitle(), allowSensitive));
        item.setContent(maskingPolicy.maskForRecall(memory.getContent(), allowSensitive));
        item.setSourceType(memory.getSourceType());
        item.setSourceId(memory.getSourceId());
        item.setConfidence(memory.getConfidence());
        item.setStatus(memory.getStatus());
        item.setHitSource("MYSQL_LONG_MEMORY");
        item.setOccurredAt(memory.getOccurredAt());
        item.setUpdatedAt(memory.getModifyDateTime());
        return item;
    }

    private void appendLine(StringBuilder context, MemoryRetrievalItemRpcDTO item) {
        context.append("- ")
                .append(textPolicy.text(item.getTitle()).isBlank() ? item.getMemoryKey() : item.getTitle())
                .append(": ")
                .append(item.getContent())
                .append("。来源=")
                .append(textPolicy.text(item.getSourceType()))
                .append(", 置信度=")
                .append(item.getConfidence() == null ? 0D : item.getConfidence())
                .append("\n");
    }

    private MemoryEventEntity event(MemoryRetrievalRpcRequest request, String result, int returnedCount) {
        MemoryEventEntity event = new MemoryEventEntity();
        event.setOperation(MemoryEventOperationEnum.RETRIEVE.code());
        event.setTargetType("PATIENT_MEMORY");
        event.setCaller(textPolicy.text(request.getCaller()));
        event.setPurpose(textPolicy.text(request.getPurpose()));
        event.setTraceId(textPolicy.text(request.getTraceId()));
        event.setUserId(textPolicy.text(request.getUserId()));
        event.setTenantId(textPolicy.text(request.getTenantId()));
        event.setSourceTenantId(textPolicy.text(request.getSourceTenantId()));
        event.setPatientId(textPolicy.text(request.getPatientId()));
        event.setResult(result);
        event.setDetailJson("{\"returnedCount\":" + returnedCount + "}");
        return event;
    }

    private void recordEval(MemoryRetrievalRpcRequest request,
                            LocalDateTime startedAt,
                            int shortHitCount,
                            int sqlHitCount,
                            List<String> hitSources,
                            String contextText) {
        MemoryRetrievalEvalEntity eval = new MemoryRetrievalEvalEntity();
        eval.setTraceId(textPolicy.text(request.getTraceId()));
        eval.setCaller(textPolicy.text(request.getCaller()));
        eval.setPurpose(textPolicy.text(request.getPurpose()));
        eval.setSourceTenantId(textPolicy.text(request.getSourceTenantId()));
        eval.setPatientId(textPolicy.text(request.getPatientId()));
        eval.setConversationId(textPolicy.text(request.getConversationId()));
        eval.setQueryText(textPolicy.text(request.getQuery()));
        eval.setMaxItems(request.getMaxItems());
        eval.setMaxTokens(request.getMaxTokens());
        eval.setShortHitCount(shortHitCount);
        eval.setSqlHitCount(sqlHitCount);
        eval.setVectorHitCount(0);
        eval.setReturnedCount(sqlHitCount);
        eval.setLatencyMs(Duration.between(startedAt, LocalDateTime.now()).toMillis());
        eval.setResultMessage(contextText.isBlank() ? "MISS" : String.join(",", hitSources));
        retrievalEvalMapper.save(eval);
    }
}
