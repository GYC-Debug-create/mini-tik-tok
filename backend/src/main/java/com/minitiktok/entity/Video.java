package com.minitiktok.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("videos")
public class Video {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String videoUrl;
    private Integer likeCount;
    /** 0=正常，1=已删除 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
