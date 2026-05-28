package com.minitiktok.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("request_logs")
public class RequestLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String method;
    private String path;
    private String requestParams;
    private Integer responseCode;
    private String responseBody;
    private Integer costMs;
    private LocalDateTime createdAt;
}
