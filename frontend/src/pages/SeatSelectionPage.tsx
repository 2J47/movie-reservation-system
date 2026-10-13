import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { getSeatsBySchedule } from '../api/seat'
import type { Seat } from '../types/seat'
import type { ShowtimeContext } from '../types/showtime'
import type { CheckoutState } from '../types/checkout'
import { formatTime } from '../utils/date'
import { SEAT_PRICE } from '../constants/pricing'
import { resolveTheaterLayoutPreset, getAisleAfterColumns } from '../utils/theaterLayout'

const SeatSelectionPage = () => {
  const { scheduleId } = useParams<{ scheduleId: string }>()
  const navigate = useNavigate()
  const location = useLocation()
  // 상세 페이지에서 릴레이된 영화/상영관/시간 정보 (직접 URL 진입 시 없을 수 있음)
  const showtimeContext = location.state as ShowtimeContext | null

  const [seats, setSeats] = useState<Seat[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [selectedSeatIds, setSelectedSeatIds] = useState<number[]>([])

  useEffect(() => {
    if (!scheduleId) return
    let isMounted = true

    const fetchSeats = async () => {
      setIsLoading(true)
      try {
        const seatData = await getSeatsBySchedule(scheduleId)
        if (isMounted) setSeats(seatData)
      } finally {
        if (isMounted) setIsLoading(false)
      }
    }

    fetchSeats()
    return () => {
      isMounted = false
    }
  }, [scheduleId])

  // seatRow 기준으로 그룹핑하고, 각 행 내부는 seatColumn 순으로 정렬
  const seatsByRow = useMemo(() => {
    const grouped: Record<string, Seat[]> = {}
    for (const seat of seats) {
      if (!grouped[seat.seatRow]) grouped[seat.seatRow] = []
      grouped[seat.seatRow].push(seat)
    }
    for (const row of Object.keys(grouped)) {
      grouped[row].sort((a, b) => a.seatColumn - b.seatColumn)
    }
    return grouped
  }, [seats])

  const rowKeys = useMemo(() => Object.keys(seatsByRow).sort(), [seatsByRow])

  // --- 아래는 순수 시각 표시(통로/좌석 크기)용 파생값. 선택/결제 로직에는 관여하지 않는다. ---
  const layoutPreset = useMemo(
    () => resolveTheaterLayoutPreset(showtimeContext?.theaterName),
    [showtimeContext?.theaterName],
  )
  const maxSeatColumn = useMemo(
    () => seats.reduce((max, seat) => Math.max(max, seat.seatColumn), 0),
    [seats],
  )
  const aisleAfterColumns = useMemo(
    () => getAisleAfterColumns(maxSeatColumn, layoutPreset.aisleRatios),
    [maxSeatColumn, layoutPreset],
  )

  const toggleSeat = (seat: Seat) => {
    if (!seat.isAvailable) return
    setSelectedSeatIds((prev) =>
      prev.includes(seat.seatId)
        ? prev.filter((id) => id !== seat.seatId)
        : [...prev, seat.seatId],
    )
  }

  // 하단 바에 표시할 선택된 좌석들 (좌석번호 순으로 정렬해서 보여주기)
  const selectedSeats = useMemo(() => {
    return seats
      .filter((seat) => selectedSeatIds.includes(seat.seatId))
      .sort((a, b) => a.seatRow.localeCompare(b.seatRow) || a.seatColumn - b.seatColumn)
  }, [seats, selectedSeatIds])

  const totalPrice = selectedSeats.length * SEAT_PRICE

  const handleCheckout = () => {
    if (selectedSeats.length === 0 || !scheduleId) return
    const checkoutState: CheckoutState = {
      scheduleId,
      seatIds: selectedSeats.map((seat) => seat.seatId),
      seatNumbers: selectedSeats.map((seat) => seat.seatNumber),
      ...(showtimeContext ?? {}),
    }
    navigate('/checkout', { state: checkoutState })
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-12 pb-40">
      {/* 영화/상영관/시간 정보 (상세 페이지에서 릴레이됨) */}
      {showtimeContext && (
        <div className="text-center mb-8">
          <h1 className="font-display text-2xl tracking-marquee text-ink-light mb-1">
            {showtimeContext.movieTitle}
          </h1>
          <p className="font-mono text-xs text-ink-muted">
            {showtimeContext.theaterName} · {formatTime(showtimeContext.startTime)} ~{' '}
            {formatTime(showtimeContext.endTime)}
          </p>
        </div>
      )}

      {/* 스크린 + 범례 */}
      <div className="relative mb-12">
        <div className="flex flex-col items-center gap-2">
          <div className="w-full max-w-xl h-2.5 rounded-full bg-gradient-to-b from-ink-muted/50 to-ink-muted/5 shadow-[0_8px_24px_-4px_rgba(255,255,255,0.15)]" />
          <span className="font-mono text-[11px] tracking-[0.4em] text-ink-muted">SCREEN</span>
        </div>

        <div className="absolute top-0 right-0 flex flex-col gap-1.5">
          <LegendItem swatchClassName="border border-marquee-line bg-marquee-card" label="예매 가능" />
          <LegendItem swatchClassName="bg-ink-light border border-ink-light" label="선택한 좌석" />
          <LegendItem
            swatchClassName="bg-marquee-line/70 border border-marquee-line"
            label="예매 완료"
          />
        </div>
      </div>

      {/* 좌석 그리드 */}
      {isLoading ? (
        <div className="flex flex-col items-center gap-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-9 w-72 rounded-md bg-marquee-card animate-pulse" />
          ))}
        </div>
      ) : seats.length === 0 ? (
        <p className="font-body text-sm text-ink-muted py-16 text-center">
          좌석 정보를 불러올 수 없습니다.
        </p>
      ) : (
        <div className="overflow-x-auto pb-2 -mx-6 px-6">
          <div className="flex flex-col gap-2 w-fit mx-auto min-w-full sm:min-w-0">
            {rowKeys.map((row) => (
              <div key={row} className="flex items-center gap-2">
                <span className="w-5 shrink-0 text-center font-mono text-xs text-ink-muted">
                  {row}
                </span>
                <div className="flex gap-2">
                  {seatsByRow[row].map((seat) => {
                    const isSelected = selectedSeatIds.includes(seat.seatId)
                    return (
                      <div key={seat.seatId} className="flex items-center gap-2">
                        <button
                          type="button"
                          disabled={!seat.isAvailable}
                          onClick={() => toggleSeat(seat)}
                          aria-label={seat.seatNumber}
                          aria-pressed={isSelected}
                          className={`${layoutPreset.seatSizeClass} shrink-0 rounded-md font-mono flex items-center justify-center
                            transition-colors border ${
                              !seat.isAvailable
                                ? 'bg-marquee-line/70 border-marquee-line text-ink-muted/40 cursor-not-allowed'
                                : isSelected
                                  ? 'bg-ink-light border-ink-light text-marquee-bg'
                                  : 'bg-marquee-card border-marquee-line text-ink-light hover:border-reel-gold'
                            }`}
                        >
                          {seat.seatColumn}
                        </button>
                        {/* 통로(빈 공간) - 실제 좌석 데이터와 무관한 순수 시각 요소 */}
                        {aisleAfterColumns.has(seat.seatColumn) && (
                          <div className={`${layoutPreset.aisleWidthClass} shrink-0`} aria-hidden />
                        )}
                      </div>
                    )
                  })}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 하단 고정 결제 바 */}
      <div className="fixed bottom-0 left-0 right-0 z-40 bg-marquee-card border-t border-marquee-line">
        <div className="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between gap-4">
          <div className="min-w-0">
            <p className="font-mono text-xs text-ink-muted mb-1">
              {selectedSeats.length === 0
                ? '좌석을 선택해 주세요'
                : `선택 좌석 ${selectedSeats.length}개 · ${selectedSeats
                    .map((seat) => seat.seatNumber)
                    .join(', ')}`}
            </p>
            <p className="font-display text-2xl tracking-marquee text-reel-gold">
              {totalPrice.toLocaleString('ko-KR')}원
            </p>
          </div>

          <button
            type="button"
            disabled={selectedSeats.length === 0}
            onClick={handleCheckout}
            className="shrink-0 bg-reel-gold hover:bg-reel-goldDark text-marquee-bg font-body font-semibold
              rounded-lg px-8 py-3 transition-colors disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-reel-gold"
          >
            결제하기
          </button>
        </div>
      </div>
    </div>
  )
}

const LegendItem = ({
  swatchClassName,
  label,
}: {
  swatchClassName: string
  label: string
}) => (
  <div className="flex items-center gap-2">
    <span className={`w-3.5 h-3.5 rounded-sm ${swatchClassName}`} />
    <span className="font-body text-xs text-ink-muted">{label}</span>
  </div>
)

export default SeatSelectionPage
