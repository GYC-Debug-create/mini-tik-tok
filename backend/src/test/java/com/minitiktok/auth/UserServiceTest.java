package com.minitiktok.auth;

import com.minitiktok.common.BusinessException;
import com.minitiktok.entity.User;
import com.minitiktok.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试 — 验证注册/登录的业务逻辑与异常场景。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper);
    }

    // ========== register ==========

    @Test
    @DisplayName("注册成功 → 返回含 id 和 createdAt 的 User")
    void registerSuccess() {
        // given
        when(userMapper.selectCount(any())).thenReturn(0L);
        // insert 时设置自增 id
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1001L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        // when
        User result = userService.register("newuser", "password123");

        // then
        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPassword()).startsWith("$2a$"); // BCrypt 密文
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(userMapper).selectCount(any());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册时用户名已存在 → 409 BusinessException")
    void registerDuplicateUsernameThrows409() {
        // given
        when(userMapper.selectCount(any())).thenReturn(1L);

        // when & then
        assertThatThrownBy(() -> userService.register("dupuser", "password123"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 409)
                .hasMessage("用户名已被注册");

        verify(userMapper, never()).insert(any());
    }

    // ========== login ==========

    @Test
    @DisplayName("登录成功 → 返回 User")
    void loginSuccess() {
        // given
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User existed = new User();
        existed.setId(10L);
        existed.setUsername("alice");
        existed.setPassword(encoder.encode("123456"));

        when(userMapper.selectOne(any())).thenReturn(existed);

        // when
        User result = userService.login("alice", "123456");

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("登录时用户名不存在 → 401 BusinessException")
    void loginUserNotFoundThrows401() {
        // given
        when(userMapper.selectOne(any())).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> userService.login("nobody", "password"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 401)
                .hasMessage("用户名或密码错误");
    }

    @Test
    @DisplayName("登录时密码错误 → 401 BusinessException")
    void loginWrongPasswordThrows401() {
        // given
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User existed = new User();
        existed.setId(10L);
        existed.setUsername("alice");
        existed.setPassword(encoder.encode("correct-password"));

        when(userMapper.selectOne(any())).thenReturn(existed);

        // when & then
        assertThatThrownBy(() -> userService.login("alice", "wrong-password"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 401)
                .hasMessage("用户名或密码错误");
    }
}
