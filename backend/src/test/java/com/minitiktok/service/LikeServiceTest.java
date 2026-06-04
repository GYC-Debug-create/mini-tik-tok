package com.minitiktok.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minitiktok.common.BusinessException;
import com.minitiktok.common.VideoConstants;
import com.minitiktok.entity.LikeRecord;
import com.minitiktok.entity.Video;
import com.minitiktok.mapper.LikeRecordMapper;
import com.minitiktok.mapper.VideoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRecordMapper likeRecordMapper;
    @Mock
    private VideoMapper videoMapper;

    private LikeService likeService;

    @BeforeEach
    void setUp() {
        likeService = new LikeService(likeRecordMapper, videoMapper);
    }

    @Test
    @DisplayName("首次点赞 → 插入记录并 like_count +1")
    void likeSuccess() {
        Video video = new Video();
        video.setId(1L);
        video.setStatus(VideoConstants.STATUS_NORMAL);
        video.setLikeCount(5);
        when(videoMapper.selectById(1L)).thenReturn(video);
        when(likeRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        likeService.like(1L, 1L);

        verify(likeRecordMapper).insert(any(LikeRecord.class));
        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoMapper).updateById(captor.capture());
        assertThat(captor.getValue().getLikeCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("重复点赞 → 幂等不更新计数")
    void likeIdempotent() {
        Video video = new Video();
        video.setId(1L);
        video.setStatus(VideoConstants.STATUS_NORMAL);
        video.setLikeCount(5);
        when(videoMapper.selectById(1L)).thenReturn(video);
        when(likeRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        likeService.like(1L, 1L);

        verify(likeRecordMapper, never()).insert(any());
        verify(videoMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("视频不存在 → 404")
    void likeVideoNotFound() {
        when(videoMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> likeService.like(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
    }
}
