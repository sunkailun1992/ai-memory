package com.kellen.memory.rpc;

import com.kellen.memory.service.MemoryGovernanceService;
import com.kellen.rpc.memory.MemoryGovernanceRpcDTO;
import com.kellen.rpc.memory.MemoryGovernanceRpcRequest;
import com.kellen.rpc.memory.MemoryGovernanceRpcService;
import com.kellen.rpc.memory.MemoryVectorRebuildRpcRequest;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * AI 记忆治理 RPC Provider。
 */
@DubboService
public class MemoryGovernanceRpcServiceImpl implements MemoryGovernanceRpcService {

    private final MemoryGovernanceService memoryGovernanceService;

    public MemoryGovernanceRpcServiceImpl(MemoryGovernanceService memoryGovernanceService) {
        this.memoryGovernanceService = memoryGovernanceService;
    }

    @Override
    public MemoryGovernanceRpcDTO govern(MemoryGovernanceRpcRequest request) {
        return memoryGovernanceService.govern(request);
    }

    @Override
    public MemoryGovernanceRpcDTO rebuildVectorIndex(MemoryVectorRebuildRpcRequest request) {
        return memoryGovernanceService.rebuildVectorIndex(request);
    }
}
