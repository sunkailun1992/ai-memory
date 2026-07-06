package com.kellen.memory.entity.enums;

/**
 * 候选记忆审核状态。
 */
public enum MemoryCandidateStatusEnum {

    PENDING("PENDING", "待确认"),
    ACCEPTED("ACCEPTED", "已晋升"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;

    private final String description;

    MemoryCandidateStatusEnum(String code, String description) {
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
