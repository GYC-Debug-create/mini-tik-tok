# miniTikTok（简易版抖音）

课程作业整包仓库：**文档 + SQL + 后端 +（后续）前端**。

---

## 当前进度

| 模块 | 负责人 | 状态 |
|------|--------|------|
| 需求/技术设计 v3.0 | 全组 | ✅（视频存阿里云 OSS，见 §8.2 / 技术 §9） |
| `sql/init.sql` + `monitoring-queries.sql` | A | ✅ |
| 后端基础设施（Entity/Mapper/日志/CORS/Result/PageResult） | A | ✅ |
| `GET /api/health`（含 DB 状态） | A | ✅ |
| 认证 + JWT（3 接口 + Filter） | B | ✅ |
| 视频 3 接口 + OSS 上传 | C | ✅ |
| Feed + 点赞 4 接口 | D | ✅ |
| `frontend/` | E | ✅ 骨架；部分页面仍 mock，见 `FRONTEND_PROGRESS.md` |

**完整对照表**：[`docs/实现状态与待办清单.md`](docs/实现状态与待办清单.md)

**成员交接文档**：

| 成员 | 文档 |
|------|------|
| A | [`docs/MEMBER-A-后端基础交接.md`](docs/MEMBER-A-后端基础交接.md) |
| B | [`docs/MEMBER-B-认证与JWT.md`](docs/MEMBER-B-认证与JWT.md) · [`docs/MEMBER-B-使用文档.md`](docs/MEMBER-B-使用文档.md)（C/D/E） |
| C | [`docs/MEMBER-C-视频管理.md`](docs/MEMBER-C-视频管理.md) |
| D | [`docs/MEMBER-D-Feed与点赞.md`](docs/MEMBER-D-Feed与点赞.md) |
| 全组 | [`docs/团队分工与提交清单.md`](docs/团队分工与提交清单.md) · Postman：[`docs/postman/`](docs/postman/) |

分工基线：`需求规格说明书_v2.1_P0-only开发冻结版.md`（文内 v3.0）、`技术设计文档_v2.1_P0-only开发冻结版.md`（文内 v3.0）§13。  
视频发布：用户上传 mp4 → 后端上传 **阿里云 OSS** → 数据库存 `video_url`（配置见 `application-example.yml`）。

---

## IDEA / Maven 怎么打开？（重要）

`pom.xml` 在 **`backend/`** 子目录，不要只把根目录当普通 Java 项目。

| 方式 | 操作 |
|------|------|
| **推荐** | IDEA → 打开 **`miniTikTok` 根目录** → 识别根 `pom.xml`（聚合模块）→ 自动加载 `backend` 子模块 |
| **备选** | IDEA → 直接打开 **`miniTikTok/backend`** 文件夹 |

打开后：Maven 面板 → **Reload** → 运行 `com.minitiktok.MiniTikTokApplication`。

若仍「包找不到」：Project SDK = **JDK 17**，Language level = 17。

---

## 快速启动（后端）

**方式 A — Docker MySQL（推荐）**

```bash
docker compose up -d
cd backend
# 复制 application-example.yml → application-local.yml，填写数据库密码与 OSS 密钥（上传接口需要）
mvn spring-boot:run
```

**方式 B — 本机 MySQL**

```bash
mysql -u root -p < sql/init.sql
cd backend
# 同上：application-local.yml
mvn spring-boot:run
```

- 健康检查：`GET http://localhost:8080/api/health`（含 `database: up/down`）
- 日志验收 SQL：见 `sql/monitoring-queries.sql`
- Postman：导入 `docs/postman/miniTikTok-P0.postman_collection.json`
- 种子账号：`alice` / `123456`

### 测试

```bash
cd backend
mvn test
```

| 类型 | 说明 |
|------|------|
| 单元测试 | MockMvc standalone / Mockito，**无需** MySQL |
| 集成测试 | `ApiIntegrationTest`（Testcontainers MySQL），**需要 Docker**；无 Docker 时自动跳过 |
| API 联调 | Postman 集合，需先 `mvn spring-boot:run` + 数据库 |

OSS：仅 `POST /api/videos/upload` 需要有效 `alioss.*`；用 JSON 发布可只填 `videoUrl`（`POST /api/videos`）。

---

## 五人分工（简表）

| 成员 | 目录/包 | 内容 |
|------|---------|------|
| A | `entity` `mapper` `common` `filter.RequestLog*` `config` | 骨架，**不含**登录与 JWT 实现 |
| B | `auth` + `JwtUtil` + `JwtAuthFilter` | 注册/登录/注销 + 统一鉴权 |
| C | 视频 Controller/Service | 发布、我的列表、删除（403） |
| D | Feed/Like | next / previous / forward、点赞 |
| E | `frontend/` | 四个页面 + Axios |

**A 与 B 边界**：A 提供 `ApiConstants` 白名单、`CurrentUser`、用户表 Mapper；B 实现 `JwtAuthFilter` 并在 Filter 中写入 `currentUserId`。详见 MEMBER-A / MEMBER-B 文档。

---

## Git 协作

- **不要提交**：`out/`、`backend/target/`、`.idea/`、`application-local.yml`。
- 队友从 `feat/auth-b`、`feat/video-c` 等分支开发，合并前联调。
- 历史提交：`849c36f`（骨架）→ `f5c0fc9`（分工文档）→ `bf6705b`（A 增强）→ `1434b21`（B 认证/JWT）→ `5026c6a`（B README）。

---

## 目录说明

| 路径 | 谁负责 | 说明 |
|------|--------|------|
| `需求规格说明书_v2.1_*.md` | 全组 | 需求基线 |
| `技术设计文档_v2.1_*.md` | 全组 | 技术基线 |
| `sql/init.sql` | A | 建库脚本 |
| `sql/monitoring-queries.sql` | A | 答辩用 `request_logs` 查询示例 |
| `backend/` | A 骨架；B/C/D 填业务 | Spring Boot |
| `frontend/` | E | Vue3 + Vite |
| `docker-compose.yml` | 全组 | 本地 MySQL 8 |
| `docs/MEMBER-*.md` | 各成员 | 交接说明 |
| `docs/实现状态与待办清单.md` | 全组 | 接口与文件待办矩阵 |

---

## 提交前 README 还需补全（期末）

环境 JDK 17、MySQL 8、Node 18；前后端启动命令；演示账号；2 分钟录屏要点见 `docs/团队分工与提交清单.md` §4。
