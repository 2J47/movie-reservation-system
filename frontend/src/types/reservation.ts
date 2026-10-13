export interface CreateReservationRequest {
  scheduleId: number | string
  seatIds: number[]
}

export interface CreateReservationResponse {
  reservationId: number
  reservationNo: string
  totalPrice: number
  status: string
}
