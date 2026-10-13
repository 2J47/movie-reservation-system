// 영화 목록/상세 공통 필드
export interface Movie {
  id: number
  title: string
  duration: number // 상영 시간(분)
  rating: string // 관람 등급 (예: "12세")
  posterUrl: string
}

// 영화 상세 응답
// 명세상 상세 API 응답에 줄거리(description) 필드가 없어 optional로 열어둠.
// 백엔드에 필드가 추가되면 자동으로 반영되고, 없으면 UI에서 안내 문구로 대체함.
export interface MovieDetail extends Movie {
  description?: string
}

// 상영 시간표
export interface MovieSchedule {
  scheduleId: number
  theaterName: string
  startTime: string // ISO 8601
  endTime: string // ISO 8601
  availableSeats: number
  totalSeats: number
}
