// ⚠️ GET /api/v1/reservations/me/tickets/upcoming 는 응답 바디 예시가 주어지지 않아,
// 화면에 필요한 항목(포스터/제목/상영관/시간/좌석/예약번호) 기준으로 구성한 추정 타입입니다.
// 실제 응답 필드명이 다르면(특히 reservationNumber, seatNumbers 부분은 다른 예매 관련
// 응답에서 reservationNo/seatNums로 쓰인 전례가 있어 불일치 가능성이 있습니다) 이 타입과
// api/ticket.ts만 맞추면 됩니다.
export interface UpcomingTicket {
  reservationNumber: string
  movieTitle: string
  posterUrl: string
  theaterName: string
  startTime: string // ISO 8601
  endTime: string // ISO 8601
  seatNumbers: string[]
}
