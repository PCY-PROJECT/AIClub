import request from '@/utils/request'
import type { ResourceItem, PageResult } from '@/types'

export function getResourceList(params: {
  category?: number
  difficulty?: number
  page?: number
  size?: number
}) {
  return request.get<any, PageResult<ResourceItem>>('/api/resources', { params })
}

export function getResourceDetail(id: number) {
  return request.get<any, ResourceItem>(`/api/resources/${id}`)
}

export function collectResource(id: number) {
  return request.post<any, { active: boolean; count: number }>(`/api/resources/${id}/collect`)
}

export function submitResource(data: {
  title: string
  summary?: string
  sourceUrl: string
  cover?: string
  author?: string
  category: number
  difficulty?: number
  tags?: string
}) {
  return request.post('/api/resources/submit', data)
}
