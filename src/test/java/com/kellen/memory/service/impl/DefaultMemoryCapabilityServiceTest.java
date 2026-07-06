package com.kellen.memory.service.impl;

import com.kellen.memory.service.results.MemoryCapabilityResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMemoryCapabilityServiceTest {

    @Test
    void shouldDescribeMemoryServiceBoundaries() {
        DefaultMemoryCapabilityService service = new DefaultMemoryCapabilityService();

        MemoryCapabilityResponse result = service.capabilities();

        assertEquals("ai-memory", result.serviceName());
        assertTrue(result.owns().contains("MySQL authoritative memory state"));
        assertTrue(result.owns().contains("Qdrant derived semantic index for long-term memory"));
        assertTrue(result.excludes().contains("model inference"));
        assertTrue(result.excludes().contains("public knowledge-base RAG"));
    }
}
