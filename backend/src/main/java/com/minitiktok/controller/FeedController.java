package com.minitiktok.controller;

import com.minitiktok.common.CurrentUser;
import com.minitiktok.common.Result;
import com.minitiktok.dto.FeedVideoDTO;
import com.minitiktok.service.FeedService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/api/feed/videos/next")
    public Result<FeedVideoDTO> next(HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        FeedVideoDTO video = feedService.getNextVideo(userId);
        if (video == null) {
            return Result.ok("暂无更多推荐视频", null);
        }
        return Result.ok(video);
    }

    @GetMapping("/api/feed/videos/previous")
    public Result<FeedVideoDTO> previous(@RequestParam Long currentVideoId,
                                         HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        if (!feedService.isInVisitRecords(userId, currentVideoId)) {
            return Result.ok("未找到该浏览记录", null);
        }
        FeedVideoDTO video = feedService.getPreviousVideo(userId, currentVideoId);
        if (video == null) {
            return Result.ok("已是第一个视频", null);
        }
        return Result.ok(video);
    }

    @GetMapping("/api/feed/videos/forward")
    public Result<FeedVideoDTO> forward(@RequestParam Long currentVideoId,
                                        HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        if (!feedService.isInVisitRecords(userId, currentVideoId)) {
            return Result.ok("未找到该浏览记录", null);
        }
        FeedVideoDTO video = feedService.getForwardVideo(userId, currentVideoId);
        if (video == null) {
            return Result.ok("已是最近浏览的视频", null);
        }
        return Result.ok(video);
    }
}
