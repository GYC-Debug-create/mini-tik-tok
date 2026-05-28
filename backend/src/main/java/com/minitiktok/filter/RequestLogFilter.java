package com.minitiktok.filter;

import com.minitiktok.common.ApiConstants;
import com.minitiktok.service.RequestLogService;
import com.minitiktok.util.LogMaskUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class RequestLogFilter extends OncePerRequestFilter {

    private final RequestLogService requestLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            int costMs = (int) (System.currentTimeMillis() - start);
            Long userId = (Long) wrappedRequest.getAttribute(ApiConstants.ATTR_USER_ID);
            String requestParams = buildRequestParams(wrappedRequest);
            String responseBody = extractResponseBody(wrappedResponse);
            requestLogService.save(
                    userId,
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    requestParams,
                    wrappedResponse.getStatus(),
                    responseBody,
                    costMs
            );
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String buildRequestParams(ContentCachingRequestWrapper request) {
        String query = request.getQueryString();
        String body = readBody(request.getContentAsByteArray(), request.getCharacterEncoding());
        String combined;
        if (query != null && !query.isBlank() && body != null && !body.isBlank()) {
            combined = "query=" + query + "; body=" + body;
        } else if (query != null && !query.isBlank()) {
            combined = query;
        } else {
            combined = body;
        }
        return LogMaskUtil.truncateRequest(LogMaskUtil.mask(combined));
    }

    private String extractResponseBody(ContentCachingResponseWrapper response) {
        String body = readBody(response.getContentAsByteArray(), response.getCharacterEncoding());
        return LogMaskUtil.truncateResponse(body);
    }

    private String readBody(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return null;
        }
        String charset = encoding != null ? encoding : StandardCharsets.UTF_8.name();
        try {
            return new String(content, charset);
        } catch (Exception ex) {
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
