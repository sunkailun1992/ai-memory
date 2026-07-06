package com.kellen.memory.service;

import com.kellen.memory.entity.MemoryEventEntity;

/**
 * 记忆审计事件服务。
 */
public interface MemoryEventService {

    MemoryEventEntity record(MemoryEventEntity event);
}
