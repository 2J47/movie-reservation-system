import api from './axios'
import type { MasterCoupon } from '../types/coupon'

// 백엔드 원본 응답 모양이 명세와 다를 수 있어 느슨하게 받은 뒤 정규화한다.
interface RawMasterCoupon {
  id?: number
  couponId?: number
  name: string
  discountType: MasterCoupon['discountType']
  discountValue: number
  expiresAt: string
}

/**
 * 발급 가능한 전체 마스터 쿠폰 목록 조회
 * GET /api/v1/coupons
 * 인증 불필요 (목록 조회 자체는 비로그인도 가능)
 *
 * ⚠️ 명세상 식별자 필드는 "id"이지만, 실제 응답이 "couponId"로 내려오는 경우를 대비해
 * 두 필드명을 모두 받아 MasterCoupon.id로 정규화한다. (실제 응답 확인 후 필드명이
 * 확정되면 이 정규화는 걷어내도 된다.)
 */
export const getAvailableCoupons = async (): Promise<MasterCoupon[]> => {
  const { data } = await api.get<RawMasterCoupon[]>('/api/v1/coupons')
  return data.map((raw) => {
    const id = raw.id ?? raw.couponId
    if (id === undefined) {
      // 개발 중 필드명 불일치를 바로 알아챌 수 있도록 콘솔에 남긴다.
      console.warn('[getAvailableCoupons] 쿠폰 응답에서 id/couponId를 찾지 못했습니다:', raw)
    }
    return { ...raw, id: id as number }
  })
}

/**
 * 쿠폰 직접 다운로드(발급)
 * POST /api/v1/users/me/assets/coupons/{couponId}
 * 로그인 필요
 *
 * 실패 시(예: 400 "이미 발급받은 쿠폰입니다.") 메시지는 axios.ts의 전역 인터셉터가
 * alert로 그대로 띄워주므로 이 함수에서 별도 처리하지 않는다.
 */
export const claimCoupon = async (couponId: number | string): Promise<void> => {
  await api.post(`/api/v1/users/me/assets/coupons/${couponId}`)
}
