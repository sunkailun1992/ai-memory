package com.kellen.memory.service.results;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * AI 记忆服务能力边界响应。
 *
 * @param serviceName 当前服务名。
 * @param owns        ai-memory 负责的能力。
 * @param excludes    ai-memory 明确不负责的能力。
 */
@Schema(description = "AI 记忆服务能力边界响应")
public record MemoryCapabilityResponse(
        @Schema(description = "当前服务名")
        String serviceName,
        @Schema(description = "ai-memory 负责的能力")
        List<String> owns,
        @Schema(description = "ai-memory 明确不负责的能力")
        List<String> excludes
) {
}
