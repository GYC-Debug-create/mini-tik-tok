package com.minitiktok.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minitiktok.common.ApiConstants;
import com.minitiktok.common.BusinessException;
import com.minitiktok.common.GlobalExceptionHandler;
import com.minitiktok.common.PageResult;
import com.minitiktok.service.OssService;
import com.minitiktok.service.VideoService;
import com.minitiktok.video.dto.MyVideoItem;
import com.minitiktok.video.dto.PublishVideoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private VideoService videoService;

    @Mock
    private OssService ossService;

    @BeforeEach
    void setUp() {
        VideoController controller = new VideoController(videoService, ossService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/videos/upload 成功 → 200 + videoUrl")
    void uploadVideoSuccess() throws Exception {
        when(ossService.upload(any(), eq(1L)))
                .thenReturn("https://java-super-project.oss-cn-beijing.aliyuncs.com/videos/1/test.mp4");

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "fake-video".getBytes());

        mockMvc.perform(multipart("/api/videos/upload")
                        .file(file)
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.videoUrl").value(
                        "https://java-super-project.oss-cn-beijing.aliyuncs.com/videos/1/test.mp4"));
    }

    @Test
    @DisplayName("POST /api/videos 成功 → 201")
    void publishVideoSuccess() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 100L);
        data.put("title", "测试视频");
        data.put("videoUrl", "https://example.com/a.mp4");
        data.put("likeCount", 0);
        data.put("createdAt", LocalDateTime.of(2026, 6, 2, 10, 0));

        when(videoService.publish(eq(1L), any(PublishVideoRequest.class))).thenReturn(data);

        PublishVideoRequest request = new PublishVideoRequest();
        request.setTitle("测试视频");
        request.setVideoUrl("https://example.com/a.mp4");

        mockMvc.perform(post("/api/videos")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("发布成功"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.title").value("测试视频"));
    }

    @Test
    @DisplayName("POST /api/videos 标题为空 → 400")
    void publishVideoEmptyTitleReturns400() throws Exception {
        PublishVideoRequest request = new PublishVideoRequest();
        request.setTitle("");
        request.setVideoUrl("https://example.com/a.mp4");

        mockMvc.perform(post("/api/videos")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/me/videos 成功 → 200 + PageResult")
    void listMyVideosSuccess() throws Exception {
        MyVideoItem item = new MyVideoItem(1L, "标题", "https://example.com/a.mp4", 0,
                LocalDateTime.of(2026, 6, 2, 10, 0));
        PageResult<MyVideoItem> pageResult = PageResult.of(1, 10, 1, List.of(item));

        when(videoService.listMyVideos(1L, 1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/api/users/me/videos")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("标题"));
    }

    @Test
    @DisplayName("DELETE /api/videos/{id} 成功 → 200")
    void deleteVideoSuccess() throws Exception {
        mockMvc.perform(delete("/api/videos/1")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(videoService).deleteVideo(1L, 1L);
    }

    @Test
    @DisplayName("DELETE /api/videos/{id} 越权 → 403")
    void deleteVideoForbidden() throws Exception {
        doThrow(new BusinessException(403, "无权删除该视频"))
                .when(videoService).deleteVideo(1L, 99L);

        mockMvc.perform(delete("/api/videos/99")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权删除该视频"));
    }

    @Test
    @DisplayName("DELETE /api/videos/{id} 不存在 → 404")
    void deleteVideoNotFound() throws Exception {
        doThrow(new BusinessException(404, "视频不存在或已删除"))
                .when(videoService).deleteVideo(1L, 999L);

        mockMvc.perform(delete("/api/videos/999")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
