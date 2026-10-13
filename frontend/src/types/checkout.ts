import type { ShowtimeContext } from './showtime'

// 좌석 선택 페이지 → 결제 페이지로 넘어갈 때 함께 실어보내는 정보.
// movieTitle/theaterName/startTime/endTime은 상세 페이지에서 릴레이된 값이라
// 직접 URL로 진입한 경우 없을 수 있어 Partial로 열어둠.
export interface CheckoutState extends Partial<ShowtimeContext> {
  scheduleId: string
  seatIds: number[]
  seatNumbers: string[]
}
