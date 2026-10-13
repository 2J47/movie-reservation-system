export type CouponDiscountType = 'AMOUNT' | 'PERCENT'

// 마스터 쿠폰 생성 요청
export interface CreateMasterCouponRequest {
  name: string
  discountType: CouponDiscountType
  discountValue: number
  expireDays: number
}

// ⚠️ 마스터 쿠폰 생성 API의 응답 바디 스펙이 제공되지 않아 구조를 확정할 수 없습니다.
// couponId가 내려온다면 "쿠폰 지급" 폼에 자동으로 채워주고, 없어도 동작엔 지장이
// 없도록 optional + 인덱스 시그니처로 열어뒀습니다. 실제 응답 확인 후 필드를 맞춰주세요.
export interface CreateMasterCouponResponse {
  couponId?: number
  [key: string]: unknown
}

// 스케줄 자동 배정(순차/교차 상영) 요청
// - 쿼리 파라미터(RequestParam)로 보낸다. movieIds는 콤마로 조인한 문자열
//   (예: "1,2,1,3")로, targetDate는 하이픈을 제거한 YYYYMMDD로 보낸다.
export interface AutoGenerateScheduleRequest {
  movieIds: number[]
  theaterId: number
  targetDate: string // YYYYMMDD
}

// 스케줄 자동 배정 드롭다운 전용 영화 옵션
// GET /api/v1/admin/movies/options - duration(러닝타임, 분)으로 예상 종료 시간을 계산한다.
export interface MovieOption {
  id: number
  title: string
  duration: number
}
