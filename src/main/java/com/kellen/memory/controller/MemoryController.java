package com.kellen.memory.controller;

import com.kellen.memory.service.MemoryCandidateService;
import com.kellen.memory.service.MemoryGovernanceService;
import com.kellen.memory.service.MemoryRetrievalService;
import com.kellen.memory.service.MemoryShortMemoryService;
import com.kellen.rpc.memory.MemoryCandidateRpcRequest;
import com.kellen.rpc.memory.MemoryGovernanceRpcDTO;
import com.kellen.rpc.memory.MemoryGovernanceRpcRequest;
import com.kellen.rpc.memory.MemoryRetrievalRpcDTO;
import com.kellen.rpc.memory.MemoryRetrievalRpcRequest;
import com.kellen.rpc.memory.MemoryShortMessageRpcRequest;
import com.kellen.rpc.memory.MemoryShortSummaryRpcRequest;
import com.kellen.rpc.memory.MemoryVectorRebuildRpcRequest;
import com.kellen.rpc.memory.MemoryWriteRpcDTO;
import com.kellen.utils.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 记忆 HTTP 调用入口。
 */
@RestController
@RequestMapping("/api/ai-memory")
@Tag(name = "AI Memory", description = "AI 短期/长期记忆读写和治理")
public class MemoryController {

    private final MemoryShortMemoryService shortMemoryService;

    private final MemoryCandidateService candidateService;

    private final MemoryRetrievalService retrievalService;

    private final MemoryGovernanceService governanceService;

    public MemoryController(MemoryShortMemoryService shortMemoryService,
                            MemoryCandidateService candidateService,
                            MemoryRetrievalService retrievalService,
                            MemoryGovernanceService governanceService) {
        this.shortMemoryService = shortMemoryService;
        this.candidateService = candidateService;
        this.retrievalService = retrievalService;
        this.governanceService = governanceService;
    }

    @PostMapping("/short-messages")
    @Operation(summary = "追加短期会话记忆")
    public ApiResponse<MemoryWriteRpcDTO> appendShortMessage(@RequestBody MemoryShortMessageRpcRequest request) {
        return ApiResponse.success(shortMemoryService.appendMessage(request));
    }

    @PostMapping("/short-summaries")
    @Operation(summary = "更新短期会话滚动摘要")
    public ApiResponse<MemoryWriteRpcDTO> upsertShortSummary(@RequestBody MemoryShortSummaryRpcRequest request) {
        return ApiResponse.success(shortMemoryService.upsertSummary(request));
    }

    @PostMapping("/candidates")
    @Operation(summary = "提交候选长期记忆")
    public ApiResponse<MemoryWriteRpcDTO> submitCandidate(@RequestBody MemoryCandidateRpcRequest request) {
        return ApiResponse.success(candidateService.submitCandidate(request));
    }

    @PostMapping("/retrievals")
    @Operation(summary = "召回可注入模型的患者记忆上下文")
    public ApiResponse<MemoryRetrievalRpcDTO> retrieve(@RequestBody MemoryRetrievalRpcRequest request) {
        return ApiResponse.success(retrievalService.retrieve(request));
    }

    @PostMapping("/governance")
    @Operation(summary = "执行记忆删除、归档、脱敏或撤销")
    public ApiResponse<MemoryGovernanceRpcDTO> govern(@RequestBody MemoryGovernanceRpcRequest request) {
        return ApiResponse.success(governanceService.govern(request));
    }

    @PostMapping("/vector-rebuild-tasks")
    @Operation(summary = "创建 Qdrant 派生索引重建任务")
    public ApiResponse<MemoryGovernanceRpcDTO> rebuildVectorIndex(@RequestBody MemoryVectorRebuildRpcRequest request) {
        return ApiResponse.success(governanceService.rebuildVectorIndex(request));
    }
}
