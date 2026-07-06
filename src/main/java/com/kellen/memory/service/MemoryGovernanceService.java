package com.kellen.memory.service;

import com.kellen.rpc.memory.MemoryGovernanceRpcDTO;
import com.kellen.rpc.memory.MemoryGovernanceRpcRequest;
import com.kellen.rpc.memory.MemoryVectorRebuildRpcRequest;

/**
 * 记忆治理服务。
 */
public interface MemoryGovernanceService {

    MemoryGovernanceRpcDTO govern(MemoryGovernanceRpcRequest request);

    MemoryGovernanceRpcDTO rebuildVectorIndex(MemoryVectorRebuildRpcRequest request);
}
