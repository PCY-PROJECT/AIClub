# AI邀请码导航 - 前端手册

**版本**: v0.2
**更新日期**: 2026-02-27
**设计参考**: [evomap.ai](https://evomap.ai) — 深色科技风，cyan/teal 主色

---

## 零、设计系统（Design System）

> 整体风格：**暗色科技感**。深色背景 + cyan/teal 点缀色，卡片半透明叠加，边框细线 + hover 发光效果，营造「AI 工具导航」的高级感。

### 0.1 颜色变量

在 `src/assets/styles/variables.css` 中定义，作为全局 CSS Custom Properties：

```css
:root {
  /* ─── 背景层级 ─── */
  --color-bg-base:     #09090f;   /* 页面最底层背景 */
  --color-bg-elevated: #111118;   /* 卡片/面板背景 */
  --color-bg-overlay:  #1a1a24;   /* 弹窗/下拉背景 */

  /* ─── 主色：Cyan ─── */
  --color-primary:     #00d2ef;   /* 主色（按钮、高亮、链接） */
  --color-primary-dim: rgba(0, 210, 239, 0.15);  /* 主色低透（hover 背景） */
  --color-primary-glow:rgba(0, 210, 239, 0.3);   /* 主色光晕（focus ring） */

  /* ─── 文字 ─── */
  --color-text-primary: #f0f2f5;  /* 主要文字 */
  --color-text-muted:   #6a7282;  /* 次要/提示文字 */
  --color-text-disabled:#3d4350;  /* 禁用态文字 */

  /* ─── 边框 ─── */
  --color-border:       #1e2030;  /* 默认边框 */
  --color-border-hover: rgba(0, 210, 239, 0.4); /* hover 边框 */

  /* ─── 状态色 ─── */
  --color-success:  #00c758;
  --color-warning:  #f99c00;
  --color-error:    #ff6568;

  /* ─── 圆角 ─── */
  --radius-sm:  6px;
  --radius-md:  10px;
  --radius-lg:  14px;
  --radius-xl:  20px;

  /* ─── 阴影 ─── */
  --shadow-card: 0 1px 3px rgba(0,0,0,0.4), 0 0 0 1px var(--color-border);
  --shadow-card-hover: 0 4px 20px rgba(0,0,0,0.5), 0 0 0 1px var(--color-border-hover);
  --shadow-glow: 0 0 16px var(--color-primary-glow);
}
```

---

### 0.2 字体

```css
/* 引入 Google Fonts（与 evomap 同款）*/
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Rajdhani:wght@400;500;600&display=swap');

:root {
  --font-display: 'Orbitron', system-ui, sans-serif;   /* 标题/Logo：科技感 */
  --font-body:    'Rajdhani', system-ui, sans-serif;    /* 正文/UI：清晰易读 */
  --font-mono:    'JetBrains Mono', ui-monospace, monospace; /* 邀请码展示 */
}

body {
  font-family: var(--font-body);
  font-size: 15px;
  color: var(--color-text-primary);
  background: var(--color-bg-base);
}
```

---

### 0.3 Element Plus 主题覆盖

在 `src/assets/styles/element-override.css` 中覆盖 Element Plus 默认变量：

```css
:root {
  /* 主色 */
  --el-color-primary:         #00d2ef;
  --el-color-primary-light-3: rgba(0,210,239,0.5);
  --el-color-primary-light-7: rgba(0,210,239,0.2);
  --el-color-primary-dark-2:  #00b7d7;

  /* 背景 */
  --el-bg-color:              #111118;
  --el-bg-color-page:         #09090f;
  --el-bg-color-overlay:      #1a1a24;

  /* 边框 */
  --el-border-color:          #1e2030;
  --el-border-color-light:    #282a3a;
  --el-border-radius-base:    10px;
  --el-border-radius-small:   6px;

  /* 文字 */
  --el-text-color-primary:    #f0f2f5;
  --el-text-color-regular:    #b0b8c8;
  --el-text-color-secondary:  #6a7282;
  --el-text-color-placeholder:#3d4350;

  /* 填充 */
  --el-fill-color:            #1a1a24;
  --el-fill-color-light:      #1e2030;

  /* 组件圆角 */
  --el-card-border-radius:    14px;
  --el-dialog-border-radius:  16px;
  --el-button-border-radius:  8px;
}
```

---

### 0.4 核心组件样式规范

#### 卡片（ProductCard / CodeCard）

```css
.app-card {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}
.app-card:hover {
  border-color: var(--color-border-hover);
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}
```

#### 主按钮

```css
.btn-primary {
  background: var(--color-primary);
  color: #09090f;                    /* 深色字在亮色背景上 */
  font-weight: 600;
  border-radius: var(--radius-md);
  padding: 8px 20px;
  border: none;
  transition: background 0.2s, box-shadow 0.2s;
}
.btn-primary:hover {
  background: #00b7d7;
  box-shadow: var(--shadow-glow);
}
```

#### 次级按钮（描边）

```css
.btn-outline {
  background: transparent;
  color: var(--color-primary);
  border: 1px solid var(--color-border-hover);
  border-radius: var(--radius-md);
  padding: 8px 20px;
  font-weight: 500;
  transition: background 0.2s, border-color 0.2s;
}
.btn-outline:hover {
  background: var(--color-primary-dim);
}
```

#### 邀请码显示（等宽字体 + 发光效果）

```css
.code-display {
  font-family: var(--font-mono);
  background: rgba(0, 210, 239, 0.08);
  border: 1px solid rgba(0, 210, 239, 0.25);
  border-radius: var(--radius-md);
  padding: 10px 16px;
  color: var(--color-primary);
  letter-spacing: 0.08em;
  font-size: 15px;
}
```

#### 积分徽章

```css
.points-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: rgba(0, 210, 239, 0.12);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  padding: 3px 10px;
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font-display);
}
```

#### 顶部导航栏

```css
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(9, 9, 15, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border);
  height: 60px;
}
```

#### 分类 Tab 栏

```css
.category-tab {
  padding: 6px 16px;
  border-radius: var(--radius-md);
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
}
.category-tab.active,
.category-tab:hover {
  background: var(--color-primary-dim);
  color: var(--color-primary);
}
```

---

### 0.5 页面布局规范

| 元素 | 规范 |
|------|------|
| 页面最大宽度 | `1200px`，左右 `padding: 0 24px` |
| 产品卡片网格 | PC：4列；平板：3列；小屏：2列 |
| 卡片间距 | `gap: 16px` |
| 页面内容上间距 | `padding-top: 32px` |
| Section 间距 | `margin-bottom: 48px` |

---

### 0.6 背景纹理（可选，增强科技感）

```css
body::before {
  content: '';
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    radial-gradient(ellipse 80% 50% at 50% -10%,
      rgba(0, 210, 239, 0.06) 0%, transparent 70%),
    radial-gradient(ellipse 60% 40% at 100% 100%,
      rgba(0, 186, 167, 0.04) 0%, transparent 60%);
  pointer-events: none;
}
```

---

## 一、技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4.x | 核心框架（Composition API） |
| Vite | 5.x | 构建工具，开发体验优先 |
| TypeScript | 5.x | 类型安全 |
| Vue Router | 4.x | 客户端路由 |
| Pinia | 2.x | 全局状态管理 |
| Element Plus | 2.x | UI 组件库（PC 端优先，配合暗色主题覆盖） |
| Axios | 1.x | HTTP 请求封装 |
| @vueuse/core | 10.x | 工具组合函数（useClipboard、useLocalStorage 等） |

> **字体引入**：Google Fonts（Orbitron + Rajdhani），在 `index.html` 中 `<link>` 引入，Vite 不打包字体文件。

---

## 二、目录结构

```
src/
├── api/                   # 接口层（每个模块一个文件）
│   ├── auth.ts            # 注册、登录、登出
│   ├── product.ts         # 产品列表、详情
│   ├── code.ts            # 邀请码接龙、提交、查看
│   ├── point.ts           # 积分余额、流水、签到
│   └── task.ts            # 任务列表
├── assets/
│   ├── styles/
│   │   ├── variables.css        # CSS 自定义变量（颜色/圆角/阴影）
│   │   ├── element-override.css # Element Plus 暗色主题覆盖
│   │   ├── global.css           # 全局基础样式（body、滚动条等）
│   │   └── animations.css       # 过渡动画（积分滚动、卡片 fade 等）
│   └── images/                  # 图片资源
├── components/            # 公共组件
│   ├── AppHeader.vue      # 顶部导航栏（含积分 Badge + 毛玻璃效果）
│   ├── ProductCard.vue    # 产品卡片（暗色卡片 + hover 发光）
│   ├── CodeCard.vue       # 邀请码接龙列表项（等宽字体展示）
│   ├── PointBadge.vue     # 积分徽章（cyan 色 badge）
│   ├── CaptchaInput.vue   # 图形验证码输入组件
│   └── PointToast.vue     # 积分变动 Toast（右下角弹出）
├── composables/           # 组合式函数
│   ├── useAuth.ts         # 登录态判断、跳转登录
│   └── usePoint.ts        # 积分变动提示（触发 PointToast）
├── router/
│   └── index.ts           # 路由配置 + 守卫
├── stores/
│   ├── auth.ts            # 用户信息、Token
│   └── point.ts           # 积分实时状态
├── types/
│   └── index.ts           # 全局 TypeScript 类型定义
├── utils/
│   ├── request.ts         # Axios 封装（统一错误处理）
│   └── format.ts          # 工具函数（时间格式化等）
└── views/
    ├── Home.vue           # 首页（产品分类 + 网格卡片）
    ├── ProductDetail.vue  # 产品详情 + 邀请码接龙
    ├── Profile.vue        # 个人中心
    ├── Login.vue          # 登录页
    └── Register.vue       # 注册页
```

**styles 加载顺序**（在 `main.ts` 中按序 import）：

```typescript
// main.ts
import './assets/styles/variables.css'
import './assets/styles/element-override.css'
import './assets/styles/global.css'
import './assets/styles/animations.css'
import 'element-plus/dist/index.css'
```

---

## 三、路由配置

```typescript
// router/index.ts
const routes = [
  { path: '/',             component: Home },
  { path: '/product/:id', component: ProductDetail },
  { path: '/profile',     component: Profile,  meta: { requiresAuth: true } },
  { path: '/login',       component: Login },
  { path: '/register',    component: Register },
]

// 路由守卫：未登录访问 requiresAuth 页面时跳转登录
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})
```

---

## 四、状态管理

### auth store

```typescript
// stores/auth.ts
interface UserInfo {
  id: number
  nickname: string
  email: string
  points: number
}

const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') as string | null,
    userInfo: null as UserInfo | null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    setToken(token: string) { ... },
    setUserInfo(info: UserInfo) { ... },
    logout() {
      // 清除 token 和 userInfo，跳转登录页
    },
    // 积分变动后调用，同步更新 userInfo.points
    updatePoints(newBalance: number) { ... },
  },
})
```

### point store

```typescript
// stores/point.ts（用于个人中心分页流水）
const usePointStore = defineStore('point', {
  state: () => ({
    records: [] as PointRecord[],
    total: 0,
  }),
})
```

---

## 五、请求封装规范

```typescript
// utils/request.ts
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

// 请求拦截：自动带上 Authorization: Bearer {token}
instance.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

// 响应拦截：统一处理业务错误和 HTTP 错误
instance.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(res)
    }
    return res.data   // 调用方直接拿到 data 字段
  },
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore().logout()    // Token 失效，跳转登录
    } else {
      ElMessage.error('网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)
```

**约定**：所有 `api/*.ts` 中的函数直接返回 `request(...)` 的 Promise，调用方使用 `async/await`。

---

## 六、页面说明

### 6.1 首页 `Home.vue`

- **顶部**：产品分类 Tab 栏（全部 / 对话 / 绘图 / 代码 / 视频 / 音乐）
- **主体**：产品卡片网格（3列 PC，2列平板）
- **产品卡片信息**：Logo、名称、分类标签、简介（截断）、可用邀请码数量徽标
- **数据获取**：页面加载时请求产品列表，分类切换时重新请求

### 6.2 产品详情页 `ProductDetail.vue`

- **上半区**：产品 Logo、名称、分类、简介、官网链接
- **下半区**：邀请码接龙列表（核心）
- **页面底部**：固定"贡献我的邀请码"悬浮按钮（需登录）

**接龙列表渲染结构**（`CodeCard.vue`）：

```
[首发条目] 无缩进，无标签
  └─ [接龙条目] 左侧缩进 16px，灰色"接龙"角标
  └─ [接龙条目] ...

[首发条目] 无接龙子项
```

| 字段 | 展示内容 |
|------|---------|
| 身份区 | 头像（24px）+ 昵称 + "接龙"标签（接龙条目才显示） |
| 码内容区 | 可用：模糊预览文字；已使用：灰色"已被领取" |
| 操作区 | 可用：蓝色"查看(-50积分)"按钮；已使用：按钮不显示 |
| 时间 | 相对时间（如"3小时前"） |

**列表数据组装逻辑（前端内存处理）**：
```typescript
// 后端返回平铺数组，前端按 parent_code_id 分组
function buildTree(list: CodeItem[]) {
  const roots = list.filter(c => !c.parentCodeId)
  const children = list.filter(c => !!c.parentCodeId)
  return roots.map(root => ({
    ...root,
    children: children.filter(c => c.parentCodeId === root.id)
  }))
}
// 排序：含可用子项的组 → 自身可用的根节点 → 全部已使用的组
```

### 6.3 个人中心 `Profile.vue`

- **顶部**：头像、昵称、积分余额（大号展示）、签到按钮
- **Tab 区域**：
  - 积分流水（biz_type 图标 + 描述 + 时间 + 积分变动）
  - 我的邀请码（产品名 + 状态 + 提交时间 + 获得积分）
  - 任务中心（任务列表 + 进度 + 领取状态）

### 6.4 登录页 `Login.vue`

```
邮箱输入框
密码输入框（含显示/隐藏切换）
图形验证码（图片 + 输入框）
登录按钮
---
"没有账号？立即注册" 跳转链接
```

### 6.5 注册页 `Register.vue`

```
邮箱输入框
图形验证码（图片 + 输入框）
[发送验证码] 按钮（60s 倒计时）
邮箱验证码输入框
密码输入框
确认密码输入框
注册按钮
---
"已有账号？立即登录" 跳转链接
```

---

## 七、关键交互设计

### 7.1 查看邀请码 → 成功弹窗 → 接龙引导（完整流程）

```
用户点击"查看(-50积分)"
  ↓
【第一层弹窗】确认消费
  标题："确认查看邀请码？"
  内容："将消耗 50 积分，邀请码只能被查看一次，查看后不可撤销。"
  按钮："确认查看" / "取消"
  ↓（用户确认）
按钮 loading，调用 POST /api/codes/{id}/view
  ↓（成功）

【第二层弹窗】结果 + 接龙引导（el-dialog，不可点击蒙层关闭）

  ┌──────────────────────────────────────────┐
  │  🎉 邀请码获取成功！                      │
  │                                          │
  │  ┌────────────────────────────────────┐  │
  │  │   abcde-12345-fghij               │  │
  │  └────────────────────────────────────┘  │
  │              [ 复制邀请码 ]               │
  │                                          │
  │  ─────────────────────────────────────  │
  │  💡 用此码注册成功后，分享你的新邀请码    │
  │     可获得 +30 积分！                     │
  │                                          │
  │  [ 我也来接龙 +30积分 ]      [ 暂不 ]    │
  └──────────────────────────────────────────┘

  ↓ 点击"复制邀请码"
  navigator.clipboard.writeText(code)
  按钮文字变为"已复制 ✓"（2s后复原）

  ↓ 点击"我也来接龙"
  关闭当前弹窗 → 打开"贡献邀请码"输入弹窗
  （产品自动锁定为当前产品，parentCodeId 由组件内部记录，用户不可见）

  ↓ 点击"暂不"
  关闭弹窗，列表中刚才查看的条目刷新为"已被领取"

  ↓（失败：积分不足）
ElMessage.error("积分不足，去任务中心获取积分")
  ↓（失败：已被他人领取）
ElMessage.warning("手慢了，该邀请码刚被他人领取，请查看其他可用邀请码")
刷新列表
```

**状态管理要点**：
- 组件内用 `ref<number | null> pendingParentCodeId` 记录"待接龙的来源码ID"
- 点击"我也来接龙"时将该值传入提交弹窗组件
- 提交成功后重置为 `null`

### 7.2 提交邀请码弹窗流程

两个入口触发同一个弹窗组件 `SubmitCodeDialog.vue`，通过 `props` 区分：

| 入口 | props | 表现 |
|------|-------|------|
| 页面底部悬浮按钮（主动首发） | `parentCodeId: null` | 弹窗标题"贡献邀请码"，无特殊提示 |
| 查看成功后点击"我也来接龙" | `parentCodeId: number` | 弹窗标题"接龙 - 贡献你的邀请码"，提示文案强调接龙 |

```
弹窗内容：

  ┌──────────────────────────────────────────┐
  │  接龙 - 贡献你的邀请码 (产品名: ChatGPT)  │
  │                                          │
  │  [接龙条目存在时]                         │
  │  ↳ 接在「张三」的邀请码后面              │
  │                                          │
  │  你的邀请码：                            │
  │  ┌────────────────────────────────────┐  │
  │  │  请输入邀请码内容                  │  │
  │  └────────────────────────────────────┘  │
  │                                          │
  │  ⚠️ 该邀请码只能被1位用户查看，           │
  │     查看后你将获得 +30 积分              │
  │                                          │
  │       [ 确认提交 ]    [ 取消 ]           │
  └──────────────────────────────────────────┘

  ↓ 确认提交
  POST /api/codes  body: { productId, code, parentCodeId }
  ↓ 成功
  ElMessage.success("提交成功！等待他人查看后获得积分")
  关闭弹窗，接龙列表末尾追加新条目（本地插入，无需刷新整页）
  ↓ 失败（重复提交同一产品）
  ElMessage.warning("你已为该产品贡献过邀请码，等待被查看中")
```

### 7.3 积分变动实时提示

- 积分增减后，顶部导航栏积分数字以动画形式更新（数字滚动效果）
- 同时在右下角弹出 Toast：`+30 积分（邀请码被查看）` 或 `-50 积分（查看邀请码）`

### 7.4 新用户冷静期提示

注册24h内点击查看邀请码时，显示友好提示：
> "账号注册不足24小时，暂不可查看邀请码，可先提交自己的邀请码赚取积分。"

---

## 八、环境变量

```
# .env.development
VITE_API_BASE_URL=http://localhost:8080

# .env.production
VITE_API_BASE_URL=https://your-domain.com
```

---

## 九、构建与部署

```bash
# 安装依赖
npm install

# 本地开发
npm run dev

# 生产构建
npm run build
# 产物在 dist/ 目录，部署到 Nginx

# 类型检查
npm run type-check
```

---

## 十、各页面视觉效果描述（对照 evomap.ai 风格）

### 10.1 首页

```
╔══════════════════════════════════════════════════════════╗
║  [毛玻璃导航栏 bg:rgba(9,9,15,0.85) blur:12px]           ║
║  🤖 AI 邀请码    [全部][对话][绘图][代码][视频][音乐]     ║
║                                        [登录] [注册]      ║
╠══════════════════════════════════════════════════════════╣
║  [cyan 光晕渐变背景 - 顶部向下淡出]                       ║
║                                                          ║
║  发现 AI 邀请码，加速你的 AI 之旅                         ║
║  [Orbitron 字体, 36px, 居中]                             ║
║                                                          ║
╠══════════════════════════════════════════════════════════╣
║  [产品卡片网格 - 4列, gap:16px]                           ║
║  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      ║
║  │ [Logo 48px] │  │ [Logo 48px] │  │ [Logo 48px] │      ║
║  │ ChatGPT     │  │ Midjourney  │  │ GitHub CP   │      ║
║  │ 对话 AI     │  │ 绘图 AI     │  │ 代码 AI     │      ║
║  │ ─────────── │  │ ─────────── │  │ ─────────── │      ║
║  │ 简介截断... │  │ 简介截断... │  │ 简介截断... │      ║
║  │        [3码]│  │        [0码]│  │        [5码]│      ║
║  └─────────────┘  └─────────────┘  └─────────────┘      ║
║  卡片 bg:#111118, border:#1e2030                         ║
║  hover: border-color:#00d2ef40, translateY(-2px)         ║
╚══════════════════════════════════════════════════════════╝
```

### 10.2 产品详情页（核心页）

```
╔══════════════════════════════════════════════════════════╗
║  [产品信息区]                                            ║
║  [Logo 64px]  ChatGPT                    [去官网 ↗]     ║
║               对话 AI  |  当前可用邀请码: 3              ║
║               OpenAI 出品的对话式 AI 助手...             ║
╠══════════════════════════════════════════════════════════╣
║  邀请码接龙列表                                          ║
║  ┌──────────────────────────────────────────────────┐   ║
║  │ 👤 张三                              2小时前      │   ║
║  │    ab******                    [查看(-50积分)]    │   ║
║  ├──────────────────────────────────────────────────┤   ║
║  │   └─ 👤 李四  [接龙]               1小时前       │   ║  ← 缩进16px
║  │         cd******               [查看(-50积分)]   │   ║
║  ├──────────────────────────────────────────────────┤   ║
║  │ 👤 赵六                              3小时前      │   ║
║  │    gh******                    [查看(-50积分)]    │   ║
║  └──────────────────────────────────────────────────┘   ║
║                                                          ║
║  [悬浮按钮 - 右下角]  ＋ 贡献我的邀请码                  ║
╚══════════════════════════════════════════════════════════╝
```

### 10.3 颜色速查表

| 用途 | 颜色 | 预览 |
|------|------|------|
| 页面背景 | `#09090f` | 近黑 |
| 卡片/面板 | `#111118` | 深灰 |
| 弹窗背景 | `#1a1a24` | 稍亮深色 |
| 默认边框 | `#1e2030` | 暗蓝灰 |
| 主色 | `#00d2ef` | 亮 Cyan |
| 主色 hover | `#00b7d7` | 稍深 Cyan |
| 主文字 | `#f0f2f5` | 近白 |
| 次文字 | `#6a7282` | 中灰 |
| 成功 | `#00c758` | 亮绿 |
| 警告 | `#f99c00` | 琥珀 |
| 错误 | `#ff6568` | 亮红 |
