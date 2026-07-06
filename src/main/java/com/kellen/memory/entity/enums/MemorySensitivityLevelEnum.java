package com.kellen.memory.entity.enums;

/**
 * 记忆敏感等级。
 */
public enum MemorySensitivityLevelEnum {

    NORMAL("NORMAL", "普通"),
    SENSITIVE("SENSITIVE", "含隐私字段"),
    RESTRICTED("RESTRICTED", "受限明文");

    private final String code;

    private final String description;

    MemorySensitivityLevelEnum(String code, String description) {
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
