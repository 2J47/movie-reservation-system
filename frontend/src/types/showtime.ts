// 영화 상세 페이지 → 좌석 선택 페이지 → 결제 페이지로 이어지는 동안
// 화면에 표시할 "영화 제목 / 상영관 / 시작~종료 시간" 컨텍스트.
//
// 백엔드 API 명세에 scheduleId 단건으로 영화/상영관/시간을 조회하는 엔드포인트가 없어서,
// 상세 페이지에서 이미 알고 있는 정보를 navigate state로 실어 다음 페이지들에 릴레이합니다.
// (나중에 "스케줄 단건 조회 API"가 추가되면, 각 페이지에서 scheduleId로 직접 조회하도록
//  바꾸는 편이 새로고침에도 안전합니다.)
export interface ShowtimeContext {
  movieTitle: string
  theaterName: string
  startTime: string
  endTime: string
}
