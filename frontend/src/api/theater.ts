import api from './axios'
import type { Theater } from '../types/theater'

/**
 * 전체 상영관 목록 조회
 * GET /api/v1/theaters
 *
 * ⚠️ 이 엔드포인트는 지금까지 받은 명세에 없어 REST 관례상 가장 자연스러운
 * 경로로 추정해 둔 것입니다. 실제 경로/응답 필드가 다르면 이 함수만 고치면
 * 됩니다. (AdminPage의 스케줄 자동 배정 폼은 이 API가 실패해도 숫자 입력
 * 방식으로 자동 대체되도록 만들어 뒀습니다.)
 */
export const getTheaters = async (): Promise<Theater[]> => {
  const { data } = await api.get<Theater[]>('/api/v1/theaters', {
    skipGlobalErrorAlert: true, // 실패해도 조용히 폴백(숫자 입력)하기 위해 전역 alert는 끔
  })
  return data
}
