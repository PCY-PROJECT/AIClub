import request from '@/utils/request'

export const getTasks = () =>
  request.get<any>('/api/tasks')
