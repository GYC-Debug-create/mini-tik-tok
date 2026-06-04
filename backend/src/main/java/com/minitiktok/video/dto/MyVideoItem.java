package com.minitiktok.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyVideoItem {

    private Long id;
    private String title;
    private String videoUrl;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
