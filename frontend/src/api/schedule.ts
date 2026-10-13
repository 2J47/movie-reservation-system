import api from './axios'
import type { MovieSchedule } from '../types/movie'

/**
 * 특정 영화의 상영 시간표 조회
 * GET /api/v1/movies/{movieId}/schedules
 * 인증 불필요
 */
export const getSchedulesByMovie = async (
  movieId: number | string,
  date: string,
): Promise<MovieSchedule[]> => {
  const { data } = await api.get<MovieSchedule[]>(`/api/v1/movies/${movieId}/schedules`, {
    params: { date },
  })
  return data
}
