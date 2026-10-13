export type PayMethod = 'CARD'

export interface VerifyPaymentRequest {
  reservationId: number
  paymentId: string
  userCouponId: number | null
  usePointAmount: number
  payMethod: PayMethod
}

export interface VerifyPaymentResponse {
  paymentId: number
  reservationNo: string
  finalAmount: number
  status: string
}
