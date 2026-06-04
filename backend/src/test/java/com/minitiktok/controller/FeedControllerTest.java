package com.minitiktok.controller;

import com.minitiktok.common.ApiConstants;
import com.minitiktok.common.GlobalExceptionHandler;
import com.minitiktok.dto.FeedVideoDTO;
import com.minitiktok.dto.PublisherDTO;
import com.minitiktok.service.FeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeedService feedService;

    @BeforeEach
    void setUp() {
        FeedController controller = new FeedController(feedService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/feed/videos/next 有推荐 → 200 + 视频")
    void nextVideoSuccess() throws Exception {
        FeedVideoDTO dto = new FeedVideoDTO(3L, "美食探店", "https://example.com/3.mp4", 72,
                LocalDateTime.now(), new PublisherDTO(1L, "alice"));
        when(feedService.getNextVideo(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/feed/videos/next")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.title").value("美食探店"));
    }

    @Test
    @DisplayName("GET /api/feed/videos/next 无更多 → 200 + data null")
    void nextVideoEmpty() throws Exception {
        when(feedService.getNextVideo(1L)).thenReturn(null);

        mockMvc.perform(get("/api/feed/videos/next")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("暂无更多推荐视频"));
    }

    @Test
    @DisplayName("GET /api/feed/videos/previous 无浏览记录 → 200 + 提示")
    void previousNotInVisitRecords() throws Exception {
        when(feedService.isInVisitRecords(1L, 99L)).thenReturn(false);

        mockMvc.perform(get("/api/feed/videos/previous")
                        .param("currentVideoId", "99")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("未找到该浏览记录"));
    }
}
