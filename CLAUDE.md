# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

AI 导航站 v3.0 —— 定位为"AI 入门用户的第一站"，包含：AI 产品导航、邀请码接龙、知识库（策展导航）、提示词广场、Agent/MCP 技能库、后台管理系统。

- 后端：`backend/`（Spring Boot 3.3 + MyBatis-Plus + MySQL + JJWT，JDK 17）
- 前端：`frontend/`（Vue3 + Vite + TypeScript + Element Plus + Pinia）

## 常用命令

### 后端

```bash
cd backend

# 开发运行（需先启动 MySQL，数据库名 ai_invitecode）
./mvnw spring-boot:run

# 打包
./mvnw clean package -DskipTests

# 生产启动
java -Xms1g -Xmx2.5g -XX:+UseG1GC -Dspring.profiles.active=prod -jar target/backend-0.0.1-SNAPSHOT.jar
```

接口文档（开发环境）：`http://localhost:8080/doc.html`（Knife4j/Swagger3）

### 前端

```bash
cd frontend

npm install
npm run dev          # 开发服务器（默认连接 http://localhost:8080）
npm run build        # 生产构建，产物在 dist/
npm run preview      # 预览构建产物
# 无独立 lint/test 命令，type-check 内嵌于 build
```

### 数据库初始化

```bash
mysql -u root -p ai_invitecode < backend/src/main/resources/db/init.sql
```

## 后端架构

### 包结构 `com.aicode`

```
common/
  annotation/    - @RateLimit 限流注解
  aspect/        - RateLimitAspect（滑动窗口，ConcurrentHashMap）
  cache/         - InMemoryCacheService（替代 Redis，TTL 缓存）
  config/        - SecurityConfig, MybatisPlusConfig
  exception/     - 全局异常处理
  result/        - 统一响应 {code, message, data}
  security/      - JwtAuthenticationFilter, SecurityUtil, AdminUtil
  util/          - JwtUtil, AesUtil（AES-256-GCM）, CodePreviewUtil

module/
  auth/          - 注册/登录/图形验证码/邮件验证码
  user/          - /api/user/me（含 role, user_no 字段）
  point/         - 积分流水/每日签到
  code/          - 邀请码 CRUD + 查看事务（核心）
  task/          - 任务列表 + 奖励发放
  product/       - AI 产品列表/详情/收藏
  news/          - 资讯（旧模块，保留兼容）
  skill/         - 提示词广场（当前代码用 t_skill，v3.1 起重命名为 t_prompt）
  resource/      - 知识库 /api/resources（v3.0 新增，替代 news）
  mcp/           - Agent 技能库 /api/mcp（v3.0 新增）
  admin/         - 后台管理 /api/admin/**（需 role=9 超管或 role=2 编辑）
```

### 关键设计决策

**无 Redis**：所有缓存（限流滑动窗口、图形验证码、签到去重）均用 `InMemoryCacheService`（JVM 内存），进程重启后清空（可接受，验证码仅 5 分钟生命周期）。

**查看邀请码事务**（`code` 模块核心）：READ_COMMITTED 隔离级别 + `SELECT FOR UPDATE` 行锁，原子完成"扣50积分 → 记流水 → 标记已使用 → 加30积分给提供者"。积分不足通过 `affected rows = 0` 检测。

**权限控制**：
- `AdminUtil.requireAdmin()` → role=9
- `AdminUtil.requireEditor()` → role>=2
- `/api/admin/**` 在 SecurityConfig 中整体拦截

**新账号冷静期**：注册后 24h 不可提交/查看邀请码（后端校验）。

**限流**：`@RateLimit` 注解 + `RateLimitAspect`，各接口限制见 TECH.md §五。

## 前端架构

### 状态管理

- `stores/auth.ts`：token（localStorage 持久化）、userInfo（含 points 余额）
- `stores/point.ts`：积分流水分页数据

### 请求封装 `utils/request.ts`

- 统一注入 `Authorization: Bearer {token}`
- 响应拦截：`code !== 0` → ElMessage.error；401 → 自动登出
- 调用方直接拿到 `data` 字段（`return res.data`）

### 路由（`router/index.ts`）

| 路径 | 组件 | 权限 |
|------|------|------|
| `/` | Home | 公开 |
| `/library` | Library | 公开 |
| `/mcp` | Mcp | 公开 |
| `/skills` | Skills | 公开 |
| `/skills/:id` | SkillDetail | 公开 |
| `/news/:id` | NewsDetail | 公开 |
| `/product/:id` | ProductDetail | 公开 |
| `/profile` | Profile | requiresAuth |
| `/admin` | Admin | requiresAuth（前端） |

### 样式系统

主题：暗色科技感，cyan `#00d2ef` 主色。
- `src/assets/styles/variables.css` — CSS 自定义变量（颜色/圆角/阴影）
- `src/assets/styles/element-override.css` — Element Plus 暗色主题覆盖
- `src/assets/styles/global.css` — 全局基础样式
- **加载顺序固定**：variables → element-override → global → animations → element-plus/dist/index.css

## 数据库核心表

| 表名 | 用途 |
|------|------|
| `t_user` | 用户（role: 1普通/2编辑/9管理员，user_no: 早期成员编号） |
| `t_invite_code` | 邀请码接龙（code_encrypted AES加密，code_preview模糊预览） |
| `t_product` | AI 产品目录 |
| `t_point_record` | 积分流水（biz_type 枚举见 TECH.md §3.2） |
| `t_task` / `t_task_record` | 任务配置与完成记录 |
| `t_resource` | 知识库（策展导航，不存全文，点击跳原文） |
| `t_skill` | 提示词（v3.1 将改为 t_prompt，现有代码保持 t_skill） |
| `t_mcp_skill` | Agent 技能/MCP 目录 |
| `t_fetch_source` | RSS 自动抓取源配置 |

数据库迁移原则：新字段加 DEFAULT，不删改已有字段。

## 环境配置

后端 `application.yml`（开发默认值）：
- MySQL: `localhost:3306/ai_invitecode`，用户名 root，密码 123456
- JWT secret 和 AES key 在 `app.jwt.secret` / `app.encrypt.key`
- 生产环境通过 `application-prod.yml` 覆盖敏感配置

前端 `.env.development`：`VITE_API_BASE_URL=http://localhost:8080`

## 业务规则速查

- 注册送 100 积分；查看邀请码 -50；被查看 +30
- 新账号 24h 冷静期：不可提交/查看邀请码
- 同 IP 24h 最多注册 3 次
- 邮箱验证码有效期 5 分钟，图形验证码一次性
- 邀请码内容 AES-256-GCM 加密存储，密钥通过 `APP_ENCRYPT_KEY` 环境变量注入
