import request from '@/utils/request'
import type { McpSkillItem, PageResult } from '@/types'

export function getMcpList(params: {
  type?: number
  category?: string
  page?: number
  size?: number
}) {
  return request.get<any, PageResult<McpSkillItem>>('/api/mcp', { params })
}

export function getMcpDetail(id: number) {
  return request.get<any, McpSkillItem>(`/api/mcp/${id}`)
}

export function recordMcpUse(id: number) {
  return request.post(`/api/mcp/${id}/use`)
}
