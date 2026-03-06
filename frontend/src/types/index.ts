export interface UserInfo {
  id: number
  nickname: string
  email: string
  points: number
  avatar?: string
  createTime: string
}

export interface Product {
  id: number
  name: string
  logo?: string
  description?: string
  officialUrl?: string
  category: number
  codeCount: number
}

export interface CodeItem {
  id: number
  productId: number
  productName?: string
  providerId: number
  providerNickname?: string
  codePreview: string
  /** 1=池中可用 2=已分配 3=已确认有效 4=已确认无效 5=待审核 6=审核拒绝 */
  status: number
  createTime: string
  claimTime?: string
  confirmDeadline?: string
  confirmResult?: number
}

export interface ClaimedCode {
  codeId: number
  productId: number
  productName?: string
  codeText: string
  /** 2=待确认 3=有效 4=无效 */
  status: number
  claimTime?: string
  confirmDeadline?: string
  confirmTime?: string
  confirmResult?: number
}

export interface PointRecord {
  id: number
  delta: number
  balance: number
  bizType: number
  remark: string
  createTime: string
}

export interface TaskItem {
  id: number
  name: string
  taskType: string
  frequency: number  // 1每日 2每周 3一次性
  pointsReward: number
  completed: boolean
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export const CategoryMap: Record<number, string> = {
  1: '对话 AI',
  2: '绘图 AI',
  3: '代码 AI',
  4: '视频 AI',
  5: '音乐 AI',
  9: '其他',
}

export const BizTypeMap: Record<number, string> = {
  1: '注册奖励',
  2: '每日签到',
  3: '查看邀请码（旧）',
  4: '邀请码被查看（旧）',
  5: '邀请码确认有效奖励',
  6: '邀请码判定无效扣分',
  7: '投稿资讯',
  8: '资讯获赞奖励',
  9: '技能审核通过',
  10: '获取邀请码',
  11: '邀请码无效退款',
  99: '管理员调整',
}

export interface SkillItem {
  id: number
  title: string
  prompt: string
  applicable?: string
  category: string
  tags?: string
  authorId: number
  authorNickname?: string
  likeCount: number
  collectCount: number
  useCount: number
  liked?: boolean
  collected?: boolean
  createTime: string
  status: number
}

export const SkillCategoryColor: Record<string, string> = {
  '写作': '#a78bfa',
  '编程': '#00d2ef',
  '设计': '#f99c00',
  '营销': '#ff6568',
  '分析': '#00c758',
  '翻译': '#60a5fa',
  '教育': '#fb923c',
  '职场': '#c084fc',
  '生活': '#34d399',
}

export interface NewsItem {
  id: number
  title: string
  summary?: string
  cover?: string
  sourceUrl?: string
  tags?: string
  category: number
  categoryName?: string
  authorId?: number
  authorNickname?: string
  likeCount: number
  viewCount: number
  liked?: boolean
  publishTime: string
  createTime: string
  status: number
}

export const NewsCategoryMap: Record<number, string> = {
  0: '全部',
  1: '产品发布',
  2: '行业动态',
  3: '技术突破',
  4: '使用技巧',
  5: '政策监管',
}

export const NewsCategoryColor: Record<number, string> = {
  1: '#00d2ef',
  2: '#f99c00',
  3: '#a78bfa',
  4: '#00c758',
  5: '#ff6568',
}

// 知识库
export interface ResourceItem {
  id: number
  title: string
  summary?: string
  sourceUrl: string
  cover?: string
  author?: string
  category: number
  categoryName?: string
  difficulty: number
  difficultyName?: string
  tags?: string
  viewCount: number
  collectCount: number
  submitUserId?: number
  submitUserNickname?: string
  collected?: boolean
  publishTime: string
  createTime: string
  status: number
}

export const ResourceCategoryMap: Record<number, string> = {
  0: '全部',
  1: '大模型',
  2: 'Agent框架',
  3: 'MCP',
  4: '教程',
  5: '工具评测',
  6: '行业洞察',
}

export const ResourceCategoryColor: Record<number, string> = {
  1: '#00d2ef',
  2: '#a78bfa',
  3: '#00c758',
  4: '#f99c00',
  5: '#60a5fa',
  6: '#fb923c',
}

export const ResourceDifficultyMap: Record<number, string> = {
  1: '入门',
  2: '进阶',
  3: '专业',
}

export const ResourceDifficultyColor: Record<number, string> = {
  1: '#00c758',
  2: '#f99c00',
  3: '#ff6568',
}

// Agent技能库 (MCP)
export interface McpSkillItem {
  id: number
  name: string
  description: string
  type: number           // 1 MCP Server  2 Agent Skill
  typeName?: string
  category?: string
  sourceUrl?: string
  installGuide?: string
  vendor?: string
  tags?: string
  status: number
  collectCount: number
  useCount: number
  sort: number
  createTime: string
}

export const McpTypeColor: Record<number, string> = {
  1: '#00d2ef',
  2: '#a78bfa',
}

// AI 网站导航
export interface SiteItem {
  id: number
  name: string
  description: string
  url: string
  logo?: string
  category?: string
  tags?: string
  status: number
  submitUserId?: number
  viewCount: number
  sort: number
  createTime: string
}

export const SiteCategoryList = ['工具', '研究', '社区', '教育', '资讯', '其他']

export const SiteCategoryColor: Record<string, string> = {
  '工具': '#00d2ef',
  '研究': '#a78bfa',
  '社区': '#00c758',
  '教育': '#f99c00',
  '资讯': '#fb923c',
  '其他': '#6a7282',
}
