package com.minitiktok.config;

import org.springframework.core.Ordered;

/**
 * Filter 注册顺序常量（全组遵守）。B 在 {@code AuthFilterConfig} 中使用 {@link #JWT_AUTH}。
 */
public final class FilterOrders {

    /** 请求日志 Filter，最外层，可记录 401 等鉴权失败响应 */
    public static final int REQUEST_LOG = Ordered.HIGHEST_PRECEDENCE;

    /** JWT 鉴权 Filter，紧挨日志 Filter 之内侧 */
    public static final int JWT_AUTH = Ordered.HIGHEST_PRECEDENCE + 1;

    private FilterOrders() {
    }
}
