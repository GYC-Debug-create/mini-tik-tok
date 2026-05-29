package com.minitiktok.filter;

import com.minitiktok.common.ApiConstants;
import com.minitiktok.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JwtAuthFilter 单元测试 — 验证公开路径放行、无 Token 拒签、有效 Token 注入。
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtUtil);
    }

    // ========== 公开路径放行 ==========

    @Test
    @DisplayName("访问 /api/auth/register → 放行，不校验 Token")
    void publicPathRegisterPasses() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/register");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).getWriter();
    }

    @Test
    @DisplayName("访问 /api/auth/login → 放行")
    void publicPathLoginPasses() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("访问 /api/auth/logout → 放行")
    void publicPathLogoutPasses() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/logout");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("访问 /api/health → 放行")
    void healthPathPasses() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/health");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ========== 非 /api 路径放行 ==========

    @Test
    @DisplayName("访问非 /api 路径 → 放行")
    void nonApiPathPasses() throws Exception {
        when(request.getRequestURI()).thenReturn("/favicon.ico");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ========== 无 Token → 401 ==========

    @Test
    @DisplayName("访问受保护接口且无 Authorization 头 → 401")
    void noAuthHeaderReturns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/videos");
        when(request.getHeader("Authorization")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(stringWriter.toString()).contains("401").contains("未登录或Token无效/过期");
    }

    @Test
    @DisplayName("Authorization 头不是 Bearer 开头 → 401")
    void nonBearerAuthReturns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/videos");
        when(request.getHeader("Authorization")).thenReturn("Basic xxx");

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(stringWriter.toString()).contains("401");
    }

    // ========== 无效 Token → 401 ==========

    @Test
    @DisplayName("Authorization: Bearer 无效 Token → 401")
    void invalidTokenReturns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/videos");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtUtil.getUserId("invalid-token")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    // ========== 有效 Token → 放行 ==========

    @Test
    @DisplayName("Authorization: Bearer 有效 Token → 注入 userId 并放行")
    void validTokenPasses() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/videos");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.getUserId("valid.jwt.token")).thenReturn(42L);

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute(ApiConstants.ATTR_USER_ID, 42L);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
