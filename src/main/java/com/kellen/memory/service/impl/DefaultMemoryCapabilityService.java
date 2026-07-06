package com.kellen.memory.service.impl;

import com.kellen.memory.service.MemoryCapabilityService;
import com.kellen.memory.service.results.MemoryCapabilityResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认 AI 记忆能力边界服务。
 */
@Service
public class DefaultMemoryCapabilityService implements MemoryCapabilityService {

    @Override
    public MemoryCapabilityResponse capabilities() {
        return new MemoryCapabilityResponse(
                "ai-memory",
                List.of(
                        "short-term conversation memory policy",
                        "long-term patient memory policy",
                        "memory retrieval authorization",
                        "memory write candidate validation",
                        "memory delete/archive/masking governance",
                        "MySQL authoritative memory state",
                        "Redis cache and lock coordination",
                        "Qdrant derived semantic index for long-term memory"
                ),
                List.of(
                        "mini-program login and patient selection",
                        "conversation display history",
                        "questionnaire workflow state",
                        "model inference",
                        "public knowledge-base RAG"
                )
        );
    }
}
