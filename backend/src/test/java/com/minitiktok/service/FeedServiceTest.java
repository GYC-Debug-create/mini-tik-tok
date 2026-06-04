package com.minitiktok.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minitiktok.common.VideoConstants;
import com.minitiktok.dto.FeedVideoDTO;
import com.minitiktok.entity.User;
import com.minitiktok.entity.Video;
import com.minitiktok.entity.VisitRecord;
import com.minitiktok.mapper.UserMapper;
import com.minitiktok.mapper.VideoMapper;
import com.minitiktok.mapper.VisitRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private VideoMapper videoMapper;
    @Mock
    private VisitRecordMapper visitRecordMapper;
    @Mock
    private UserMapper userMapper;

    private FeedService feedService;

    @BeforeEach
    void setUp() {
        feedService = new FeedService(videoMapper, visitRecordMapper, userMapper);
    }

    @Test
    @DisplayName("getNextVideo → 排除已访问、按点赞排序并写入 visit_records")
    void getNextVideoSuccess() {
        VisitRecord v1 = visitRecord(1L, 1L);
        VisitRecord v2 = visitRecord(1L, 2L);
        when(visitRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(v1, v2));

        Video candidate = video(3L, 1L, "美食探店", 72);
        when(videoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);

        User publisher = new User();
        publisher.setId(1L);
        publisher.setUsername("alice");
        when(userMapper.selectById(1L)).thenReturn(publisher);

        FeedVideoDTO dto = feedService.getNextVideo(1L);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getTitle()).isEqualTo("美食探店");
        assertThat(dto.getPublisher().getUsername()).isEqualTo("alice");

        ArgumentCaptor<VisitRecord> captor = ArgumentCaptor.forClass(VisitRecord.class);
        verify(visitRecordMapper).insert(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getVideoId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getNextVideo 无候选 → null")
    void getNextVideoEmpty() {
        when(visitRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(videoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThat(feedService.getNextVideo(1L)).isNull();
    }

    @Test
    @DisplayName("getPreviousVideo → 返回历史中更早的正常视频")
    void getPreviousVideoSuccess() {
        VisitRecord recent = visitRecord(1L, 2L);
        VisitRecord older = visitRecord(1L, 1L);
        when(visitRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(recent, older));

        Video video = video(1L, 1L, "搞笑猫咪", 128);
        when(videoMapper.selectById(1L)).thenReturn(video);
        when(userMapper.selectById(1L)).thenReturn(user(1L, "alice"));

        FeedVideoDTO dto = feedService.getPreviousVideo(1L, 2L);

        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("writeVisitRecord 幂等 → DuplicateKeyException 被忽略")
    void visitRecordIdempotent() {
        when(visitRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(videoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(video(3L, 1L, "t", 1));
        when(userMapper.selectById(any())).thenReturn(user(1L, "alice"));
        doThrow(new DuplicateKeyException("dup")).when(visitRecordMapper).insert(any(VisitRecord.class));

        assertThat(feedService.getNextVideo(1L)).isNotNull();
    }

    @Test
    @DisplayName("isInVisitRecords → 按 user/video 计数")
    void isInVisitRecords() {
        when(visitRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThat(feedService.isInVisitRecords(1L, 2L)).isTrue();
    }

    private static VisitRecord visitRecord(Long userId, Long videoId) {
        VisitRecord record = new VisitRecord();
        record.setUserId(userId);
        record.setVideoId(videoId);
        record.setViewedAt(LocalDateTime.now());
        return record;
    }

    private static Video video(Long id, Long userId, String title, int likeCount) {
        Video video = new Video();
        video.setId(id);
        video.setUserId(userId);
        video.setTitle(title);
        video.setVideoUrl("https://example.com/" + id + ".mp4");
        video.setLikeCount(likeCount);
        video.setStatus(VideoConstants.STATUS_NORMAL);
        video.setCreatedAt(LocalDateTime.now());
        return video;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
