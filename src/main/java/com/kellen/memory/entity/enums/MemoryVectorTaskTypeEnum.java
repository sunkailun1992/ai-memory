package com.kellen.memory.entity.enums;

/**
 * Qdrant 派生索引任务类型。
 */
public enum MemoryVectorTaskTypeEnum {

    UPSERT("UPSERT", "新增或更新向量 point"),
    DELETE("DELETE", "删除向量 point"),
    REBUILD("REBUILD", "重建索引批次");

    private final String code;

    private final String description;

    MemoryVectorTaskTypeEnum(String code, String description) {
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
