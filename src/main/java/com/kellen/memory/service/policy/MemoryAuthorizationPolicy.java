package com.kellen.memory.service.policy;

import com.kellen.rpc.memory.MemoryCandidateRpcRequest;
import com.kellen.rpc.memory.MemoryGovernanceRpcRequest;
import com.kellen.rpc.memory.MemoryRetrievalRpcRequest;
import com.kellen.rpc.memory.MemoryShortMessageRpcRequest;
import com.kellen.rpc.memory.MemoryShortSummaryRpcRequest;
import com.kellen.rpc.memory.MemoryVectorRebuildRpcRequest;
import org.springframework.stereotype.Component;

/**
 * 记忆访问授权和租户隔离策略。
 */
@Component
public class MemoryAuthorizationPolicy {

    private final MemoryTextPolicy textPolicy;

    public MemoryAuthorizationPolicy(MemoryTextPolicy textPolicy) {
        this.textPolicy = textPolicy;
    }

    public void assertCanWriteCandidate(MemoryCandidateRpcRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        requirePatientScope(request.getPatientId(), request.getSourceTenantId());
        textPolicy.required(request.getCaller(), "caller");
        textPolicy.required(request.getMemoryType(), "memoryType");
        textPolicy.required(request.getMemoryKey(), "memoryKey");
        textPolicy.required(request.getContent(), "content");
    }

    public void assertCanAppendShortMessage(MemoryShortMessageRpcRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        textPolicy.required(request.getConversationId(), "conversationId");
        textPolicy.required(request.getCaller(), "caller");
        textPolicy.required(request.getMessageRole(), "messageRole");
        textPolicy.required(request.getContent(), "content");
    }

    public void assertCanUpsertSummary(MemoryShortSummaryRpcRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        textPolicy.required(request.getConversationId(), "conversationId");
        textPolicy.required(request.getCaller(), "caller");
        textPolicy.required(request.getSummaryText(), "summaryText");
    }

    public void assertCanRetrieve(MemoryRetrievalRpcRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        requirePatientScope(request.getPatientId(), request.getSourceTenantId());
        textPolicy.required(request.getCaller(), "caller");
        textPolicy.required(request.getPurpose(), "purpose");
    }

    public void assertCanGovern(MemoryGovernanceRpcRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        requirePatientScope(request.getPatientId(), request.getSourceTenantId());
        textPolicy.required(request.getCaller(), "caller");
        textPolicy.required(request.getOperation(), "operation");
    }

    public void assertCanRebuild(MemoryVectorRebuildRpcRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        textPolicy.required(request.getCaller(), "caller");
    }

    private void requirePatientScope(String patientId, String sourceTenantId) {
        textPolicy.required(patientId, "patientId");
        textPolicy.required(sourceTenantId, "sourceTenantId");
    }
}
