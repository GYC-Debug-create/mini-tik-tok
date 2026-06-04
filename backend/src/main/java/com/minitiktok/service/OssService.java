package com.minitiktok.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.minitiktok.common.BusinessException;
import com.minitiktok.config.OssProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class OssService {

    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp4", "webm", "mov", "avi", "mkv");

    private final OssProperties ossProperties;
    private final OSS ossClient;

    public OssService(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
        validateConfig();
        this.ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
    }

    public String upload(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "视频文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "视频文件大小不能超过100MB");
        }

        String extension = resolveExtension(file);
        String objectKey = "videos/" + userId + "/" + UUID.randomUUID() + "." + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (StringUtils.hasText(file.getContentType())) {
            metadata.setContentType(file.getContentType());
        }

        try {
            ossClient.putObject(ossProperties.getBucketName(), objectKey, file.getInputStream(), metadata);
        } catch (IOException ex) {
            log.error("OSS 上传失败, objectKey={}", objectKey, ex);
            throw new BusinessException(500, "视频上传失败");
        }

        return buildPublicUrl(objectKey);
    }

    String buildPublicUrl(String objectKey) {
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectKey;
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (ALLOWED_EXTENSIONS.contains(ext)) {
                return ext;
            }
        }
        throw new BusinessException(400, "不支持的视频格式，仅支持: mp4, webm, mov, avi, mkv");
    }

    private void validateConfig() {
        if (!StringUtils.hasText(ossProperties.getEndpoint())
                || !StringUtils.hasText(ossProperties.getAccessKeyId())
                || !StringUtils.hasText(ossProperties.getAccessKeySecret())
                || !StringUtils.hasText(ossProperties.getBucketName())) {
            throw new IllegalStateException("OSS 配置不完整，请检查 alioss.* 配置项");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
