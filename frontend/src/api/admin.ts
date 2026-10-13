import api from './axios'
import type {
  CreateMasterCouponRequest,
  CreateMasterCouponResponse,
  AutoGenerateScheduleRequest,
  MovieOption,
} from '../types/admin'

/**
 * 마스터 쿠폰 생성
 * POST /api/v1/admin/coupons
 * 관리자 권한 필요
 */
export const createMasterCoupon = async (
  payload: CreateMasterCouponRequest,
): Promise<CreateMasterCouponResponse> => {
  const { data } = await api.post<CreateMasterCouponResponse>('/api/v1/admin/coupons', payload)
  return data
}

/**
 * 특정 유저에게 쿠폰 지급
 * POST /api/v1/admin/users/{userId}/coupons/{couponId}
 * 관리자 권한 필요
 */
export const grantCouponToUser = async (
  userId: number | string,
  couponId: number | string,
): Promise<void> => {
  await api.post(`/api/v1/admin/users/${userId}/coupons/${couponId}`)
}

/**
 * 박스오피스 기준 영화 DB 동기화
 * POST /api/v1/admin/movies/sync?targetDate=YYYYMMDD
 * 관리자 권한 필요
 *
 * 응답 바디 스펙이 없어 unknown으로 열어두고, 호출부에서 있으면 있는 대로
 * alert에 같이 보여주는 방식으로 처리한다.
 */
export const syncMoviesFromBoxOffice = async (targetDate: string): Promise<unknown> => {
  const { data } = await api.post('/api/v1/admin/movies/sync', undefined, {
    params: { targetDate },
  })
  return data
}

/**
 * 스케줄 자동 배정 (순차/교차 상영)
 * POST /api/v1/admin/schedules/auto-generate?movieIds=1,2,1,3&theaterId=3&targetDate=20231024
 * 관리자 권한 필요
 *
 * 쿼리 파라미터(RequestParam)로 보낸다. axios의 기본 params 직렬화를 쓰면
 * 콤마(,)가 %2C로 URL 인코딩되는데, 백엔드가 "1,2,1,3" 같은 순수 콤마
 * 구분 문자열을 기대하므로 쿼리스트링을 직접 만들어 붙인다.
 * (인코딩된 콤마도 서버가 디코딩해서 처리한다면 문제없지만, 명세의 예시가
 * 인코딩되지 않은 콤마라 안전하게 원문 그대로 보낸다.)
 */
export const autoGenerateSchedule = async (
  payload: AutoGenerateScheduleRequest,
): Promise<unknown> => {
  const query = [
    `movieIds=${payload.movieIds.join(',')}`,
    `theaterId=${payload.theaterId}`,
    `targetDate=${payload.targetDate}`,
  ].join('&')

  const { data } = await api.post(`/api/v1/admin/schedules/auto-generate?${query}`)
  return data
}

// 백엔드 원본 응답 모양이 명세와 다를 수 있어 느슨하게 받은 뒤 정규화한다.
interface RawMovieOption {
  id: number
  title?: string
  name?: string
  movieTitle?: string
  duration: number
}

/**
 * 스케줄 자동 배정 폼의 영화 드롭다운 전용 옵션 목록 조회 (duration 포함)
 * GET /api/v1/admin/movies/options
 * 관리자 권한 필요
 *
 * ⚠️ 명세상 제목 필드는 "title"이지만, 실제 응답이 "name"이나 "movieTitle"로
 * 내려오는 경우를 대비해 셋 다 받아 MovieOption.title로 정규화한다. (실제
 * 응답 확인 후 필드명이 확정되면 이 정규화는 걷어내도 된다.)
 */
export const getMovieOptions = async (): Promise<MovieOption[]> => {
  const { data } = await api.get<RawMovieOption[]>('/api/v1/admin/movies/options')
  return data.map((raw) => {
    const title = raw.title ?? raw.name ?? raw.movieTitle
    if (title === undefined) {
      // 개발 중 필드명 불일치를 바로 알아챌 수 있도록 콘솔에 남긴다.
      console.warn('[getMovieOptions] 응답에서 제목 필드를 찾지 못했습니다:', raw)
    }
    return { ...raw, title: title ?? '(제목 없음)' }
  })
}
