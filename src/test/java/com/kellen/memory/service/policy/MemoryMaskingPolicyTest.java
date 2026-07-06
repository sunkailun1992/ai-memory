package com.kellen.memory.service.policy;

import com.kellen.memory.entity.enums.MemorySensitivityLevelEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemoryMaskingPolicyTest {

    private final MemoryMaskingPolicy policy = new MemoryMaskingPolicy();

    @Test
    void shouldMaskMobileAndCertificateNumberByDefault() {
        String masked = policy.mask("手机号 13712349028，身份证 330106199001011219");

        assertEquals("手机号 137****9028，身份证 3301************19", masked);
    }

    @Test
    void shouldDetectSensitiveContent() {
        assertEquals(MemorySensitivityLevelEnum.SENSITIVE.code(), policy.sensitivityLevel("13712349028", null));
        assertEquals(MemorySensitivityLevelEnum.NORMAL.code(), policy.sensitivityLevel("患者表示没有不适", null));
    }
}
