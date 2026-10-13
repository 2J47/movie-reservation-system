// 좌석 정보
export interface Seat {
  seatId: number
  seatNumber: string // 예: "A1"
  seatRow: string // 예: "A"
  seatColumn: number // 예: 1
  isAvailable: boolean
}
