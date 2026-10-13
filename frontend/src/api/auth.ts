import api from './axios'
import type { LoginRequest, LoginResponse, SignupRequest } from '../types/auth'

/**
 * 회원가입 API
 * POST /api/v1/auth/signup
 * 성공 시 201 응답 (본문 없음 가정)
 */
export const signup = async (payload: SignupRequest): Promise<void> => {
  await api.post('/api/v1/auth/signup', payload)
}

/**
 * 로그인 API
 * POST /api/v1/auth/login
 * 성공 시 200 응답 + { accessToken: string }
 */
export const login = async (payload: LoginRequest): Promise<LoginResponse> => {
  const { data } = await api.post<LoginResponse>('/api/v1/auth/login', payload)
  return data
}
