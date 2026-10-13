import type { CouponDiscountType } from './coupon'

// 보유 쿠폰
// ⚠️ 명세 예시상 name은 "string"(placeholder)인 반면 discountType에 실제 표시용 문구
// ("오픈 기념 3000원 할인")가 들어있다고 봤었으나, 관리자 쿠폰 생성 기능(Phase 6)을
// 보면 실제로는 마스터 쿠폰과 동일하게 'AMOUNT' | 'PERCENT' 값이 내려오는 것으로
// 확인되어 타입을 그에 맞게 정정했다.
export interface Coupon {
  userCouponId: number
  name: string
  discountType: CouponDiscountType
  discountValue: number
  expiresAt: string // ISO 8601
}

// 내 자산(지갑 포인트 + 쿠폰 목록)
export interface UserAssets {
  balance: number
  coupons: Coupon[]
}
