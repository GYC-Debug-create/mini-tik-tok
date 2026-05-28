package com.minitiktok.service;

import com.minitiktok.entity.RequestLog;
import com.minitiktok.mapper.RequestLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogMapper requestLogMapper;

    public void save(Long userId, String method, String path, String requestParams,
                     int responseCode, String responseBody, int costMs) {
        RequestLog log = new RequestLog();
        log.setUserId(userId);
        log.setMethod(method);
        log.setPath(path);
        log.setRequestParams(requestParams);
        log.setResponseCode(responseCode);
        log.setResponseBody(responseBody);
        log.setCostMs(costMs);
        log.setCreatedAt(LocalDateTime.now());
        requestLogMapper.insert(log);
    }
}
