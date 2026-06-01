package com.minitiktok.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minitiktok.common.BusinessException;
import com.minitiktok.common.VideoConstants;
import com.minitiktok.entity.LikeRecord;
import com.minitiktok.entity.Video;
import com.minitiktok.mapper.LikeRecordMapper;
import com.minitiktok.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final VideoMapper videoMapper;

    @Transactional
    public void like(Long userId, Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null || video.getStatus() == null || video.getStatus() != VideoConstants.STATUS_NORMAL) {
            throw new BusinessException(404, "视频不存在或已删除");
        }

        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
               .eq(LikeRecord::getVideoId, videoId);
        Long count = likeRecordMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }

        LikeRecord record = new LikeRecord();
        record.setUserId(userId);
        record.setVideoId(videoId);
        likeRecordMapper.insert(record);

        video.setLikeCount(video.getLikeCount() + 1);
        videoMapper.updateById(video);
    }
}
