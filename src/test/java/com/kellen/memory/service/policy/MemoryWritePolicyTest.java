package com.kellen.memory.service.policy;

import com.kellen.rpc.memory.MemoryCandidateRpcRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryWritePolicyTest {

    private final MemoryWritePolicy policy = new MemoryWritePolicy();

    @Test
    void shouldAutoPromoteConfidentCandidateWithoutHumanReview() {
        MemoryCandidateRpcRequest request = new MemoryCandidateRpcRequest();
        request.setConfidence(0.85);
        request.setRequiresHumanReview(false);

        assertTrue(policy.shouldAutoPromote(request));
        assertFalse(policy.shouldReject(request));
    }

    @Test
    void shouldKeepHumanReviewCandidatePending() {
        MemoryCandidateRpcRequest request = new MemoryCandidateRpcRequest();
        request.setConfidence(0.9);
        request.setRequiresHumanReview(true);

        assertFalse(policy.shouldAutoPromote(request));
        assertFalse(policy.shouldReject(request));
    }

    @Test
    void shouldRejectLowConfidenceCandidate() {
        MemoryCandidateRpcRequest request = new MemoryCandidateRpcRequest();
        request.setConfidence(0.3);

        assertFalse(policy.shouldAutoPromote(request));
        assertTrue(policy.shouldReject(request));
    }
}
