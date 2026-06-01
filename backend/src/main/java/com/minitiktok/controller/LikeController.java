package com.minitiktok.controller;

import com.minitiktok.common.CurrentUser;
import com.minitiktok.common.Result;
import com.minitiktok.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/api/videos/{videoId}/like")
    public Result<Void> like(@PathVariable Long videoId, HttpServletRequest request) {
        Long userId = CurrentUser.requireUserId(request);
        likeService.like(userId, videoId);
        return Result.ok(null);
    }
}
