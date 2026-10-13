import api from './axios'
import type { UpdateProfileRequest, ChangePasswordRequest } from '../types/user'


/**
 * 내 프로필(이름/이메일/전화번호) + 잔여 포인트 조회
 * GET /api/v1/users/me
 * 로그인 필요 (Authorization 헤더는 axios 인터셉터가 자동으로 붙여줌)
 */
export const getMyProfile = async (): Promise<UserProfile> => {
  const { data } = await api.get<UserProfile>('/api/v1/users/me')
  return data
}

/**
 * 기본 정보(이름/전화번호) 수정
 * PUT /api/v1/users/me
 * 로그인 필요
 */
export const updateMyProfile = async (payload: UpdateProfileRequest): Promise<void> => {
  await api.put('/api/v1/users/me', payload)
}

/**
 * 비밀번호 변경
 * PUT /api/v1/users/me/password
 * 로그인 필요
 */
export const changeMyPassword = async (payload: ChangePasswordRequest): Promise<void> => {
  await api.put('/api/v1/users/me/password', payload)
}

/**
 * 회원 탈퇴
 * DELETE /api/v1/users/me
 * 로그인 필요
 */
export const deleteMyAccount = async (): Promise<void> => {
  await api.delete('/api/v1/users/me')
}
