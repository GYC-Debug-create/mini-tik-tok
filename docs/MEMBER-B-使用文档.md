# 成员 B — 认证与 JWT 使用指南

> 本文档供 C（视频管理）、D（推荐与点赞）、E（前端）成员查阅，说明如何调用认证接口、获取当前用户身份、以及 JWT 鉴权机制。

---

## 1. 我实现的文件清单

| 文件 | 职责 |
|---|---|
| `auth/AuthController.java` | 注册/登录/注销 三个公开端点 |
| `auth/UserService.java` | BCrypt 加密注册、登录密码校验 |
| `auth/dto/RegisterRequest.java` | 注册请求体 DTO |
| `auth/dto/LoginRequest.java` | 登录请求体 DTO |
| `util/JwtUtil.java` | JWT 生成（登录用）、解析（Filter 用） |
| `config/JwtProperties.java` | 绑定 `jwt.secret` 和 `jwt.expiration-days` |
| `filter/JwtAuthFilter.java` | Bearer Token 校验 Filter |
| `config/AuthFilterConfig.java` | 注册 JwtAuthFilter（Order = JWT_AUTH） |

---

## 2. 如何获取当前登录用户（C/D 必读）

在你们的 Controller 方法中，直接使用 A 已提供的工具类：

```java
import com.minitiktok.common.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class VideoController {

    @PostMapping("/api/videos")
    public Result<?> publish(HttpServletRequest request, @RequestBody ...) {
        // 只需一行，拿到当前登录用户 ID
        Long userId = CurrentUser.requireUserId(request);
        // userId 是 Long 类型，不可能为 null（无 Token 时会被 JwtAuthFilter 拦截）
        // ...
    }
}
```

- `request` 参数直接加在方法参数里，Spring 会自动注入。
- 如果请求没带 Token 或 Token 无效，**根本不会进入 Controller**（JwtAuthFilter 返回 401），所以 `requireUserId` 在 Controller 里一定成功。
- 如果因为某种原因获取不到，`requireUserId` 会抛出 `BusinessException(401, "未登录或Token无效/过期")`，由全局异常处理器处理。

---

## 3. 三个公开接口（前端 E 调用）

### 3.1 注册

```
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",    // 2-50 字符
  "password": "<REDACTED>" // 6-100 字符
}
```

**成功 201：**
```json
{
  "code": 201,
  "message": "注册成功",
  "data": {
    "id": 1001,
    "username": "newuser",
    "createdAt": "2026-05-29T10:00:00"
  }
}
```

**错误：**
| 条件 | 状态码 | message |
|---|---|---|
| 用户名为空 | 400 | 用户名不能为空 |
| 密码为空 | 400 | 密码不能为空 |
| 用户名已存在 | 409 | 用户名已被注册 |

### 3.2 登录

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "<REDACTED>"
}
```

**成功 200：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "<REDACTED>",
    "userId": 1,
    "username": "alice"
  }
}
```

**错误：**
| 条件 | 状态码 | message |
|---|---|---|
| 用户名为空 | 400 | 用户名不能为空 |
| 密码为空 | 400 | 密码不能为空 |
| 用户名或密码错误 | 401 | 用户名或密码错误 |

> Token 用 `Bearer <token>` 格式放入后续请求的 Authorization 头。

### 3.3 注销

```
POST /api/auth/logout
```

**成功 200：**
```json
{
  "code": 200,
  "message": "已退出登录",
  "data": null
}
```

> 后端占位返回 200，不做 Token 黑名单。前端清除本地 Token 并跳转登录页即可。

---

## 4. JWT Token 说明（全组了解）

| 属性 | 值 |
|---|---|
| 算法 | HS256（HMAC-SHA256） |
| Payload | `{userId, username, iat, exp}` |
| 过期时间 | 默认 7 天（由 `application.yml` 的 `jwt.expiration-days` 控制） |
| 密钥 | `application.yml` 的 `jwt.secret` |

**Token 在请求头中的格式：**
```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

**前端如何处理 401：**
- 若收到 `401` 响应 → 清除本地 Token → 跳转登录页
- 可用 Axios 拦截器统一处理

---

## 5. Filter 链顺序（全组遵守）

```
RequestLogFilter（A，Order = HIGHEST_PRECEDENCE）
  → JwtAuthFilter（B，Order = HIGHEST_PRECEDENCE + 1）
    → Controller（B/C/D）
```

- 日志 Filter 在最外层，可以记录**所有**请求的输入输出，包括 JWT 鉴权失败的 401 响应
- JwtAuthFilter 验证通过后才会进入 Controller
- 公开路径（register/login/logout/health）直接放行，不校验 Token

---

## 6. 受保护接口一览（7 个）

JwtAuthFilter 统一鉴权，以下接口必须带有效 Token：

| # | 方法 | 路径 | 负责人 |
|---|---|---|---|
| 4 | GET | `/api/feed/videos/next` | D |
| 5 | GET | `/api/feed/videos/previous?currentVideoId={id}` | D |
| 6 | GET | `/api/feed/videos/forward?currentVideoId={id}` | D |
| 7 | POST | `/api/videos` | C |
| 8 | GET | `/api/users/me/videos?page=1&pageSize=10` | C |
| 9 | DELETE | `/api/videos/{videoId}` | C |
| 10 | POST | `/api/videos/{videoId}/like` | D |

> 无 Token / Token 无效 / Token 过期 → 统一返回 401：
> ```json
> {"code": 401, "message": "未登录或Token无效/过期", "data": null}
> ```

---

## 7. 种子用户（测试用）

`sql/init.sql` 中已包含种子用户：

| 用户名 | 密码 |
|---|---|
| alice | 123456 |

> 密码已 BCrypt 加密存储，直接用明文登录即可。

---

## 8. 自检清单（供联调使用）

1. [ ] **注册 → 登录→ 拿到 Token**：`POST /api/auth/register` → `POST /api/auth/login` 返回 `token`
2. [ ] **携带 Token 访问受保护接口**：`Authorization: Bearer <token>` → 200
3. [ ] **不携带 Token 访问受保护接口**：→ 401
4. [ ] **携带错误 Token 访问受保护接口**：`Authorization: Bearer xxx` → 401
5. [ ] **登录 Token 超过 7 天**：→ 401（等 7 天或调小 `expiration-days` 验证）
6. [ ] **删除他人视频 → 403**：这是 C 的校验，但依赖 B 注入的 `userId` 正确
7. [ ] **`request_logs` 中登录接口的 token 被脱敏**：查库确认 `response_body` 中 `token` 为 `***`
8. [ ] **`request_logs` 中密码字段脱敏**：注册请求体的 `password` 在日志中为 `***`

---

## 9. 常见问题

**Q: 为什么我的 Controller 拿到的 userId 是 null？**
A: 检查是否在方法参数中声明了 `HttpServletRequest request`，然后调用 `CurrentUser.requireUserId(request)`。如果没带 Token 还能进入 Controller，说明 JwtAuthFilter 配置有问题。

**Q: 新增了一个公开接口，怎么加白名单？**
A: 联系 A 成员，在 `ApiConstants.isPublicPath()` 中添加路径。

**Q: Token 过期时间在哪改？**
A: `backend/src/main/resources/application.yml` 中 `jwt.expiration-days`，默认 7。

**Q: 登录成功的 token 有多长？**
A: 由 JWT 内容决定，通常 150-200 字符左右。
