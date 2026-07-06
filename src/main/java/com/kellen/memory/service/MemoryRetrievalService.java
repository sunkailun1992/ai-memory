package com.kellen.memory.service;

import com.kellen.rpc.memory.MemoryRetrievalRpcDTO;
import com.kellen.rpc.memory.MemoryRetrievalRpcRequest;

/**
 * 记忆召回服务。
 */
public interface MemoryRetrievalService {

    MemoryRetrievalRpcDTO retrieve(MemoryRetrievalRpcRequest request);
}
