package com.minitiktok.video.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PublishVideoRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    @NotBlank(message = "视频URL不能为空")
    @Size(max = 500, message = "视频URL长度不能超过500")
    private String videoUrl;
}
