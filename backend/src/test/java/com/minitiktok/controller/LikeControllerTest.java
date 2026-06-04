package com.minitiktok.controller;

import com.minitiktok.common.ApiConstants;
import com.minitiktok.common.GlobalExceptionHandler;
import com.minitiktok.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LikeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LikeService likeService;

    @BeforeEach
    void setUp() {
        LikeController controller = new LikeController(likeService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/videos/{id}/like 成功 → 200")
    void likeSuccess() throws Exception {
        mockMvc.perform(post("/api/videos/1/like")
                        .requestAttr(ApiConstants.ATTR_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(likeService).like(1L, 1L);
    }
}
