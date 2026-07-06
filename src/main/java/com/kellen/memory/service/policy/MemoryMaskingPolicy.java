package com.kellen.memory.service.policy;

import com.kellen.memory.entity.enums.MemorySensitivityLevelEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 记忆隐私识别和返回脱敏策略。
 */
@Component
public class MemoryMaskingPolicy {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("(?<!\\d)(1[3-9]\\d)\\d{4}(\\d{4})(?!\\d)");

    private static final Pattern CERT_PATTERN = Pattern.compile("(?<![0-9A-Za-z])(\\d{4})\\d{10,12}([0-9Xx]{2})(?![0-9A-Za-z])");

    public String sensitivityLevel(String content, String factsJson) {
        String merged = (content == null ? "" : content) + "\n" + (factsJson == null ? "" : factsJson);
        if (MOBILE_PATTERN.matcher(merged).find() || CERT_PATTERN.matcher(merged).find()) {
            return MemorySensitivityLevelEnum.SENSITIVE.code();
        }
        return MemorySensitivityLevelEnum.NORMAL.code();
    }

    public String maskForRecall(String value, boolean allowSensitive) {
        if (allowSensitive || !StringUtils.hasText(value)) {
            return value == null ? "" : value.trim();
        }
        return mask(value);
    }

    public String mask(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String masked = MOBILE_PATTERN.matcher(value).replaceAll("$1****$2");
        return CERT_PATTERN.matcher(masked).replaceAll("$1************$2");
    }
}
