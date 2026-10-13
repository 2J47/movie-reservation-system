export type ReservationStatus = 'CONFIRMED' | 'CANCELED'

// 마이페이지 예매 내역 항목
export interface ReservationHistoryItem {
  reservationId: number
  reservationNo: string
  movieTitle: string
  theaterName: string
  startTime: string // ISO 8601
  seatNums: string[]
  totalPrice: number
  discountAmount: number
  finalAmount: number
  status: ReservationStatus
}
