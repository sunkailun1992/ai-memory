package com.kellen.memory.service;

import com.kellen.rpc.memory.MemoryCandidateRpcRequest;
import com.kellen.rpc.memory.MemoryWriteRpcDTO;

/**
 * 候选记忆写入服务。
 */
public interface MemoryCandidateService {

    MemoryWriteRpcDTO submitCandidate(MemoryCandidateRpcRequest request);
}
