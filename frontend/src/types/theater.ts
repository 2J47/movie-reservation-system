// ⚠️ "상영관 목록 조회" API가 지금까지의 명세에 없어 추정한 타입입니다.
// 실제 백엔드 응답 필드명이 다르면 api/theater.ts와 이 타입만 맞추면 됩니다.
export interface Theater {
  id: number
  name: string
}
