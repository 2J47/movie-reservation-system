export type CouponDiscountType = 'AMOUNT' | 'PERCENT'

// 발급 가능한 마스터 쿠폰 (이벤트 페이지에 노출되는 쿠폰)
export interface MasterCoupon {
  id: number
  name: string
  discountType: CouponDiscountType
  discountValue: number
  expiresAt: string // ISO 8601
}
