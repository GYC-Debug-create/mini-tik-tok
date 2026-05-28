# miniTikTok（简易版抖音）

课程作业整包仓库：**文档 + SQL + 后端 +（后续）前端**。

---

## 当前进度（一眼看懂）

| 模块 | 负责人 | 状态 |
|------|--------|------|
| 需求/技术设计 v2.1 | 全组 | ✅ 已入仓（工作区可能有未提交微调） |
| `sql/init.sql` | A | ✅ |
| 后端基础设施（Entity/Mapper/日志/CORS/Result） | A | ✅ |
| `GET /api/health` | A | ✅ |
| 认证 + JWT（3 接口 + Filter） | B | ⬜ 待实现 |
| 视频 3 接口 | C | ⬜ 待实现 |
| Feed + 点赞 4 接口 | D | ⬜ 待实现 |
| `frontend/` | E | ⬜ 待建 |

**完整对照表**：[`docs/实现状态与待办清单.md`](docs/实现状态与待办清单.md)

**成员交接文档**：

| 成员 | 文档 |
|------|------|
| A | [`docs/MEMBER-A-后端基础交接.md`](docs/MEMBER-A-后端基础交接.md) |
| B | [`docs/MEMBER-B-认证与JWT.md`](docs/MEMBER-B-认证与JWT.md) |
| 全组 | [`docs/团队分工与提交清单.md`](docs/团队分工与提交清单.md) |

分工基线：`需求规格说明书_v2.1_P0-only开发冻结版.md` §15、`技术设计文档_v2.1_P0-only开发冻结版.md` §13。

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

```bash
mysql -u root -p < sql/init.sql
cd backend
# 本地密码勿提交：复制 application-example.yml → application-local.yml 并修改
mvn spring-boot:run
```

- 健康检查：`GET http://localhost:8080/api/health`
- 种子账号（联调 B 登录后）：`alice` / `123456`

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

## Git 协作（当前仓库状态）

- 仓库已 `git init`，**尚未有任何 commit**。
- 大部分文件已在暂存区（`git add` 过）；**还需补 add**：
  - `.gitignore`（忽略 `target/`、`out/`、`.idea/` 等）
  - 工作区已修改的 3 个文件：两份 v2.1 规格/设计文档、`docs/团队分工与提交清单.md`
- **不要提交**：`out/`（IDEA 编译输出）、`backend/target/`、`.idea/`、`application-local.yml`

建议 A 首次提交（在仓库根目录执行）：

```bash
git add .gitignore
git add "docs/团队分工与提交清单.md"
git add "技术设计文档_v2.1_P0-only开发冻结版.md"
git add "需求规格说明书_v2.1_P0-only开发冻结版.md"
git add docs/实现状态与待办清单.md
git status   # 确认无 out/、target/
git commit -m "feat(infra): A 后端骨架、SQL、文档 v2.1 与 B/C/D/E 待办说明"
```

推远程（配置 Gitee 后）：

```bash
git remote add origin <你的仓库 URL>
git push -u origin master
```

队友建议从 `dev` 或 `feat/xxx` 分支开发，合并前 Pull Request / 联调。

---

## 目录说明

| 路径 | 谁负责 | 说明 |
|------|--------|------|
| `需求规格说明书_v2.1_*.md` | 全组 | 需求基线 |
| `技术设计文档_v2.1_*.md` | 全组 | 技术基线 |
| `sql/init.sql` | A | 建库脚本 |
| `backend/` | A 骨架；B/C/D 填业务 | Spring Boot |
| `frontend/` | E（待建） | Vue3 |
| `docs/MEMBER-*.md` | 各成员 | 交接说明 |
| `docs/实现状态与待办清单.md` | 全组 | 接口与文件待办矩阵 |

---

## 提交前 README 还需补全（期末）

环境 JDK 17、MySQL 8、Node 18；前后端启动命令；演示账号；2 分钟录屏要点见 `docs/团队分工与提交清单.md` §4。
