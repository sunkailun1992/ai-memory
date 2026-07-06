package com.kellen.memory.entity.enums;

/**
 * Qdrant 派生索引任务状态。
 */
public enum MemoryVectorTaskStatusEnum {

    PENDING("PENDING", "待执行"),
    RUNNING("RUNNING", "执行中"),
    SUCCEEDED("SUCCEEDED", "已完成"),
    FAILED("FAILED", "已失败"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;

    private final String description;

    MemoryVectorTaskStatusEnum(String code, String description) {
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
