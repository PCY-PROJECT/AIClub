import request from '@/utils/request'

/** 贡献邀请码（提交后进入待审核状态） */
export const contributeCode = (data: { productId: number; code: string }) =>
  request.post('/api/codes/contribute', data)

/** 花50积分从池中获取邀请码（返回 ClaimedCodeVO，含明文） */
export const claimCode = (productId: number) =>
  request.post<any>(`/api/codes/${productId}/claim`)

/** 确认邀请码有效性 result: "valid" | "invalid" */
export const confirmCode = (id: number, result: 'valid' | 'invalid') =>
  request.post(`/api/codes/${id}/confirm`, { result })

/** 我贡献的邀请码列表 */
export const getMyContributed = (page = 1, size = 20) =>
  request.get<any>('/api/codes/mine', { params: { page, size } })

/** 我获取的邀请码列表 */
export const getMyClaimed = (page = 1, size = 20) =>
  request.get<any>('/api/codes/claimed', { params: { page, size } })
