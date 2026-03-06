import request from '@/utils/request'
import type { SiteItem } from '@/types'

export const getSiteList = (category?: string) =>
  request.get<any, SiteItem[]>('/api/sites', { params: category ? { category } : {} })

export const submitSite = (data: {
  name: string
  description: string
  url: string
  logo?: string
  category?: string
  tags?: string
}) => request.post('/api/sites/submit', data)

export const recordSiteView = (id: number) =>
  request.post(`/api/sites/${id}/view`)

// admin
export const adminListSites = (params: { status?: number; page?: number; size?: number }) =>
  request.get('/api/admin/sites', { params })

export const adminApproveSite = (id: number) =>
  request.post(`/api/admin/sites/${id}/approve`)

export const adminRejectSite = (id: number) =>
  request.post(`/api/admin/sites/${id}/reject`)

export const adminDeleteSite = (id: number) =>
  request.delete(`/api/admin/sites/${id}`)
