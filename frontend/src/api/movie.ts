import api from './axios'
import type { Movie, MovieDetail } from '../types/movie'

/**
 * 영화 목록 조회
 * GET /api/v1/movies
 * 인증 불필요 (토큰이 없어도 정상 동작)
 */
export const getMovies = async (): Promise<Movie[]> => {
  const { data } = await api.get<Movie[]>('/api/v1/movies')
  return data
}

/**
 * 현재 상영작(스케줄이 존재하는 영화) 전체 목록 조회
 * GET /api/v1/movies/now-playing
 * 인증 불필요 - 메인 페이지 목록에서 사용
 */
export const getNowPlayingMovies = async (): Promise<Movie[]> => {
  const { data } = await api.get<Movie[]>('/api/v1/movies/now-playing')
  return data
}

/**
 * 영화 상세 조회
 * GET /api/v1/movies/{movieId}
 * 인증 불필요
 */
export const getMovieDetail = async (movieId: number | string): Promise<MovieDetail> => {
  const { data } = await api.get<MovieDetail>(`/api/v1/movies/${movieId}`)
  return data
}
