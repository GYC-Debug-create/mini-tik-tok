package com.minitiktok.config;

import com.minitiktok.filter.RequestLogFilter;
import com.minitiktok.service.RequestLogService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 成员 A：仅注册请求日志 Filter。
 * 成员 B：在 {@code AuthFilterConfig} 中注册 {@code JwtAuthFilter}，Order = {@link Ordered#HIGHEST_PRECEDENCE} + 1。
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestLogFilter> requestLogFilter(RequestLogService requestLogService) {
        FilterRegistrationBean<RequestLogFilter> bean = new FilterRegistrationBean<>(
                new RequestLogFilter(requestLogService));
        bean.addUrlPatterns("/api/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
