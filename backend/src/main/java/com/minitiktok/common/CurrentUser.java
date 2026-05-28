package com.minitiktok.common;

import com.minitiktok.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 供 B/C/D 在 Controller 中获取当前登录用户 ID。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long requireUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(ApiConstants.ATTR_USER_ID);
        if (userId instanceof Long longValue) {
            return longValue;
        }
        throw new BusinessException(401, "未登录或Token无效/过期");
    }
}
