package com.kellen.memory.service;

import com.kellen.rpc.memory.MemoryShortMessageRpcRequest;
import com.kellen.rpc.memory.MemoryShortSummaryRpcRequest;
import com.kellen.rpc.memory.MemoryWriteRpcDTO;

/**
 * 短期会话记忆服务。
 */
public interface MemoryShortMemoryService {

    MemoryWriteRpcDTO appendMessage(MemoryShortMessageRpcRequest request);

    MemoryWriteRpcDTO upsertSummary(MemoryShortSummaryRpcRequest request);
}
