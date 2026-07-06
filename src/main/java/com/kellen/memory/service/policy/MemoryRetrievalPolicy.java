package com.kellen.memory.service.policy;

import com.kellen.rpc.memory.MemoryRetrievalRpcRequest;
import org.springframework.stereotype.Component;

/**
 * 记忆召回预算策略。
 */
@Component
public class MemoryRetrievalPolicy {

    private static final int DEFAULT_MAX_ITEMS = 12;

    private static final int DEFAULT_MAX_TOKENS = 1200;

    private final MemoryTextPolicy textPolicy;

    public MemoryRetrievalPolicy(MemoryTextPolicy textPolicy) {
        this.textPolicy = textPolicy;
    }

    public int maxItems(MemoryRetrievalRpcRequest request) {
        return textPolicy.bounded(request.getMaxItems(), DEFAULT_MAX_ITEMS, 1, 30);
    }

    public int maxCharacters(MemoryRetrievalRpcRequest request) {
        int tokens = textPolicy.bounded(request.getMaxTokens(), DEFAULT_MAX_TOKENS, 100, 6000);
        return tokens * 4;
    }

    public boolean includeArchived(MemoryRetrievalRpcRequest request) {
        return Boolean.TRUE.equals(request.getIncludeArchived());
    }

    public boolean allowSensitive(MemoryRetrievalRpcRequest request) {
        return Boolean.TRUE.equals(request.getAllowSensitive());
    }

    public String truncate(String value, int maxCharacters) {
        if (value == null || value.length() <= maxCharacters) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(0, maxCharacters));
    }
}
