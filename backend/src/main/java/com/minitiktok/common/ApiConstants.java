package com.minitiktok.common;

public final class ApiConstants {

    public static final String ATTR_USER_ID = "currentUserId";

    private ApiConstants() {
    }

    public static boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/logout")
                || path.equals("/api/health");
    }
}
