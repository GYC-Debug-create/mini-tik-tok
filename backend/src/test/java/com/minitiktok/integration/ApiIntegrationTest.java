package com.minitiktok.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minitiktok.entity.RequestLog;
import com.minitiktok.mapper.RequestLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 全栈集成测试：Testcontainers MySQL + Spring 上下文 + JwtAuthFilter + RequestLogFilter。
 * 需要本机 Docker；无 Docker 时该测试类会失败。
 */
@SpringBootTest(properties = "spring.profiles.active=integration")
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Testcontainers(disabledWithoutDocker = true)
class ApiIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("douyin_lite")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("schema-integration.sql"),
                    "/docker-entrypoint-initdb.d/01-schema.sql");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestLogMapper requestLogMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanRequestLogs() {
        jdbcTemplate.execute("DELETE FROM request_logs");
    }

    @Test
    @DisplayName("健康检查 → database up")
    void healthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.database").value("up"));
    }

    @Test
    @DisplayName("无 Token 访问 Feed → 401 且 request_logs 记录 user_id=null")
    void feedWithoutTokenWritesRequestLog() throws Exception {
        mockMvc.perform(get("/api/feed/videos/next"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        RequestLog log = latestLog("/api/feed/videos/next");
        assertThat(log.getUserId()).isNull();
        assertThat(log.getResponseCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("登录写 request_logs：password/token 脱敏；带 Token 访问 Feed → 200")
    void loginMasksPasswordAndFeedWithToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        RequestLog loginLog = latestLog("/api/auth/login");
        assertThat(loginLog.getRequestParams()).contains("\"password\":\"***\"");
        assertThat(loginLog.getRequestParams()).doesNotContain("123456");
        if (loginLog.getResponseBody() != null) {
            assertThat(loginLog.getResponseBody()).doesNotContain("eyJ");
        }

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = root.get("data").get("token").asText();

        mockMvc.perform(get("/api/feed/videos/next")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        RequestLog feedLog = latestLog("/api/feed/videos/next");
        assertThat(feedLog.getUserId()).isEqualTo(1L);
        assertThat(feedLog.getResponseCode()).isEqualTo(200);
    }

    private RequestLog latestLog(String path) {
        LambdaQueryWrapper<RequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestLog::getPath, path)
                .orderByDesc(RequestLog::getId)
                .last("LIMIT 1");
        return requestLogMapper.selectOne(wrapper);
    }
}
