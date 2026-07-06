package com.kellen.memory.service.policy;

import com.kellen.rpc.memory.MemoryCandidateRpcRequest;
import org.springframework.stereotype.Component;

/**
 * 候选记忆晋升策略。
 */
@Component
public class MemoryWritePolicy {

    private static final double AUTO_PROMOTE_CONFIDENCE = 0.7;

    private static final double MIN_ACCEPTABLE_CONFIDENCE = 0.55;

    public boolean shouldAutoPromote(MemoryCandidateRpcRequest request) {
        double confidence = confidence(request.getConfidence());
        return confidence >= AUTO_PROMOTE_CONFIDENCE && !Boolean.TRUE.equals(request.getRequiresHumanReview());
    }

    public boolean shouldReject(MemoryCandidateRpcRequest request) {
        return confidence(request.getConfidence()) < MIN_ACCEPTABLE_CONFIDENCE;
    }

    public double confidence(Double value) {
        if (value == null) {
            return 0.8;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
