package com.kellen.memory.controller;

import com.kellen.memory.service.MemoryCapabilityService;
import com.kellen.memory.service.results.MemoryCapabilityResponse;
import com.kellen.utils.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 记忆服务能力说明入口。
 *
 * <p>当前仓库先提供独立服务骨架和职责边界，真实短期记忆、长期记忆、候选记忆写入、
 * 召回、删除、归档、脱敏和授权接口在后续迭代补齐。</p>
 */
@RestController
@RequestMapping("/api/ai-memory")
@Tag(name = "AI Memory", description = "AI 短期/长期记忆能力")
public class MemoryCapabilityController {

    private final MemoryCapabilityService memoryCapabilityService;

    public MemoryCapabilityController(MemoryCapabilityService memoryCapabilityService) {
        this.memoryCapabilityService = memoryCapabilityService;
    }

    /**
     * 查询当前 ai-memory 服务能力边界。
     *
     * @return 服务能力说明。
     */
    @GetMapping("/capabilities")
    @Operation(summary = "查询 AI 记忆服务能力边界", description = "返回 ai-memory 当前负责和禁止负责的能力说明")
    public ApiResponse<MemoryCapabilityResponse> capabilities() {
        return ApiResponse.success(memoryCapabilityService.capabilities());
    }
}
