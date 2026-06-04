package com.minitiktok.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogMaskUtilTest {

    @Test
    @DisplayName("password 字段脱敏为 ***")
    void masksPasswordField() {
        String raw = "{\"username\":\"alice\",\"password\":\"123456\"}";
        assertThat(LogMaskUtil.mask(raw)).contains("\"password\":\"***\"");
        assertThat(LogMaskUtil.mask(raw)).doesNotContain("123456");
    }

    @Test
    @DisplayName("token 字段与 Bearer 脱敏")
    void masksTokenAndBearer() {
        String raw = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9\"} Authorization: Bearer eyJ.secret.part";
        String masked = LogMaskUtil.mask(raw);
        assertThat(masked).contains("\"token\":\"***\"");
        assertThat(masked).doesNotContain("eyJ.secret.part");
        assertThat(masked).contains("Authorization:");
        assertThat(masked).doesNotContain("Bearer eyJ");
    }

    @Test
    @DisplayName("响应体截断前也会脱敏")
    void truncateResponseAppliesMask() {
        String longBody = "{\"password\":\"secret\",\"data\":\"" + "x".repeat(1200) + "\"}";
        String truncated = LogMaskUtil.truncateResponse(longBody);
        assertThat(truncated).hasSizeLessThanOrEqualTo(1000);
        assertThat(truncated).doesNotContain("secret");
    }
}
