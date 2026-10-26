import api from './axios'
import type { UpcomingTicket } from '../types/ticket'

export const getUpcomingTickets = async (): Promise<UpcomingTicket[]> => {
  const { data } = await api.get<UpcomingTicket[]>('/api/v1/reservations/me/upcoming', {
    skipGlobalErrorAlert: true,
  })
  return data
}
