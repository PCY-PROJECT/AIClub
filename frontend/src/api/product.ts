import request from '@/utils/request'

export const getProducts = (params?: { category?: number; group?: number }) =>
  request.get<any>('/api/products', { params: params ?? {} })

export const getProduct = (id: number | string) =>
  request.get<any>(`/api/products/${id}`)
