# mini-tiktok-backend

Spring Boot 3 后端模块（Maven 工程根目录即本文件夹）。

## IDEA 导入

- **方式 1**：在 IDEA 打开上一级 `miniTikTok` 根目录（含聚合 `pom.xml`），选中 `backend` 子模块。  
- **方式 2**：直接打开本 `backend` 文件夹。

SDK：**JDK 17**。主类：`com.minitiktok.MiniTikTokApplication`。

## 启动

```bash
mvn spring-boot:run
```

先执行仓库根目录：`mysql -u root -p < ../sql/init.sql`

## 包与分工

| 包/类 | 负责人 |
|-------|--------|
| `entity` `mapper` `common`（含 `PageResult`、`FilterOrders`） | A |
| `filter.RequestLogFilter` `config.FilterConfig` | A |
| `auth.*` `JwtUtil` `JwtAuthFilter` | **B（已完成）** |
| `controller` 视频 | C |
| `controller` Feed/Like | D |

详见 `../docs/MEMBER-A-后端基础交接.md`、`../docs/MEMBER-B-认证与JWT.md`、`../docs/MEMBER-B-使用文档.md`（C/D/E 必读）、`../docs/MEMBER-C-视频管理.md`、`../docs/MEMBER-D-Feed与点赞.md`。

日志验收 SQL：`../sql/monitoring-queries.sql`
