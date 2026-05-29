package com.minitiktok.util;

import com.minitiktok.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtUtil 单元测试 — 验证 Token 生成、解析、异常处理。
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-that-is-at-least-32-characters-long");
        props.setExpirationDays(7);
        jwtUtil = new JwtUtil(props);
    }

    @Test
    @DisplayName("生成 Token → 可解析出 userId 和 username")
    void generateAndParseToken() {
        // when
        String token = jwtUtil.generateToken(1001L, "alice");

        // then
        assertThat(token).isNotBlank();

        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.get("userId", Long.class)).isEqualTo(1001L);
        assertThat(claims.get("username", String.class)).isEqualTo("alice");
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("getUserId 从 Token 中正确提取 userId")
    void getUserIdReturnsCorrectId() {
        String token = jwtUtil.generateToken(42L, "bob");
        assertThat(jwtUtil.getUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("无效 Token → getUserId 返回 null")
    void invalidTokenReturnsNull() {
        assertThat(jwtUtil.getUserId("garbage.token.string")).isNull();
    }

    @Test
    @DisplayName("空字符串 Token → getUserId 返回 null")
    void emptyTokenReturnsNull() {
        assertThat(jwtUtil.getUserId("")).isNull();
    }

    @Test
    @DisplayName("不同密钥签名的 Token → parseToken 抛出 JwtException")
    void tokenSignedWithDifferentKeyThrowsException() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("another-different-secret-key-that-is-also-long-enough");
        otherProps.setExpirationDays(7);
        JwtUtil otherJwt = new JwtUtil(otherProps);

        String token = otherJwt.generateToken(1L, "eve");

        assertThatThrownBy(() -> jwtUtil.parseToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("不同用户生成不同 Token → getUserId 正确区分")
    void differentUsersGetDifferentTokens() {
        String tokenAlice = jwtUtil.generateToken(1L, "alice");
        String tokenBob = jwtUtil.generateToken(2L, "bob");

        assertThat(jwtUtil.getUserId(tokenAlice)).isEqualTo(1L);
        assertThat(jwtUtil.getUserId(tokenBob)).isEqualTo(2L);
    }

    @Test
    @DisplayName("Token 的 username claim 完整保留")
    void tokenContainsUsername() {
        String token = jwtUtil.generateToken(7L, "charlie");
        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.get("username", String.class)).isEqualTo("charlie");
    }
}
