package com.minitiktok.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minitiktok.common.VideoConstants;
import com.minitiktok.dto.FeedVideoDTO;
import com.minitiktok.dto.PublisherDTO;
import com.minitiktok.entity.User;
import com.minitiktok.entity.Video;
import com.minitiktok.entity.VisitRecord;
import com.minitiktok.mapper.UserMapper;
import com.minitiktok.mapper.VideoMapper;
import com.minitiktok.mapper.VisitRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final VideoMapper videoMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final UserMapper userMapper;

    public FeedVideoDTO getNextVideo(Long userId) {
        List<Long> visitedIds = getVisitedVideoIds(userId);

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getStatus, VideoConstants.STATUS_NORMAL);
        if (!visitedIds.isEmpty()) {
            wrapper.notIn(Video::getId, visitedIds);
        }
        wrapper.orderByDesc(Video::getLikeCount)
               .orderByDesc(Video::getCreatedAt)
               .last("LIMIT 1");

        Video video = videoMapper.selectOne(wrapper);
        if (video == null) {
            return null;
        }

        writeVisitRecord(userId, video.getId());

        return buildFeedVideoDTO(video);
    }

    public FeedVideoDTO getPreviousVideo(Long userId, Long currentVideoId) {
        List<VisitRecord> records = getVisitRecordsDesc(userId);
        int index = findRecordIndex(records, currentVideoId);
        if (index < 0) {
            return null;
        }

        for (int i = index + 1; i < records.size(); i++) {
            Video video = videoMapper.selectById(records.get(i).getVideoId());
            if (video != null && video.getStatus() != null && video.getStatus() == VideoConstants.STATUS_NORMAL) {
                return buildFeedVideoDTO(video);
            }
        }
        return null;
    }

    public FeedVideoDTO getForwardVideo(Long userId, Long currentVideoId) {
        List<VisitRecord> records = getVisitRecordsDesc(userId);
        int index = findRecordIndex(records, currentVideoId);
        if (index < 0) {
            return null;
        }

        for (int i = index - 1; i >= 0; i--) {
            Video video = videoMapper.selectById(records.get(i).getVideoId());
            if (video != null && video.getStatus() != null && video.getStatus() == VideoConstants.STATUS_NORMAL) {
                return buildFeedVideoDTO(video);
            }
        }
        return null;
    }

    private List<Long> getVisitedVideoIds(Long userId) {
        LambdaQueryWrapper<VisitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VisitRecord::getUserId, userId)
               .select(VisitRecord::getVideoId);
        List<VisitRecord> records = visitRecordMapper.selectList(wrapper);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .map(VisitRecord::getVideoId)
                .collect(Collectors.toList());
    }

    private List<VisitRecord> getVisitRecordsDesc(Long userId) {
        LambdaQueryWrapper<VisitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VisitRecord::getUserId, userId)
               .orderByDesc(VisitRecord::getViewedAt);
        return visitRecordMapper.selectList(wrapper);
    }

    private int findRecordIndex(List<VisitRecord> records, Long currentVideoId) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getVideoId().equals(currentVideoId)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isInVisitRecords(Long userId, Long videoId) {
        LambdaQueryWrapper<VisitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VisitRecord::getUserId, userId)
               .eq(VisitRecord::getVideoId, videoId);
        return visitRecordMapper.selectCount(wrapper) > 0;
    }

    private void writeVisitRecord(Long userId, Long videoId) {
        VisitRecord record = new VisitRecord();
        record.setUserId(userId);
        record.setVideoId(videoId);
        try {
            visitRecordMapper.insert(record);
        } catch (DuplicateKeyException ignored) {
        }
    }

    private FeedVideoDTO buildFeedVideoDTO(Video video) {
        FeedVideoDTO dto = new FeedVideoDTO();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setVideoUrl(video.getVideoUrl());
        dto.setLikeCount(video.getLikeCount());
        dto.setCreatedAt(video.getCreatedAt());

        User publisher = userMapper.selectById(video.getUserId());
        if (publisher != null) {
            dto.setPublisher(new PublisherDTO(publisher.getId(), publisher.getUsername()));
        }
        return dto;
    }
}
