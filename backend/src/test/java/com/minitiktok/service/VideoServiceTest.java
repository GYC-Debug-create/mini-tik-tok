package com.minitiktok.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minitiktok.common.BusinessException;
import com.minitiktok.common.PageResult;
import com.minitiktok.common.VideoConstants;
import com.minitiktok.entity.Video;
import com.minitiktok.mapper.VideoMapper;
import com.minitiktok.video.dto.MyVideoItem;
import com.minitiktok.video.dto.PublishVideoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoMapper videoMapper;

    private VideoService videoService;

    @BeforeEach
    void setUp() {
        videoService = new VideoService(videoMapper);
    }

    @Test
    @DisplayName("发布视频 → 插入 status=0 并返回数据")
    void publishSuccess() {
        doAnswer(invocation -> {
            Video video = invocation.getArgument(0);
            video.setId(10L);
            return 1;
        }).when(videoMapper).insert(any(Video.class));

        PublishVideoRequest request = new PublishVideoRequest();
        request.setTitle("我的视频");
        request.setVideoUrl("https://example.com/v.mp4");

        Map<String, Object> result = videoService.publish(1L, request);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoMapper).insert(captor.capture());
        Video saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getTitle()).isEqualTo("我的视频");
        assertThat(saved.getStatus()).isEqualTo(VideoConstants.STATUS_NORMAL);
        assertThat(saved.getLikeCount()).isEqualTo(0);

        assertThat(result.get("id")).isEqualTo(10L);
        assertThat(result.get("title")).isEqualTo("我的视频");
    }

    @Test
    @DisplayName("我的视频列表 → 分页参数归一化")
    void listMyVideosNormalizesPagination() {
        Video video = new Video();
        video.setId(1L);
        video.setTitle("t");
        video.setVideoUrl("u");
        video.setLikeCount(0);
        video.setCreatedAt(LocalDateTime.now());

        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of(video));
        page.setTotal(1);

        when(videoMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResult<MyVideoItem> result = videoService.listMyVideos(1L, 0, 200);

        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getTitle()).isEqualTo("t");
    }

    @Test
    @DisplayName("删除不存在视频 → 404")
    void deleteVideoNotFound() {
        when(videoMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> videoService.deleteVideo(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);

        verify(videoMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("删除他人视频 → 403")
    void deleteVideoForbidden() {
        Video video = new Video();
        video.setId(1L);
        video.setUserId(2L);
        video.setStatus(VideoConstants.STATUS_NORMAL);
        when(videoMapper.selectById(1L)).thenReturn(video);

        assertThatThrownBy(() -> videoService.deleteVideo(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403)
                .hasMessage("无权删除该视频");

        verify(videoMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("重复删除已删视频 → 幂等不更新")
    void deleteVideoIdempotent() {
        Video video = new Video();
        video.setId(1L);
        video.setUserId(1L);
        video.setStatus(VideoConstants.STATUS_DELETED);
        when(videoMapper.selectById(1L)).thenReturn(video);

        videoService.deleteVideo(1L, 1L);

        verify(videoMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("删除本人视频 → 软删除 status=1")
    void deleteVideoSuccess() {
        Video video = new Video();
        video.setId(1L);
        video.setUserId(1L);
        video.setStatus(VideoConstants.STATUS_NORMAL);
        when(videoMapper.selectById(1L)).thenReturn(video);

        videoService.deleteVideo(1L, 1L);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VideoConstants.STATUS_DELETED);
    }
}
