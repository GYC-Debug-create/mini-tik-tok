package com.minitiktok.controller;

import com.minitiktok.common.CurrentUser;
import com.minitiktok.common.PageResult;
import com.minitiktok.common.Result;
import com.minitiktok.service.OssService;
import com.minitiktok.service.VideoService;
import com.minitiktok.video.dto.MyVideoItem;
import com.minitiktok.video.dto.PublishVideoRequest;
import com.minitiktok.video.dto.UploadVideoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final OssService ossService;

    @PostMapping("/api/videos/upload")
    public Result<UploadVideoResponse> uploadVideo(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        Long userId = CurrentUser.requireUserId(request);
        String videoUrl = ossService.upload(file, userId);
        return Result.ok("上传成功", new UploadVideoResponse(videoUrl));
    }

    @PostMapping("/api/videos")
    public ResponseEntity<Result<Map<String, Object>>> publishVideo(
            HttpServletRequest request,
            @Valid @RequestBody PublishVideoRequest body) {
        Long userId = CurrentUser.requireUserId(request);
        Map<String, Object> data = videoService.publish(userId, body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.created("发布成功", data));
    }

    @GetMapping("/api/users/me/videos")
    public Result<PageResult<MyVideoItem>> listMyVideos(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = CurrentUser.requireUserId(request);
        PageResult<MyVideoItem> data = videoService.listMyVideos(userId, page, pageSize);
        return Result.ok(data);
    }

    @DeleteMapping("/api/videos/{videoId}")
    public Result<Void> deleteVideo(
            HttpServletRequest request,
            @PathVariable Long videoId) {
        Long userId = CurrentUser.requireUserId(request);
        videoService.deleteVideo(userId, videoId);
        return Result.ok("删除成功", null);
    }
}
