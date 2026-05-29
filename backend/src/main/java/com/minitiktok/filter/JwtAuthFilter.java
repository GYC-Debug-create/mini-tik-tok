package com.minitiktok.filter;

import com.minitiktok.common.ApiConstants;
import com.minitiktok.common.Result;
import com.minitiktok.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 鉴权 Filter。
 * 由 B 实现。
 * - 公开路径（register/login/logout/health）直接放行
 * - 其余 /api/** 请求必须携带有效 Bearer Token
 * - 校验通过后将 userId 写入 request attribute（供 CurrentUser 使用）
 * - 校验失败返回 401 JSON
 *
 * Order = FilterOrders.JWT_AUTH（紧挨 RequestLogFilter 内侧）。
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 公开路径直接放行
        if (ApiConstants.isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 非 /api/** 路径直接放行（如静态资源）
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取 Authorization Header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或Token无效/过期");
            return;
        }

        String token = authHeader.substring(7); // 去掉 "Bearer "
        try {
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                writeUnauthorized(response, "未登录或Token无效/过期");
                return;
            }
            // 将 userId 注入请求上下文，供 Controller 通过 CurrentUser 获取
            request.setAttribute(ApiConstants.ATTR_USER_ID, userId);
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            writeUnauthorized(response, "未登录或Token无效/过期");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        response.getWriter().write(json);
    }
}
