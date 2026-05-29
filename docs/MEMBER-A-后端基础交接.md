# 成员 A — 后端基础设施（不含登录/JWT 业务）

> **包名**：`com.minitiktok`，启动类 `MiniTikTokApplication`。  
> **登录、JWT 生成与验票全部由成员 B 实现**（见 `docs/MEMBER-B-认证与JWT.md`）。

---

## A 负责什么 / 不负责什么

| A 负责（已提交或维护） | A 不负责（队友填代码） |
|------------------------|------------------------|
| Maven 工程、`application.yml` 模板（含数据源与 `jwt.*` 供 B 读取） | `AuthController`、注册登录逻辑 |
| 5 张表 Entity + Mapper | `JwtUtil`、`JwtAuthFilter`（**归 B**） |
| `Result`、`GlobalExceptionHandler`、`BusinessException` | `VideoController` / `FeedController`（C/D） |
| `RequestLogFilter` + `RequestLogService` | 前端（E） |
| `CorsConfig`、`MybatisPlusConfig` | |
| `ApiConstants`（公开路径白名单，B 只读） | |
| `CurrentUser`、`VideoConstants`、`PageResult` | |
| `FilterOrders`、`sql/monitoring-queries.sql`、Postman 集合骨架 | |
| `GET /api/health` 冒烟接口 | |
| （B 已交付）`auth` + JWT Filter | 见 `MEMBER-B-使用文档.md`，A 只验收日志与 Filter 顺序 |
| 根目录聚合 `pom.xml`（方便 IDEA 识别模块） | |

---

## IDEA 打不开依赖？（你问的第 1 点）

**原因**：真正的 Spring Boot 工程在 **`backend/`**，根目录以前是文档+SQL，没有 Maven 工程。

**做法（二选一）**：

1. **打开仓库根目录 `miniTikTok`** → 应识别根 `pom.xml`（packaging=pom）+ 子模块 `backend` → Maven 面板 Reload。  
2. **只打开 `backend` 文件夹** → 直接当单体 Maven 项目。

然后：Project SDK = **JDK 17**，运行 `MiniTikTokApplication`。

详见根目录 `README.md`。

---

## 本地启动

```bash
# 在仓库根目录
mysql -u root -p < sql/init.sql

cd backend
# 改 application.yml 里数据库密码，或复制 application-example.yml → application-local.yml
mvn spring-boot:run
```

- `GET http://localhost:8080/api/health` → 200  
- 此时**尚无** JwtAuthFilter，除 health 外接口不会自动 401（B 接上 Filter 后才有）

---

## 给队友的约束（A 定规矩）

1. **包名**统一 `com.minitiktok`，禁止再建 `douyinlite` 等第二套。  
2. **业务按人分包**：B→`auth`+JWT Filter；C→视频；D→Feed/Like。  
3. **不要改** `RequestLogFilter` 的 Order（`FilterOrders.REQUEST_LOG`）。B 使用 `FilterOrders.JWT_AUTH`。  
4. 受保护接口取用户：`CurrentUser.requireUserId(request)`。  
5. 抛错用 `BusinessException(403, "…")` 等，走统一 JSON。  
6. 公开路径只改 `ApiConstants.isPublicPath()`，改前在群里说一声。

---

## A 本周 checklist

- [x] 骨架、Entity、Mapper、日志 Filter、CORS  
- [x] 基础设施增强（`bf6705b`）  
- [x] 成员 B 已合并认证模块（`1434b21`），见 [`MEMBER-B-使用文档.md`](MEMBER-B-使用文档.md)  
- [ ] **日志联调**（与 B，约 15 分钟，见下节）  
- [ ] 通知 C/D 拉 `master`，按 Postman 先 login 再开发  
- [ ] 第 2 周：C/D 联调时你只 code review，少改别人 Controller  

---

## 日志联调（B 已上线后，A 主导验收）

1. 启动：`cd backend && mvn spring-boot:run`（先执行 `sql/init.sql`）。  
2. Postman：`POST /api/auth/login`（alice / 123456）→ 确认集合变量 `token` 有值。  
3. 无 Token：`GET /api/feed/videos/next` → **401**，且 `request_logs` 新增一行（`user_id` 为 NULL）。  
4. 带 Token：同上请求 → 当前为 **404**（D 未实现属正常）；`user_id` 应为登录用户 ID。  
5. 查表：`sql/monitoring-queries.sql` 或  
   `SELECT response_body, request_params FROM request_logs ORDER BY id DESC LIMIT 3;`  
   → `password`、`token` 字段值应为 `***`，不得出现明文。  
6. 通过后，在 [`实现状态与待办清单.md`](实现状态与待办清单.md) §4 勾选「日志联调」。

---

## 仓库结构（不要改成「根目录=纯后端」）

```
miniTikTok/           ← 作业整包（文档+sql+backend+frontend）
├── pom.xml           ← 聚合模块，给 IDEA 用
├── backend/          ← 你只在这下面写基础设施
├── frontend/         ← E 后续
├── sql/
└── docs/
```

前后端分离 = 两个进程（8080 / 5173），**不是**去掉 `backend` 这一层文件夹。
