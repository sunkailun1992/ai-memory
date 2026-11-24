package com.kellen.memory.rpc;

import com.kellen.memory.service.MemoryRetrievalService;
import com.kellen.rpc.memory.MemoryRetrievalRpcDTO;
import com.kellen.rpc.memory.MemoryRetrievalRpcRequest;
import com.kellen.rpc.memory.MemoryRetrievalRpcService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;

/**
 * AI 记忆召回 RPC Provider。
 *
 * <p>{@code retrieve} 声明为幂等（{@code idempotent}）：它不改记忆本身，每次执行只写一条审计事件与一条检索评估记录。
 * 这两条是执行记录，不是业务状态，按 RFC 9110 §9.2.1 不影响方法的幂等；重试多出的记录如实反映又执行了一次，带同一 traceId。
 * 维护者 2026-11-24 09:09 定。utils 2.1.1 起的 Dubbo 调用端统一重试据此在请求发出后的连接类失败时重试它。</p>
 */
@DubboService(methods = @Method(name = "retrieve", parameters = {"idempotent", "true"}))
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
