package com.minitiktok.auth;

import com.minitiktok.auth.dto.LoginRequest;
import com.minitiktok.auth.dto.RegisterRequest;
import com.minitiktok.common.BusinessException;
import com.minitiktok.common.Result;
import com.minitiktok.entity.User;
import com.minitiktok.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证 Controller：注册、登录、注销。
 * 由 B 实现，无需 Token 鉴权（公开路径）。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/auth/register
     * 注册新用户。密码 BCrypt 加密存储。
     * 成功 → 201，返回 userId、username、createdAt。
     */
    @PostMapping("/register")
    public ResponseEntity<Result<Map<String, Object>>> register(
            @Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword());

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("createdAt", user.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.created("注册成功", data));
    }

    /**
     * POST /api/auth/login
     * 登录，成功返回 JWT Token + userId + username。
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());

        return Result.ok("登录成功", data);
    }

    /**
     * POST /api/auth/logout
     * 注销占位。后端不做 Token 黑名单，前端清除 Token 即可。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok("已退出登录", null);
    }
}
