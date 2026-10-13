import { useEffect, useState } from 'react'
import { getUpcomingTickets } from '../api/ticket'
import type { UpcomingTicket } from '../types/ticket'
import { formatTicketDateParts } from '../utils/date'

const SmartTicketPage = () => {
  const [tickets, setTickets] = useState<UpcomingTicket[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [currentIndex, setCurrentIndex] = useState(0)

  useEffect(() => {
    let isMounted = true

    const fetchTickets = async () => {
      setIsLoading(true)
      try {
        const data = await getUpcomingTickets()
        if (isMounted) setTickets(data)
      } catch {
        if (isMounted) setTickets([])
      } finally {
        if (isMounted) setIsLoading(false)
      }
    }

    fetchTickets()
    return () => {
      isMounted = false
    }
  }, [])

  // 티켓 목록이 바뀌었는데 현재 인덱스가 범위를 벗어나면 되돌린다.
  useEffect(() => {
    if (currentIndex > 0 && currentIndex >= tickets.length) {
      setCurrentIndex(0)
    }
  }, [tickets, currentIndex])

  const hasMultiple = tickets.length > 1
  const goPrev = () => setCurrentIndex((i) => Math.max(i - 1, 0))
  const goNext = () => setCurrentIndex((i) => Math.min(i + 1, tickets.length - 1))

  return (
    <div className="max-w-3xl mx-auto px-6 py-12">
      <p className="font-mono text-xs tracking-widest text-reel-gold mb-2">SMART TICKET</p>
      <h1 className="font-display text-2xl tracking-marquee text-ink-light mb-5">스마트 티켓</h1>

      {isLoading ? (
        <div className="h-56 rounded-lg bg-marquee-card border border-marquee-line animate-pulse" />
      ) : tickets.length === 0 ? (
        <p className="font-mono text-sm text-ink-muted py-20 text-center border border-dashed border-marquee-line rounded-lg">
          상영 예정인 예매 티켓이 없습니다.
        </p>
      ) : (
        <>
          <div className="relative flex items-center gap-2">
            {hasMultiple && (
              <button
                type="button"
                onClick={goPrev}
                disabled={currentIndex === 0}
                aria-label="이전 티켓"
                className="shrink-0 w-8 h-8 rounded-full border border-marquee-line text-ink-light
                  hover:border-reel-gold transition-colors disabled:opacity-30 disabled:cursor-not-allowed
                  flex items-center justify-center font-mono"
              >
                ‹
              </button>
            )}

            <TicketAsciiCard ticket={tickets[currentIndex]} />

            {hasMultiple && (
              <button
                type="button"
                onClick={goNext}
                disabled={currentIndex === tickets.length - 1}
                aria-label="다음 티켓"
                className="shrink-0 w-8 h-8 rounded-full border border-marquee-line text-ink-light
                  hover:border-reel-gold transition-colors disabled:opacity-30 disabled:cursor-not-allowed
                  flex items-center justify-center font-mono"
              >
                ›
              </button>
            )}
          </div>

          {hasMultiple && (
            <div className="flex items-center justify-center gap-1.5 mt-4">
              {tickets.map((_, i) => (
                <span
                  key={i}
                  className={`h-1.5 rounded-full transition-all ${
                    i === currentIndex ? 'w-4 bg-reel-gold' : 'w-1.5 bg-marquee-line'
                  }`}
                />
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}

// 실제 종이 티켓처럼: 헤더(작품 정보) → 포스터(컬러 유지) → 절취선 → 상영관/좌석/날짜/시간
// 그리드 → 바코드 순으로 구성한다.
const TicketAsciiCard = ({ ticket }: { ticket: UpcomingTicket }) => {
  const { date, time } = formatTicketDateParts(ticket.startTime, ticket.endTime)

  return (
    <div className="relative flex-1 overflow-hidden rounded-2xl bg-marquee-card border border-marquee-line shadow-2xl shadow-black/50">
      <div className="flex min-h-[340px]">
        {/* 왼쪽: 포스터 + 영화 정보 */}
        <div className="w-2/5 shrink-0">
          <img
            src={ticket.posterUrl}
            alt={`${ticket.movieTitle} 포스터`}
            className="w-full h-full object-cover"
          />
        </div>

        {/* 오른쪽: 티켓 정보 */}
        <div className="flex flex-1 flex-col">
          {/* 영화 제목 */}
          <div className="px-6 pt-6 pb-4">
            <p className="font-mono text-[10px] tracking-[0.3em] text-ink-muted mb-1">
              MOVIE
            </p>
            <h3 className="font-display text-2xl tracking-wide text-ink-light leading-tight">
              {ticket.movieTitle}
            </h3>
          </div>

          {/* 점선 */}
          <div className="border-t border-dashed border-marquee-line" />

          {/* 상영 정보 */}
          <div className="grid grid-cols-2 gap-x-6 gap-y-5 px-6 py-5">
            <div>
              <p className="font-mono text-[10px] tracking-widest text-ink-muted mb-1">
                SCREEN
              </p>
              <p className="font-mono text-base font-bold text-ink-light truncate">
                {ticket.theaterName}
              </p>
            </div>

            <div>
              <p className="font-mono text-[10px] tracking-widest text-ink-muted mb-1">
                SEAT
              </p>
              <p className="font-mono text-base font-bold text-reel-gold truncate">
                {ticket.seatNumbers.join(', ')}
              </p>
            </div>

            <div>
              <p className="font-mono text-[10px] tracking-widest text-ink-muted mb-1">
                DATE
              </p>
              <p className="font-mono text-base font-bold text-ink-light">
                {date}
              </p>
            </div>

            <div>
              <p className="font-mono text-[10px] tracking-widest text-ink-muted mb-1">
                TIME
              </p>
              <p className="font-mono text-base font-bold text-ink-light">
                {time}
              </p>
            </div>
          </div>

          {/* 예매 번호 */}
          <div className="mt-auto border-t border-dashed border-marquee-line px-6 py-4">
            <p className="font-mono text-[11px] tracking-[0.3em] text-ink-muted">
              {ticket.reservationNumber}
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}


export default SmartTicketPage
