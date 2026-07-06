package com.kellen.memory.service;

import com.kellen.memory.service.results.MemoryCapabilityResponse;

/**
 * AI 记忆服务能力边界查询服务。
 */
public interface MemoryCapabilityService {

    /**
     * 返回当前服务的能力边界。
     *
     * @return 能力边界响应。
     */
    MemoryCapabilityResponse capabilities();
}
