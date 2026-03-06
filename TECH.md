# AI 导航站 - 技术设计文档

**版本**: v0.3
**更新日期**: 2026-03-01

---

## 一、技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.5.x | Web + 安全 + 数据访问全家桶 |
| 运行时 | JDK | 17 LTS | LTS 版本，稳定性优先 |
| 数据库 | MySQL | 9.6.x | 主数据持久化 |
| 内存缓存 | JVM 内存缓存 | - | `InMemoryCacheService`（ConcurrentHashMap + TTL），替代 Redis；限流、验证码、签到去重均用此方案 |
| ORM | MyBatis-Plus | 3.5.16 | 简化 CRUD，支持分页 |
| 安全 | Spring Security + JJWT | - | 认证鉴权 |
| 邮件 | Spring Mail | - | 注册验证邮件 |
| 加密 | JDK AES-256-GCM | - | 邀请码内容加密存储 |
| 接口文档 | Knife4j (Swagger3) | - | 开发阶段调试（`/doc.html`） |
| 构建 | Maven | 3.9.x | 依赖管理与打包 |
| 前端 | Vue3 + Vite + TypeScript | 3.4.x / 5.x | 见 FRONTEND.md |
| 状态管理 | Pinia | 2.x | 用户 Token/信息持久化到 localStorage |
| UI 组件 | Element Plus | 2.x | 表单、对话框、标签页等 |

---

## 二、系统架构

```
[浏览器]
   │
[Nginx :80/443]
   ├── /           → 静态资源 (Vue3 dist/)
   ├── /api/**     → 反向代理 → 127.0.0.1:8080
   └── /admin/**   → 后台管理前端（独立部署或同域路由）

[Spring Boot :8080]
   ├── Controller        (路由 + 参数校验)
   ├── Service           (业务逻辑)
   ├── Mapper            (数据访问)
   ├── InMemoryCache     (限流 / 验证码 / 签到去重)
   ├── ScheduledTask     (内容自动抓取候选队列)
   └── MySQL             (持久化)
```

**架构选型说明**：6G 内存单机、低并发 ToC 场景，无需微服务和 Redis，单体 Jar 部署运维成本最低。验证码、限流、签到去重均用 JVM 内存解决，进程重启后缓存清空（可接受：验证码只有5分钟生命周期）。

---

## 三、数据库设计

### 3.1 用户表 `t_user`

```sql
CREATE TABLE t_user (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    email         VARCHAR(128) NOT NULL UNIQUE          COMMENT '邮箱（登录账号）',
    password      VARCHAR(256) NOT NULL                 COMMENT 'BCrypt 加密密码',
    nickname      VARCHAR(64)                           COMMENT '昵称',
    avatar        VARCHAR(512)                          COMMENT '头像 URL',
    points        INT          NOT NULL DEFAULT 0       COMMENT '当前积分余额',
    status        TINYINT      NOT NULL DEFAULT 1       COMMENT '1正常 2封禁',
    role          TINYINT      NOT NULL DEFAULT 1       COMMENT '1普通用户 2编辑员 9超级管理员',
    user_no       INT          UNIQUE                   COMMENT '注册序号（创世/先行者/早期成员身份依据）',
    invite_code   VARCHAR(32)  UNIQUE                   COMMENT '用户推广邀请码',
    inviter_id    BIGINT                                COMMENT '邀请人用户 ID',
    reg_ip        VARCHAR(64)                           COMMENT '注册 IP',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_invite_code (invite_code),
    INDEX idx_user_no (user_no)
) COMMENT '用户表';
```

> `user_no` 用于标记早期用户身份（前1000为创世成员，1001-10000为先行者等），注册时按序自增赋值。

### 3.2 积分流水表 `t_point_record`

```sql
CREATE TABLE t_point_record (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL                  COMMENT '用户 ID',
    delta       INT          NOT NULL                  COMMENT '积分变化（正增负减）',
    balance     INT          NOT NULL                  COMMENT '变动后余额快照',
    biz_type    TINYINT      NOT NULL                  COMMENT '业务类型，见下方枚举',
    biz_id      VARCHAR(64)                            COMMENT '关联业务 ID',
    remark      VARCHAR(256)                           COMMENT '备注描述',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '积分流水表';
```

**biz_type 枚举**：

| 值 | 说明 |
|----|------|
| 1 | 注册奖励 |
| 2 | 每日签到 |
| 3 | 查看邀请码（消耗） |
| 4 | 邀请码被查看（收益） |
| 5 | 邀请好友注册 |
| 6 | 任务奖励 |
| 7 | 知识库投稿审核通过 |
| 8 | 知识库投稿被收藏里程碑 |
| 9 | 提示词审核通过 |
| 10 | 提示词被收藏里程碑 |
| 11 | 提示词被点赞里程碑 |

### 3.3 AI 产品表 `t_product`

```sql
CREATE TABLE t_product (
    id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(128)  NOT NULL               COMMENT '产品名称',
    logo         VARCHAR(512)                         COMMENT 'Logo URL',
    description  VARCHAR(1024)                        COMMENT '产品简介（面向普通用户语言）',
    suitable_for VARCHAR(256)                         COMMENT '适合谁用',
    use_cases    VARCHAR(1024)                        COMMENT '3个具体使用场景，JSON数组',
    official_url VARCHAR(512)                         COMMENT '官网地址',
    category     VARCHAR(64)   NOT NULL               COMMENT '对话/绘图/编程/视频/写作/搜索/效率/Agent/音乐/其他',
    access_type  TINYINT       NOT NULL DEFAULT 1     COMMENT '1直接访问 2需要工具 3需要邀请码',
    free_quota   VARCHAR(256)                         COMMENT '免费额度说明',
    pricing_info VARCHAR(512)                         COMMENT '定价信息',
    status       TINYINT       NOT NULL DEFAULT 1     COMMENT '1上线 2下线',
    sort         INT           NOT NULL DEFAULT 0     COMMENT '排序权重',
    code_count   INT           NOT NULL DEFAULT 0     COMMENT '当前可用邀请码数（冗余字段）',
    collect_count INT          NOT NULL DEFAULT 0     COMMENT '收藏数',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_status (category, status, sort)
) COMMENT 'AI 产品表';
```

### 3.4 邀请码表 `t_invite_code`

```sql
CREATE TABLE t_invite_code (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    product_id      BIGINT       NOT NULL               COMMENT '所属产品 ID',
    provider_id     BIGINT       NOT NULL               COMMENT '提供者用户 ID',
    parent_code_id  BIGINT                              COMMENT '接龙来源邀请码 ID，NULL=首发',
    code_encrypted  VARCHAR(512) NOT NULL               COMMENT 'AES-256 加密后的邀请码内容',
    code_preview    VARCHAR(32)  NOT NULL               COMMENT '预览文本（如：ab******）',
    status          TINYINT      NOT NULL DEFAULT 1     COMMENT '1可用 2已使用',
    viewer_id       BIGINT                              COMMENT '查看者用户 ID',
    view_time       DATETIME                            COMMENT '查看时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_status (product_id, status, create_time),
    INDEX idx_provider_id (provider_id),
    INDEX idx_parent_code_id (parent_code_id)
) COMMENT '邀请码接龙表';
```

### 3.5 知识库表 `t_resource`

> 替代原 `t_news` 表。定位从"资讯"升级为"策展导航"，核心字段是 `source_url`，内容不存全文。

```sql
CREATE TABLE t_resource (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    title         VARCHAR(256)  NOT NULL               COMMENT '标题（编辑可优化为用户友好表述）',
    summary       VARCHAR(512)                          COMMENT '2句话摘要：讲什么 + 适合谁',
    source_url    VARCHAR(512)  NOT NULL               COMMENT '原文链接（核心字段，点击直接跳转）',
    source_name   VARCHAR(128)                          COMMENT '来源名称：Twitter/@xxx / Medium / 少数派',
    cover         VARCHAR(512)                          COMMENT '封面图 URL',
    tags          VARCHAR(256)                          COMMENT '标签，逗号分隔',
    category      VARCHAR(64)   NOT NULL               COMMENT '大模型/Agent框架/MCP/教程/工具评测/行业洞察',
    difficulty    TINYINT       NOT NULL DEFAULT 1     COMMENT '1入门 2进阶 3专业',
    author_id     BIGINT                               COMMENT '投稿用户ID，NULL=编辑录入',
    status        TINYINT       NOT NULL DEFAULT 1     COMMENT '1待审核 2已发布 3已拒绝',
    collect_count INT           NOT NULL DEFAULT 0,
    view_count    INT           NOT NULL DEFAULT 0,
    is_auto_fetched TINYINT     NOT NULL DEFAULT 0     COMMENT '0手动录入 1自动抓取候选',
    publish_time  DATETIME,
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status_category (status, category, publish_time),
    INDEX idx_author (author_id)
) COMMENT '知识库表（策展导航，不存全文）';
```

### 3.6 知识库收藏表 `t_resource_collect`

```sql
CREATE TABLE t_resource_collect (
    id          BIGINT   PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_resource_user (resource_id, user_id)
) COMMENT '知识库收藏表';
```

### 3.7 提示词表 `t_prompt`

> 原 `t_skill` 表，改名为 `t_prompt` 更准确反映内容。现有代码的 t_skill 逻辑保持，后续版本统一迁移。

```sql
CREATE TABLE t_prompt (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    title         VARCHAR(256)  NOT NULL               COMMENT '提示词标题（一句话说明用途）',
    content       TEXT          NOT NULL               COMMENT '提示词全文',
    applicable    VARCHAR(256)                          COMMENT '适用产品，逗号分隔',
    category      VARCHAR(64)                           COMMENT '写作/编程/设计/营销/分析/翻译/教育/职场/生活',
    tags          VARCHAR(256)                          COMMENT '场景标签',
    author_id     BIGINT        NOT NULL               COMMENT '提交者用户ID',
    status        TINYINT       NOT NULL DEFAULT 1     COMMENT '1待审核 2已发布 3已拒绝',
    like_count    INT           NOT NULL DEFAULT 0,
    collect_count INT           NOT NULL DEFAULT 0,
    use_count     INT           NOT NULL DEFAULT 0,
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status_category (status, category, collect_count)
) COMMENT '提示词表';
```

### 3.8 Agent 技能 / MCP 表 `t_mcp_skill`

```sql
CREATE TABLE t_mcp_skill (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(128)  NOT NULL               COMMENT '技能/MCP名称',
    description     VARCHAR(512)                          COMMENT '面向用户的功能描述',
    value_sentence  VARCHAR(256)                          COMMENT '一句话价值说明，如"让AI帮你管理GitHub"',
    type            TINYINT       NOT NULL               COMMENT '1 MCP Server 2 Agent Skill',
    category        VARCHAR(64)                           COMMENT '文件管理/代码/通信/数据库/搜索/日历/其他',
    framework       VARCHAR(256)                          COMMENT '适用框架：OpenClaw,AutoGen,通用...',
    difficulty      TINYINT       NOT NULL DEFAULT 1     COMMENT '1入门 2进阶 3专业',
    source_url      VARCHAR(512)                          COMMENT 'GitHub/文档链接',
    install_guide   TEXT                                  COMMENT '安装说明摘要',
    tags            VARCHAR(256),
    status          TINYINT       NOT NULL DEFAULT 1     COMMENT '1待审核 2已发布',
    collect_count   INT           NOT NULL DEFAULT 0,
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type_category (type, category, status)
) COMMENT 'Agent技能/MCP目录表';
```

### 3.9 任务配置表 `t_task`

```sql
CREATE TABLE t_task (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(128) NOT NULL               COMMENT '任务名称',
    task_type     VARCHAR(64)  NOT NULL UNIQUE        COMMENT '任务类型标识',
    frequency     TINYINT      NOT NULL               COMMENT '1每日 2每周 3一次性',
    points_reward INT          NOT NULL               COMMENT '完成奖励积分',
    status        TINYINT      NOT NULL DEFAULT 1     COMMENT '1启用 2停用',
    sort          INT          NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '任务配置表';
```

### 3.10 任务完成记录 `t_task_record`

```sql
CREATE TABLE t_task_record (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    task_id     BIGINT      NOT NULL,
    task_type   VARCHAR(64) NOT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_task_date (user_id, task_type, create_time)
) COMMENT '任务完成记录';
```

### 3.11 产品收藏表 `t_product_collect`

```sql
CREATE TABLE t_product_collect (
    id          BIGINT   PRIMARY KEY AUTO_INCREMENT,
    product_id  BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_user (product_id, user_id)
) COMMENT '产品收藏表';
```

---

## 四、关键业务逻辑

### 4.1 查看邀请码（事务行锁）

```
BEGIN TRANSACTION（隔离级别 READ COMMITTED）
  1. SELECT ... FOR UPDATE 锁住邀请码行，校验 status=1（可用）
  2. SELECT ... FOR UPDATE 锁住查看者用户行，校验 points >= 50
  3. UPDATE t_user SET points = points - 50 WHERE id=viewerId AND points >= 50
     → affected rows = 0 则抛业务异常"积分不足"
  4. INSERT t_point_record (delta=-50, biz_type=3)
  5. UPDATE t_invite_code SET status=2, viewer_id=?, view_time=NOW()
  6. UPDATE t_user SET points = points + 30 WHERE id=providerId
  7. INSERT t_point_record (delta=+30, biz_type=4)
  8. UPDATE t_product SET code_count = code_count - 1
COMMIT
解密 code_encrypted，返回明文给查看者
```

### 4.2 积分不能为负的保障

```sql
UPDATE t_user SET points = points - 50
WHERE id = ? AND points >= 50
-- affected rows = 0 → 抛业务异常"积分不足"
```

### 4.3 里程碑奖励去重

收藏/点赞里程碑奖励以 `t_point_record` 的 `(biz_type, biz_id, user_id)` 唯一性作为防重机制，不依赖内存。

### 4.4 早期用户编号分配

用户注册时通过数据库序列或自增逻辑赋值 `user_no`：

```sql
-- 注册时获取下一个序号
SELECT MAX(user_no) + 1 FROM t_user;  -- 简单方案（低并发场景）
-- 或：t_user.id 直接作为 user_no 使用
```

---

## 五、限流方案（JVM 内存滑动窗口）

| 接口 | 维度 | 限制 | 窗口 |
|------|------|------|------|
| 注册接口 | 同 IP | 3次 | 24h |
| 登录接口 | 同邮箱 | 10次 | 5min |
| 发送验证码 | 同邮箱 | 1次 | 60s |
| 查看邀请码 | 同用户 | 5次 | 1min |
| 提交邀请码 | 同用户 | 10次 | 1h |
| 内容投稿 | 同用户 | 20次 | 1h |

实现：`RateLimitAspect` 使用 `ConcurrentHashMap<String, Deque<Long>>` 维护滑动窗口，`@RateLimit` 注解统一拦截。

---

## 六、API 设计

统一响应格式：
```json
{ "code": 0, "message": "success", "data": {} }
```

### 6.1 认证模块 `/api/auth`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/captcha` | 无 | 图形验证码 |
| POST | `/send-email-code` | 无 | 发送邮箱验证码 |
| POST | `/register` | 无 | 邮箱注册 |
| POST | `/login` | 无 | 登录，返回 token |
| POST | `/logout` | 登录 | 登出 |

### 6.2 用户模块 `/api/user`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/me` | 登录 | 当前用户信息 |
| PUT | `/me` | 登录 | 更新昵称/头像 |

### 6.3 积分模块 `/api/points`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/records` | 登录 | 积分流水（分页） |
| POST | `/checkin` | 登录 | 每日签到 |

### 6.4 产品模块 `/api/products`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/` | 无 | 产品列表（分类筛选、分页） |
| GET | `/{id}` | 无 | 产品详情 |
| POST | `/{id}/collect` | 登录 | 收藏/取消收藏 |

### 6.5 邀请码模块 `/api/codes`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/{productId}/list` | 无 | 邀请码接龙列表（模糊预览） |
| POST | `/` | 登录 | 提交邀请码 |
| POST | `/{id}/view` | 登录 | 花积分查看明文 |
| GET | `/mine` | 登录 | 我提交的邀请码 |

### 6.6 知识库模块 `/api/resources`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/` | 无 | 列表（分类/难度筛选、分页） |
| GET | `/{id}` | 无 | 详情（view_count+1） |
| POST | `/submit` | 登录 | 用户投稿（待审核） |
| POST | `/{id}/collect` | 登录 | 收藏/取消收藏 |

### 6.7 提示词模块 `/api/prompts`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/` | 无 | 列表（分类筛选、分页） |
| GET | `/{id}` | 无 | 详情（use_count+1） |
| POST | `/` | 登录 | 提交提示词（待审核） |
| POST | `/{id}/like` | 登录 | 点赞/取消点赞 |
| POST | `/{id}/collect` | 登录 | 收藏/取消收藏 |
| GET | `/mine` | 登录 | 我的提示词 |

### 6.8 Agent 技能库 `/api/mcp`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/` | 无 | 列表（类型/分类筛选、分页） |
| GET | `/{id}` | 无 | 详情 |
| POST | `/{id}/collect` | 登录 | 收藏/取消收藏 |

### 6.9 任务模块 `/api/tasks`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/` | 登录 | 任务列表（含完成状态） |

### 6.10 后台管理模块 `/api/admin`（需管理员权限）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/PUT | `/resources` | 知识库内容管理 |
| PUT | `/resources/{id}/approve` | 审核通过投稿 |
| PUT | `/resources/{id}/reject` | 拒绝投稿 |
| GET/POST/PUT | `/products` | 工具库管理 |
| GET/PUT | `/prompts` | 提示词审核管理 |
| GET/POST/PUT | `/mcp` | MCP技能库管理 |
| GET/PUT | `/users` | 用户管理 |
| GET | `/fetch-queue` | 查看自动抓取候选队列 |
| POST | `/fetch-queue/{id}/approve` | 确认上线 |
| POST | `/fetch-queue/{id}/reject` | 拒绝 |

---

## 七、后台管理系统

### 7.1 架构方案

后台与前台共用同一个 Spring Boot 服务，通过 `/api/admin/**` 路由区分，Security 层对此路径要求 `role=ADMIN` 权限。

后台前端可选方案：
- **推荐**：直接复用现有 Vue3 项目，新增 `/admin` 路由组，简单 CRUD 表格界面
- 备选：使用低代码后台（如 Ant Design Pro）独立部署

### 7.2 核心功能

**内容审核工作流**：
```
用户投稿 → status=1（待审核）
  ↓
后台管理员看到待审核队列
  ↓
审核通过 → status=2（已发布）+ 触发积分奖励
审核拒绝 → status=3（已拒绝）+ 可选通知用户
```

**自动抓取候选队列**：
```
定时任务抓取 RSS/指定来源 → 存入 t_resource（is_auto_fetched=1, status=1）
  ↓
后台显示候选列表（标题 + 来源 + 摘要）
  ↓
编辑一键确认上线 → status=2
编辑拒绝 → status=3（不进入正式内容库）
```

---

## 八、内容自动化方案

### 8.1 知识库自动抓取

**目标**：定期从指定来源发现候选内容，降低编辑工作量。

**技术方案**：Spring `@Scheduled` 定时任务 + HTTP 抓取

```java
// 每小时执行一次
@Scheduled(cron = "0 0 * * * *")
public void fetchCandidates() {
    // 1. 遍历配置的 RSS 源列表
    // 2. 解析 RSS/Atom，提取：title / link / description / pubDate
    // 3. 去重检查（source_url 唯一索引）
    // 4. 插入 t_resource（status=1, is_auto_fetched=1）
    // 5. 后台人工确认上线
}
```

**初期抓取源**：
- AI 公司官方博客 RSS（OpenAI / Anthropic / Google DeepMind）
- 少数派 AI 标签
- 其他高质量 AI 媒体 RSS

**注意**：抓取后不自动发布，必须经后台人工确认后上线，保证质量。

### 8.2 内容来源配置

抓取源通过数据库配置，支持热更新：

```sql
CREATE TABLE t_fetch_source (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(128) NOT NULL    COMMENT '来源名称',
    url         VARCHAR(512) NOT NULL    COMMENT 'RSS/Atom URL 或网页 URL',
    type        TINYINT      NOT NULL    COMMENT '1 RSS 2 网页解析',
    category    VARCHAR(64)              COMMENT '默认分类',
    status      TINYINT      NOT NULL DEFAULT 1  COMMENT '1启用 2停用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '内容抓取源配置';
```

---

## 九、安全方案

### 9.1 密码安全
- BCrypt 加密，cost factor = 10
- 不存储明文密码

### 9.2 JWT 方案

```
Access Token：
  - 有效期：2h
  - 存储：前端 localStorage
  - Payload：userId, email, role, 过期时间

登出处理：
  - 前端删除 localStorage Token
  - Token 有效期仅 2h，低并发场景可接受
```

### 9.3 数据加密
- 邀请码内容：AES-256-GCM 加密存库
- 密钥通过环境变量注入（`APP_ENCRYPT_KEY`）

### 9.4 防重复提交
- 前端：操作后立即 loading 禁用，防用户连击
- 后端：事务行锁 + status 状态机校验

### 9.5 后台访问控制
- `/api/admin/**` 路径在 SecurityConfig 中要求 `role >= EDITOR`
- 超级管理员（role=9）可执行用户管理、积分调整等高权限操作
- 编辑员（role=2）只可执行内容审核操作

---

## 十、部署方案

### 服务器资源分配（6G 内存）

| 服务 | 分配 |
|------|------|
| JVM（Spring Boot） | -Xms1g -Xmx2.5g |
| MySQL innodb_buffer_pool_size | 2G |
| Nginx + OS 预留 | ~1.5G |

### 目录结构

```
/opt/app/
├── backend/
│   ├── app.jar
│   └── application-prod.yml
├── frontend/
│   └── dist/
└── nginx/
    └── conf.d/app.conf
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /opt/app/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 启动命令

```bash
java -Xms1g -Xmx2.5g \
     -XX:+UseG1GC \
     -Dspring.profiles.active=prod \
     -jar /opt/app/backend/app.jar
```

### 数据备份

- MySQL：每日凌晨全量 mysqldump，保留 7 天
- 备份文件上传至 OSS

---

## 十一、技术路线图

### 阶段一：基础成型（当前 → v3.0）

**目标**：把 demo 变成完整可运营的产品

```
优先级  任务
P0     后台管理系统（内容审核核心能力）
P0     知识库模块（t_resource，策展导航，替代 t_news）
P0     Agent 技能库（t_mcp_skill，MCP 目录）
P0     提示词广场重命名（t_skill → t_prompt，逻辑不变）
P1     Onboarding 流程
P1     任务系统完善（新手任务+里程碑）
P1     全站搜索（MySQL LIKE，先跑通）
P2     早期用户编号体系（user_no）
```

### 阶段二：内容与增长（v3.1）

```
P0     RSS 自动抓取候选队列
P0     SEO：vite-plugin-ssg 预渲染工具/提示词/知识库页
P1     用户收藏夹（统一管理收藏的工具/提示词/知识库）
P1     工具评价功能（评分 + 短评）
P1     排行榜（贡献者榜/热门提示词榜）
P2     分享卡生成（带平台水印）
```

### 阶段三：商业化（v3.2+）

```
P0     广告位系统（工具赞助位、知识库赞助）
P0     Nuxt 3 SSR 迁移（SEO 完整支持）
P1     搜索升级（Meilisearch 中文全文搜索）
P1     合作伙伴入口（AI 项目方认领工具页）
P2     会员体系（积分换特权）
P2     移动端优化
```

### SEO 方案演进

| 阶段 | 方案 | 说明 |
|------|------|------|
| 过渡期 | `vite-plugin-ssg` 静态预渲染 | 改动小，覆盖主要内容页 |
| 正式迁移 | Nuxt 3 SSR | Vue3 同生态，SEO 完整支持 |

### 搜索方案演进

| 阶段 | 方案 | 说明 |
|------|------|------|
| 初期 | MySQL LIKE 查询 | 数据量小时够用 |
| 中期 | Meilisearch | 轻量自部署，中文分词，单机可用 |
| 不推荐 | Elasticsearch | 资源消耗过大 |
