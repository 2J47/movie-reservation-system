import api from './axios'
import type { CreateReservationRequest, CreateReservationResponse } from '../types/reservation'
import type { ReservationHistoryItem } from '../types/reservationHistory'

/**
 * 예매 대기(Pending) 생성 - 좌석 락
 * POST /api/v1/reservations
 * 로그인 필요
 */
export const createReservation = async (
  payload: CreateReservationRequest,
): Promise<CreateReservationResponse> => {
  const { data } = await api.post<CreateReservationResponse>('/api/v1/reservations', payload, {
    skipGlobalErrorAlert: true, // CheckoutPage가 자체 에러 모달로 안내함
  })
  return data
}

/**
 * 예매 취소(롤백/환불) - 결제 실패 시 락 해제 목적으로도 사용, 마이페이지 취소 버튼에서도 재사용
 * POST /api/v1/reservations/{reservationNo}/cancel
 * 로그인 필요
 */
export const cancelReservation = async (reservationNo: string): Promise<void> => {
  await api.post(`/api/v1/reservations/${reservationNo}/cancel`, undefined, {
    skipGlobalErrorAlert: true, // 호출부(CheckoutPage 롤백 / MyPage)가 각자 알맞게 에러를 안내함
  })
}

/**
 * 내 예매 내역 목록 조회
 * GET /api/v1/users/me/reservations
 * 로그인 필요
 */
export const getMyReservations = async (): Promise<ReservationHistoryItem[]> => {
  const { data } = await api.get<ReservationHistoryItem[]>('/api/v1/users/me/reservations')
  return data
}
