package com.kellen.memory.rpc;

import com.kellen.memory.service.MemoryShortMemoryService;
import com.kellen.rpc.memory.MemoryShortMemoryRpcService;
import com.kellen.rpc.memory.MemoryShortMessageRpcRequest;
import com.kellen.rpc.memory.MemoryShortSummaryRpcRequest;
import com.kellen.rpc.memory.MemoryWriteRpcDTO;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * AI 短期会话记忆 RPC Provider。
 */
@DubboService
public class MemoryShortMemoryRpcServiceImpl implements MemoryShortMemoryRpcService {

    private final MemoryShortMemoryService memoryShortMemoryService;

    public MemoryShortMemoryRpcServiceImpl(MemoryShortMemoryService memoryShortMemoryService) {
        this.memoryShortMemoryService = memoryShortMemoryService;
    }

    @Override
    public MemoryWriteRpcDTO appendMessage(MemoryShortMessageRpcRequest request) {
        return memoryShortMemoryService.appendMessage(request);
    }

    @Override
    public MemoryWriteRpcDTO upsertSummary(MemoryShortSummaryRpcRequest request) {
        return memoryShortMemoryService.upsertSummary(request);
    }
}
