/**
 * <b>成员 B 负责</b>：用户认证与 JWT 全套实现。
 * <ul>
 *   <li>{@code AuthController} — register / login / logout</li>
 *   <li>{@code UserService} — BCrypt、用户表读写</li>
 *   <li>{@code JwtUtil} — 生成与解析 Token</li>
 *   <li>{@code JwtAuthFilter} + {@code AuthFilterConfig} — 鉴权 Filter（Order 在 RequestLogFilter 之后）</li>
 * </ul>
 * 规范见 {@code docs/MEMBER-B-认证与JWT.md}。
 */
package com.minitiktok.auth;
