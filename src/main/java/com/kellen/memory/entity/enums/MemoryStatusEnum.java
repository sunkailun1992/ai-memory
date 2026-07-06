package com.kellen.memory.entity.enums;

/**
 * 长期记忆权威状态。
 */
public enum MemoryStatusEnum {

    ACTIVE("ACTIVE", "可召回"),
    ARCHIVED("ARCHIVED", "已归档"),
    MASKED("MASKED", "已脱敏"),
    DELETED("DELETED", "已删除"),
    REVOKED("REVOKED", "已撤销");

    private final String code;

    private final String description;

    MemoryStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }
}
