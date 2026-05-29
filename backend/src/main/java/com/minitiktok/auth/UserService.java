package com.minitiktok.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minitiktok.common.BusinessException;
import com.minitiktok.entity.User;
import com.minitiktok.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户认证服务：注册（BCrypt 加密）、登录校验。
 * 由 B 实现，供 AuthController 调用。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 注册新用户。
     *
     * @param username 用户名（唯一）
     * @param password 明文密码
     * @return 创建后的 User（含 id、createdAt）
     * @throws BusinessException 用户名重复 → 409
     */
    public User register(String username, String password) {
        // 查重
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count != null && count > 0) {
            throw new BusinessException(409, "用户名已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    /**
     * 登录校验。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return User 实体
     * @throws BusinessException 用户名或密码错误 → 401
     */
    public User login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return user;
    }
}
