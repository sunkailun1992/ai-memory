package com.kellen.memory.entity.enums;

/**
 * 记忆审计事件操作。
 */
public enum MemoryEventOperationEnum {

    APPEND_SHORT_MESSAGE("APPEND_SHORT_MESSAGE", "追加短记忆消息"),
    UPSERT_SHORT_SUMMARY("UPSERT_SHORT_SUMMARY", "更新短记忆摘要"),
    WRITE_CANDIDATE("WRITE_CANDIDATE", "写入候选记忆"),
    PROMOTE_CANDIDATE("PROMOTE_CANDIDATE", "晋升候选记忆"),
    RETRIEVE("RETRIEVE", "召回记忆"),
    ARCHIVE("ARCHIVE", "归档记忆"),
    DELETE("DELETE", "删除记忆"),
    MASK("MASK", "脱敏记忆"),
    REVOKE("REVOKE", "撤销记忆"),
    REBUILD_INDEX("REBUILD_INDEX", "重建索引");

    private final String code;

    private final String description;

    MemoryEventOperationEnum(String code, String description) {
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
