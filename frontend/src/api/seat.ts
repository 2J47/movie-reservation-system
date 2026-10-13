import api from './axios'
import type { Seat } from '../types/seat'

/**
 * 특정 스케줄의 좌석 정보 조회
 * GET /api/v1/schedules/{scheduleId}/seats
 * 로그인 필요 (Authorization 헤더는 axios 인터셉터가 자동으로 붙여줌)
 */
export const getSeatsBySchedule = async (scheduleId: number | string): Promise<Seat[]> => {
  const { data } = await api.get<Seat[]>(`/api/v1/schedules/${scheduleId}/seats`)
  return data
}
