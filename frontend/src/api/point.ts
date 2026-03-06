import request from '@/utils/request'

export const checkin = () =>
  request.post('/api/points/checkin')

export const getPointRecords = (page = 1, size = 20) =>
  request.get<any>('/api/points/records', { params: { page, size } })
