package com.minitiktok.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 绑定 application.yml 中 jwt.* 配置。
 * B 的 JwtUtil 通过本类获取 secret 和 expiration-days。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HMAC-SHA256 签名密钥，至少 32 字符 */
    private String secret;

    /** Token 过期天数，默认 7 */
    private int expirationDays = 7;
}
