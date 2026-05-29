package com.minitiktok.util;

import com.minitiktok.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析工具。
 * 由 B 实现，供 AuthController（登录）和 JwtAuthFilter（验票）使用。
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token，Payload 含 userId、username，过期时间由配置决定。
     */
    public String generateToken(Long userId, String username) {
        long expMs = jwtProperties.getExpirationDays() * 24L * 60 * 60 * 1000;
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token，返回 Claims。校验签名和过期时间。
     *
     * @param token Bearer 后面的纯 Token 字符串
     * @return Claims 包含 userId、username、exp
     * @throws JwtException 签名无效或已过期
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取 userId，方便快捷获取。
     *
     * @return userId，解析失败返回 null
     */
    public Long getUserId(String token) {
        try {
            return parseToken(token).get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }
}
