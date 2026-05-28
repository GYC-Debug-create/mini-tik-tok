# 成员 D — 推荐流与点赞

> A 已提供：`Video`、`VisitRecord`、`LikeRecord` 实体与 Mapper、`VideoConstants`、`CurrentUser`、`Result`。  
> 种子数据见 `sql/init.sql`（含 `visit_records` 便于测 previous/forward）。

## 你要实现的接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/feed/videos/next` | 新推荐：未访问 + `like_count` DESC；写入 `visit_records` |
| GET | `/api/feed/videos/previous?currentVideoId=` | 访问记录中更早一条 |
| GET | `/api/feed/videos/forward?currentVideoId=` | 访问记录中更新一条；已在最近 → `data: null` |
| POST | `/api/videos/{videoId}/like` | 点赞幂等；事务更新 `likes` + `videos.like_count` |

## 约束

1. `currentVideoId` 缺失或非法 → **400**  
2. Feed 返回 DTO 含 `publisher.id`、`publisher.username`（技术设计 §5.1 Feed DTO）  
3. `visit_records`：`INSERT IGNORE` 或等价幂等  
4. 点赞重复：`like_count` 不重复增加（`INSERT IGNORE` + 判断影响行数）  
5. 所有视频查询带 `status = 0`

## 建议类

- `controller/FeedController.java`  
- `controller/LikeController.java`（或与 Feed 分 Service）  
- `service/FeedService.java`、`service/LikeService.java`  

算法见《技术设计文档》§6.1～§6.2、§6.5。
