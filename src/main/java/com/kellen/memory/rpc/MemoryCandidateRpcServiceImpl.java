package com.kellen.memory.rpc;

import com.kellen.memory.service.MemoryCandidateService;
import com.kellen.rpc.memory.MemoryCandidateRpcRequest;
import com.kellen.rpc.memory.MemoryCandidateRpcService;
import com.kellen.rpc.memory.MemoryWriteRpcDTO;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * AI 记忆候选写入 RPC Provider。
 */
@DubboService
public class MemoryCandidateRpcServiceImpl implements MemoryCandidateRpcService {

    private final MemoryCandidateService memoryCandidateService;

    public MemoryCandidateRpcServiceImpl(MemoryCandidateService memoryCandidateService) {
        this.memoryCandidateService = memoryCandidateService;
    }

    @Override
    public MemoryWriteRpcDTO submitCandidate(MemoryCandidateRpcRequest request) {
        return memoryCandidateService.submitCandidate(request);
    }
}
