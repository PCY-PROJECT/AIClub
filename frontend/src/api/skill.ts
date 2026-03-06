import request from '@/utils/request'
import type { SkillItem, PageResult } from '@/types'

export function getSkillList(params: { category?: string; page?: number; size?: number }) {
  return request.get<any, PageResult<SkillItem>>('/api/skills', { params })
}

export function getSkillDetail(id: number) {
  return request.get<any, SkillItem>(`/api/skills/${id}`)
}

export function likeSkill(id: number) {
  return request.post<any, { active: boolean; count: number }>(`/api/skills/${id}/like`)
}

export function collectSkill(id: number) {
  return request.post<any, { active: boolean; count: number }>(`/api/skills/${id}/collect`)
}

export function submitSkill(data: {
  title: string
  prompt: string
  applicable?: string
  category: string
  tags?: string
}) {
  return request.post('/api/skills', data)
}
