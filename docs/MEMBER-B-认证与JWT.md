# 成员 B — 认证与 JWT（完整实现）

> A 已提供：Entity/Mapper、Result、异常处理、RequestLogFilter、CORS、公开路径常量 {@link com.minitiktok.common.ApiConstants}。  
> **登录注册与 JWT 验票全部由 B 在本包 `com.minitiktok.auth` 及 `filter` 中实现。**

## 你要新建的文件（建议）

| 文件 | 职责 |
|------|------|
| `auth/AuthController.java` | POST register、login、logout |
| `auth/UserService.java` | 注册、校验密码、BCrypt |
| `auth/dto/RegisterRequest.java` 等 | 入参 DTO |
| `util/JwtUtil.java` | 生成/解析 JWT（读 `application.yml` 的 `jwt.*`） |
| `config/JwtProperties.java` | 绑定 jwt.secret、expiration-days |
| `filter/JwtAuthFilter.java` | 校验 Bearer Token，注入 `ApiConstants.ATTR_USER_ID` |
| `config/AuthFilterConfig.java` | 注册 JwtAuthFilter，**Order = `FilterOrders.JWT_AUTH`**（见 A 提供的 `config.FilterOrders`） |

## 与 A 的边界

| 事项 | 归属 |
|------|------|
| `users` 表、`User` 实体、`UserMapper` | A 已建，B **只调用** Mapper |
| `ApiConstants.isPublicPath()` | A 已建，B **只读**；新增匿名接口找 A 加白名单 |
| `CurrentUser.requireUserId(request)` | A 已建，依赖你在 Filter 里设置的 `currentUserId` |
| `RequestLogFilter` | A **主导**实现；B 联调时确认 `request_logs` 不含明文密码、完整 Token |
| `Result` / `BusinessException` | A 已实现，Controller 直接用 |
| 登录成功响应 | B 负责：`data.token` + `userId` + `username`（见下文示例） |

## JwtAuthFilter 要点

1. 公开路径（与 A 一致）：`/api/auth/register`、`/api/auth/login`、`/api/auth/logout`、`/api/health`
2. 其余 `/api/**` 必须 `Authorization: Bearer <token>`
3. 失败返回 401 + `Result{code:401, message:"未登录或Token无效/过期"}`
4. 成功：`request.setAttribute(ApiConstants.ATTR_USER_ID, userId)`

## 登录成功响应示例

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJ...",
    "userId": 1,
    "username": "alice"
  }
}
```

## 注册

- 路径：`POST /api/auth/register`
- 密码 BCrypt 加密存库（建议在 `backend/pom.xml` 增加 `spring-security-crypto`，仅用 `BCryptPasswordEncoder`，不必引入完整 Spring Security）
- 用户名重复 → 409

## 联调自检

1. 注册 → 登录拿 token
2. 无 token 访问 `GET /api/feed/videos/next` → 401（需 D 实现后测，或先测任意受保护占位）
3. 查 `request_logs`：`response_body` 中 token 为 `***`
4. 种子用户：`alice` / `123456`（`sql/init.sql`）

## Filter 顺序（全组遵守）

```
RequestLogFilter（A，Order = FilterOrders.REQUEST_LOG）
  → JwtAuthFilter（B，Order = FilterOrders.JWT_AUTH）
    → Controller（B/C/D）
```
