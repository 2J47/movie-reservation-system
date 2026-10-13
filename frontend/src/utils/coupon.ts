import type { CouponDiscountType } from '../types/coupon'

interface DiscountLike {
  discountType: CouponDiscountType
  discountValue: number
}

// "3,000원 할인" / "10% 할인" 형태로 사람이 읽을 문구를 만든다.
export const formatDiscountLabel = (coupon: DiscountLike): string =>
  coupon.discountType === 'PERCENT'
    ? `${coupon.discountValue}% 할인`
    : `${coupon.discountValue.toLocaleString('ko-KR')}원 할인`

// 실제 할인 "금액"(원)을 계산한다. PERCENT면 기준 금액에 비율을 곱해서 원 단위로 환산하고,
// AMOUNT면 그 값을 그대로 쓴다. 어느 쪽이든 기준 금액을 넘지 않도록 상한을 둔다.
export const calculateDiscountAmount = (coupon: DiscountLike, baseAmount: number): number => {
  const rawDiscount =
    coupon.discountType === 'PERCENT'
      ? Math.round((baseAmount * coupon.discountValue) / 100)
      : coupon.discountValue
  return Math.min(Math.max(rawDiscount, 0), baseAmount)
}
