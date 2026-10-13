import api from './axios'
import type { VerifyPaymentRequest, VerifyPaymentResponse } from '../types/payment'

/**
 * 포트원 결제 검증 및 확정
 * POST /api/v1/payments/verify
 * 로그인 필요
 */
export const verifyPayment = async (
  payload: VerifyPaymentRequest,
): Promise<VerifyPaymentResponse> => {
  const { data } = await api.post<VerifyPaymentResponse>('/api/v1/payments/verify', payload, {
    skipGlobalErrorAlert: true, // CheckoutPage가 자체 에러 모달로 안내함
  })
  return data
}
