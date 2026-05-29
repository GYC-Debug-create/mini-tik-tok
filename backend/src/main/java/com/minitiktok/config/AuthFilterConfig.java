package com.minitiktok.config;

import com.minitiktok.filter.JwtAuthFilter;
import com.minitiktok.util.JwtUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 成员 B：注册 JwtAuthFilter。
 * Order = FilterOrders.JWT_AUTH（HIGHEST_PRECEDENCE + 1），
 * 紧挨 A 的 RequestLogFilter（HIGHEST_PRECEDENCE）内侧。
 */
@Configuration
public class AuthFilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter(JwtUtil jwtUtil) {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>(
                new JwtAuthFilter(jwtUtil));
        bean.addUrlPatterns("/api/*");
        bean.setOrder(FilterOrders.JWT_AUTH);
        return bean;
    }
}
