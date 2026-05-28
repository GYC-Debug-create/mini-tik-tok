# 成员 A — 后端基础设施（不含登录/JWT 业务）

> **包名**：`com.minitiktok`，启动类 `MiniTikTokApplication`。  
> **登录、JWT 生成与验票全部由成员 B 实现**（见 `docs/MEMBER-B-认证与JWT.md`）。

---

## A 负责什么 / 不负责什么

| A 负责（已提交或维护） | A 不负责（队友填代码） |
|------------------------|------------------------|
| Maven 工程、`application.yml` 模板 | `AuthController`、注册登录逻辑 |
| 5 张表 Entity + Mapper | `JwtUtil`、`JwtAuthFilter`（**归 B**） |
| `Result`、`GlobalExceptionHandler`、`BusinessException` | `VideoController` / `FeedController`（C/D） |
| `RequestLogFilter` + `RequestLogService` | 前端（E） |
| `CorsConfig`、`MybatisPlusConfig` | |
| `ApiConstants`（公开路径白名单，B 只读） | |
| `CurrentUser`、`VideoConstants` | |
| `GET /api/health` 冒烟接口 | |
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
3. **不要改** `RequestLogFilter` 的 Order（最外层）。B 的 JwtAuthFilter 必须在其后。  
4. 受保护接口取用户：`CurrentUser.requireUserId(request)`。  
5. 抛错用 `BusinessException(403, "…")` 等，走统一 JSON。  
6. 公开路径只改 `ApiConstants.isPublicPath()`，改前在群里说一声。

---

## A 本周 checklist

- [x] 骨架、Entity、Mapper、日志 Filter、CORS  
- [ ] 完成首次 `git commit`（含 `.gitignore` 与文档最新版，见根 `README.md`「Git 协作」）  
- [ ] 推 Gitee，通知 B 拉分支开始 `auth` 包  
- [ ] 全组对照 [`实现状态与待办清单.md`](实现状态与待办清单.md) 更新各接口 ⬜/✅  
- [ ] B 提交 JWT 后，联调 `request_logs` 脱敏  
- [ ] 第 2 周：C/D 联调时你只 code review，少改别人 Controller  

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
