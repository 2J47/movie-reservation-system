import api from './axios'
import type { UserAssets } from '../types/asset'

/**
 * 내 자산(지갑 포인트 잔액 + 보유 쿠폰 목록) 조회
 * GET /api/v1/users/me/assets
 * 로그인 필요
 */
export const getMyAssets = async (): Promise<UserAssets> => {
  const { data } = await api.get<UserAssets>('/api/v1/users/me/assets', {
    skipGlobalErrorAlert: true, // CheckoutPage가 실패 시 자체적으로 처리함
  })
  return data
}
