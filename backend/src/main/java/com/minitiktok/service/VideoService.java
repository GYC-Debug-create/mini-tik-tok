package com.minitiktok.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minitiktok.common.BusinessException;
import com.minitiktok.common.PageResult;
import com.minitiktok.common.VideoConstants;
import com.minitiktok.entity.Video;
import com.minitiktok.mapper.VideoMapper;
import com.minitiktok.video.dto.MyVideoItem;
import com.minitiktok.video.dto.PublishVideoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;

    public Map<String, Object> publish(Long userId, PublishVideoRequest request) {
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(request.getTitle());
        video.setVideoUrl(request.getVideoUrl());
        video.setLikeCount(0);
        video.setStatus(VideoConstants.STATUS_NORMAL);
        video.setCreatedAt(LocalDateTime.now());
        video.setUpdatedAt(LocalDateTime.now());

        videoMapper.insert(video);

        Map<String, Object> data = new HashMap<>();
        data.put("id", video.getId());
        data.put("title", video.getTitle());
        data.put("videoUrl", video.getVideoUrl());
        data.put("likeCount", video.getLikeCount());
        data.put("createdAt", video.getCreatedAt());
        return data;
    }

    public PageResult<MyVideoItem> listMyVideos(Long userId, int page, int pageSize) {
        int normalizedPage = page <= 0 ? 1 : page;
        int normalizedPageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .eq(Video::getUserId, userId)
                .eq(Video::getStatus, VideoConstants.STATUS_NORMAL)
                .orderByDesc(Video::getCreatedAt);

        Page<Video> resultPage = videoMapper.selectPage(
                new Page<>(normalizedPage, normalizedPageSize),
                wrapper
        );

        List<MyVideoItem> items = resultPage.getRecords().stream()
                .map(this::toMyVideoItem)
                .toList();

        return PageResult.of(normalizedPage, normalizedPageSize, resultPage.getTotal(), items);
    }

    public void deleteVideo(Long userId, Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException(404, "视频不存在或已删除");
        }
        if (!userId.equals(video.getUserId())) {
            throw new BusinessException(403, "无权删除该视频");
        }
        if (video.getStatus() == VideoConstants.STATUS_DELETED) {
            return;
        }

        Video update = new Video();
        update.setId(videoId);
        update.setStatus(VideoConstants.STATUS_DELETED);
        update.setUpdatedAt(LocalDateTime.now());
        videoMapper.updateById(update);
    }

    private MyVideoItem toMyVideoItem(Video video) {
        return new MyVideoItem(
                video.getId(),
                video.getTitle(),
                video.getVideoUrl(),
                video.getLikeCount(),
                video.getCreatedAt()
        );
    }
}
