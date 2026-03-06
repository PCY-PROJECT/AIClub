import request from '@/utils/request'
import type { NewsItem, PageResult } from '@/types'

export function getNewsList(params: { category?: number; page?: number; size?: number }) {
  return request.get<any, PageResult<NewsItem>>('/api/news', { params })
}

export function getNewsDetail(id: number) {
  return request.get<any, NewsItem>(`/api/news/${id}`)
}

export function likeNews(id: number) {
  return request.post<any, { liked: boolean; likeCount: number }>(`/api/news/${id}/like`)
}

export function submitNews(data: {
  title: string
  summary?: string
  sourceUrl?: string
  cover?: string
  tags?: string
  category: number
}) {
  return request.post('/api/news/submit', data)
}

export function createNews(data: {
  title: string
  summary?: string
  sourceUrl?: string
  cover?: string
  tags?: string
  category: number
}) {
  return request.post('/api/news', data)
}
