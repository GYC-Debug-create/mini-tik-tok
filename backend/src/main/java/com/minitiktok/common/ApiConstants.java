package com.minitiktok.common;

/**
 * 全组共享常量。公开路径白名单由 A 维护；B 的 JwtAuthFilter 应调用 {@link #isPublicPath(String)}。
 */
public final class ApiConstants {

    public static final String ATTR_USER_ID = "currentUserId";

    private ApiConstants() {
    }

    /**
     * @param path {@link jakarta.servlet.http.HttpServletRequest#getRequestURI()}，不含 query
     */
    public static boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/logout")
                || path.equals("/api/health");
    }
}
