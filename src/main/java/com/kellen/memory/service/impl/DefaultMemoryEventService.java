package com.kellen.memory.service.impl;

import com.kellen.memory.entity.MemoryEventEntity;
import com.kellen.memory.mapper.MemoryEventMapper;
import com.kellen.memory.service.MemoryEventService;
import org.springframework.stereotype.Service;

/**
 * 默认记忆事件服务。
 */
@Service
public class DefaultMemoryEventService implements MemoryEventService {

    private final MemoryEventMapper eventMapper;

    public DefaultMemoryEventService(MemoryEventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    @Override
    public MemoryEventEntity record(MemoryEventEntity event) {
        eventMapper.save(event);
        return event;
    }
}
