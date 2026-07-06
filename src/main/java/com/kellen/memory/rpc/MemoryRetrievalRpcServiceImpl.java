package com.kellen.memory.rpc;

import com.kellen.memory.service.MemoryRetrievalService;
import com.kellen.rpc.memory.MemoryRetrievalRpcDTO;
import com.kellen.rpc.memory.MemoryRetrievalRpcRequest;
import com.kellen.rpc.memory.MemoryRetrievalRpcService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * AI 记忆召回 RPC Provider。
 */
@DubboService
public class MemoryRetrievalRpcServiceImpl implements MemoryRetrievalRpcService {

    private final MemoryRetrievalService memoryRetrievalService;

    public MemoryRetrievalRpcServiceImpl(MemoryRetrievalService memoryRetrievalService) {
        this.memoryRetrievalService = memoryRetrievalService;
    }

    @Override
    public MemoryRetrievalRpcDTO retrieve(MemoryRetrievalRpcRequest request) {
        return memoryRetrievalService.retrieve(request);
    }
}
