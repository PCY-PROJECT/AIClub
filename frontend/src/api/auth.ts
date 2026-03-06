import request from '@/utils/request'

export const getCaptcha = () =>
  request.get<any>('/api/auth/captcha')

export const sendEmailCode = (data: { captchaId: string; captchaCode: string; email: string }) =>
  request.post('/api/auth/send-email-code', data)

export const register = (data: {
  captchaId: string; captchaCode: string
  email: string; emailCode: string
  password: string; inviteCode?: string
}) => request.post('/api/auth/register', data)

export const login = (data: { captchaId: string; captchaCode: string; email: string; password: string }) =>
  request.post<any>('/api/auth/login', data)
