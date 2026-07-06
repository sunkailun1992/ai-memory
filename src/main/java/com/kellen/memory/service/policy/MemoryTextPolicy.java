package com.kellen.memory.service.policy;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 记忆文本通用规范化策略。
 */
@Component
public class MemoryTextPolicy {

    public String required(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    public String text(String value) {
        return value == null ? "" : value.trim();
    }

    public int bounded(Integer value, int fallback, int min, int max) {
        int actual = value == null ? fallback : value;
        return Math.max(min, Math.min(max, actual));
    }
}
