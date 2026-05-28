# 成员 C — 视频管理

> A 已提供：`Video`/`User` 实体与 Mapper、`Result`、`PageResult`、`CurrentUser`、`VideoConstants`（`status` 0/1）、全局异常、日志 Filter。  
> B 完成 JWT 后，受保护接口需带 `Authorization: Bearer <token>`。

## 你要实现的接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/videos` | 发布视频；body: `title`, `videoUrl`；成功 **201** |
| GET | `/api/users/me/videos?page=1&pageSize=10` | 当前用户视频分页；`data` 为 `PageResult` |
| DELETE | `/api/videos/{videoId}` | 软删除；非本人 → **403**；不存在或已删 → **404** |

## 约束

1. 用户 ID：`CurrentUser.requireUserId(request)`  
2. 所有查询/推荐可见视频：`status = VideoConstants.STATUS_NORMAL`（0）  
3. 删除：`UPDATE status = 1`，禁止物理删除  
4. 越权删除：`BusinessException(403, "无权删除该视频")`  
5. 分页：使用 MyBatis-Plus `Page`，组装 `PageResult` 放入 `Result.ok(data)`

## 建议类

- `controller/VideoController.java`（或 `video/VideoController.java`）  
- `service/VideoService.java`  

算法与字段见《技术设计文档》§6.3、§6.4。
