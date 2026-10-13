import api from './axios'
import type { UpcomingTicket } from '../types/ticket'

/**
 * 상영이 임박한 내 예매 목록 조회 (스마트 티켓)
 * GET /api/v1/reservations/me/tickets/upcoming
 * 로그인 필요
 *
 * 비로그인 상태에서는 호출부(컴포넌트)에서 아예 호출하지 않도록 막는다.
 * 실패해도 위젯 자체를 조용히 숨기면 되므로 전역 alert는 띄우지 않는다.
 */
export const getUpcomingTickets = async (): Promise<UpcomingTicket[]> => {
  const { data } = await api.get<UpcomingTicket[]>('/api/v1/reservations/me/tickets/upcoming', {
    skipGlobalErrorAlert: true,
  })
  return data
}
