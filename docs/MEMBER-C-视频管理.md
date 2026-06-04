# 成员 C — 视频管理（含阿里云 OSS 上传）

> A 已提供：`Video`/`User` 实体与 Mapper、`Result`、`PageResult`、`CurrentUser`、`VideoConstants`、全局异常、日志 Filter。  
> B 已完成 JWT；受保护接口需 `Authorization: Bearer <token>`。  
> 发布方案见《技术设计文档》§5.3.7、§9。

## 你要实现的接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/videos` | **multipart**：`title` + `file`（mp4）→ 上传 OSS → 写 `video_url` → **201** |
| GET | `/api/users/me/videos?page=1&pageSize=10` | 当前用户视频分页；`data` 为 `PageResult` |
| DELETE | `/api/videos/{videoId}` | 软删除；非本人 → **403** |

## 建议新增（由 C 实现）

| 类 | 职责 |
|----|------|
| `service/OssService.java` 或 `util/OssUtil.java` | 封装 `putObject`、生成访问 URL |
| `config/OssProperties.java` | 绑定 `aliyun.oss.*` |
| `controller/VideoController.java` | 三个 HTTP 接口 |
| `service/VideoService.java` | 发布/分页/删除业务 |

`pom.xml` 增加：`com.aliyun.oss:aliyun-sdk-oss`（版本见技术设计或阿里云文档）。

## 约束

1. 用户 ID：`CurrentUser.requireUserId(request)`  
2. 查询带 `status = VideoConstants.STATUS_NORMAL`（0）  
3. OSS 密钥只放 `application-local.yml`，**勿提交 Git**  
4. Bucket 配置 **CORS**，允许浏览器 `GET` 视频（前端 `<video>` 播放）  
5. 日志中勿打印完整 OSS 密钥；大文件不进 `request_logs` 正文  

## 与 E 联调

- 发布页：`FormData` 提交 `title` + `file`  
- 取消「手填 URL」为主流程（可选保留高级入口，非必须）
