export interface UpcomingTicket {
  reservationNumber: string
  movieTitle: string
  posterUrl: string
  theaterName: string
  startTime: string // ISO 8601
  endTime: string // ISO 8601
  seatNumbers: string[]
}
