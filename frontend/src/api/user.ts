import request from '@/utils/request'

export const getMe = () =>
  request.get<any>('/api/user/me')
