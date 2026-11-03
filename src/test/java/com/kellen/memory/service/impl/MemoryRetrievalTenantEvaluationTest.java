package com.kellen.memory.service.impl;

import com.kellen.memory.entity.MemoryRetrievalEvalEntity;
import com.kellen.memory.mapper.*;
import com.kellen.memory.service.MemoryEventService;
import com.kellen.memory.service.policy.*;
import com.kellen.rpc.memory.MemoryRetrievalRpcRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.mockito.ArgumentCaptor;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/** 真实 Service / 策略 Bean 保留调用方租户到评估记录；不连接业务数据库。 */
@SpringJUnitConfig(MemoryRetrievalTenantEvaluationTest.Config.class)
class MemoryRetrievalTenantEvaluationTest {
    @Autowired DefaultMemoryRetrievalService service;
    @Autowired MemoryRetrievalEvalMapper evaluations;
    @Autowired MemoryLongFactMapper facts;

    @Test void evaluationRetainsTheRequestTenantEvenOnACacheMiss() {
        when(facts.findRecallable(anyString(), anyString(), anyString(), anyBoolean(), anyInt())).thenReturn(List.of());
        MemoryRetrievalRpcRequest request = new MemoryRetrievalRpcRequest();
        request.setTenantId("tenant-fixture");
        request.setSourceTenantId("source-fixture");
        request.setPatientId("subject-fixture");
        request.setCaller("test-client");
        request.setPurpose("test-retrieval");
        assertThat(service.retrieve(request).isMatched()).isFalse();
        ArgumentCaptor<MemoryRetrievalEvalEntity> captured = ArgumentCaptor.forClass(MemoryRetrievalEvalEntity.class);
        verify(evaluations).save(captured.capture());
        assertThat(captured.getValue().getTenantId()).isEqualTo("tenant-fixture");
        assertThat(captured.getValue().getSourceTenantId()).isEqualTo("source-fixture");
    }

    @Configuration(proxyBeanMethods = false)
    @Import({DefaultMemoryRetrievalService.class, MemoryAuthorizationPolicy.class, MemoryRetrievalPolicy.class,
            MemoryMaskingPolicy.class, MemoryTextPolicy.class})
    static class Config {
        @Bean MemoryShortSummaryMapper summaries() { return mock(MemoryShortSummaryMapper.class); }
        @Bean MemoryShortMessageMapper messages() { return mock(MemoryShortMessageMapper.class); }
        @Bean MemoryLongFactMapper facts() { return mock(MemoryLongFactMapper.class); }
        @Bean MemoryRetrievalEvalMapper evaluations() { return mock(MemoryRetrievalEvalMapper.class); }
        @Bean MemoryEventService events() { return mock(MemoryEventService.class); }
    }
}
