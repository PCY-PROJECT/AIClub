import request from '@/utils/request'
import type { ResourceItem, McpSkillItem, NewsItem, SkillItem, PageResult } from '@/types'

// ─── 知识库 ────────────────────────────────────────────────

export function adminListResources(params: { category?: number; status?: number; page?: number; size?: number }) {
  return request.get<any, PageResult<ResourceItem>>('/api/admin/resources', { params })
}
export function adminCreateResource(data: object) {
  return request.post('/api/admin/resources', data)
}
export function adminApproveResource(id: number) {
  return request.put(`/api/admin/resources/${id}/approve`)
}
export function adminRejectResource(id: number) {
  return request.put(`/api/admin/resources/${id}/reject`)
}
export function adminOfflineResource(id: number) {
  return request.put(`/api/admin/resources/${id}/offline`)
}

// ─── 提示词 ────────────────────────────────────────────────

export function adminListPrompts(params: { category?: string; status?: number; page?: number; size?: number }) {
  return request.get<any, PageResult<SkillItem>>('/api/admin/prompts', { params })
}
export function adminApprovePrompt(id: number) {
  return request.put(`/api/admin/prompts/${id}/approve`)
}
export function adminRejectPrompt(id: number) {
  return request.put(`/api/admin/prompts/${id}/reject`)
}

// ─── Agent技能库 ───────────────────────────────────────────

export function adminListMcp(params: { type?: number; category?: string; page?: number; size?: number }) {
  return request.get<any, PageResult<McpSkillItem>>('/api/admin/mcp', { params })
}
export function adminCreateMcp(data: object) {
  return request.post('/api/admin/mcp', data)
}
export function adminUpdateMcp(id: number, data: object) {
  return request.put(`/api/admin/mcp/${id}`, data)
}
export function adminUpdateMcpStatus(id: number, status: number) {
  return request.put(`/api/admin/mcp/${id}/status`, null, { params: { status } })
}

// ─── 资讯 ─────────────────────────────────────────────────

export function adminListNews(params: { category?: number; status?: number; page?: number; size?: number }) {
  return request.get<any, PageResult<NewsItem>>('/api/admin/news', { params })
}
export function adminApproveNews(id: number) {
  return request.put(`/api/admin/news/${id}/approve`)
}
export function adminRejectNews(id: number) {
  return request.put(`/api/admin/news/${id}/reject`)
}
export function adminOfflineNews(id: number) {
  return request.put(`/api/admin/news/${id}/offline`)
}

// ─── 用户 ─────────────────────────────────────────────────

export function adminListUsers(params: { page?: number; size?: number }) {
  return request.get<any, PageResult<any>>('/api/admin/users', { params })
}
export function adminAdjustPoints(userId: number, delta: number, remark?: string) {
  return request.post('/api/admin/users/points', { userId, delta, remark })
}
export function adminUpdateUserStatus(id: number, status: number) {
  return request.put(`/api/admin/users/${id}/status`, null, { params: { status } })
}

// ─── 产品管理 ──────────────────────────────────────────────

export function adminListProducts(params: { status?: number; page?: number; size?: number }) {
  return request.get<any, any>('/api/admin/products', { params })
}
export function adminCreateProduct(data: object) {
  return request.post('/api/admin/products', data)
}
export function adminUpdateProduct(id: number, data: object) {
  return request.put(`/api/admin/products/${id}`, data)
}
export function adminUpdateProductStatus(id: number, status: number) {
  return request.put(`/api/admin/products/${id}/status`, null, { params: { status } })
}

// ─── 邀请码 ───────────────────────────────────────────────

export function adminListCodes(params: { productId?: number; status?: number; page?: number; size?: number }) {
  return request.get<any, PageResult<any>>('/api/admin/codes', { params })
}
export function adminApproveCode(id: number) {
  return request.put(`/api/admin/codes/${id}/approve`)
}
export function adminRejectCode(id: number) {
  return request.put(`/api/admin/codes/${id}/reject`)
}
