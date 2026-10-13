import { useNavigate } from 'react-router-dom'
import type { MovieSchedule } from '../types/movie'
import type { ShowtimeContext } from '../types/showtime'
import { formatTime } from '../utils/date'

interface ScheduleCardProps {
  schedule: MovieSchedule
  movieTitle: string
}

const ScheduleCard = ({ schedule, movieTitle }: ScheduleCardProps) => {
  const navigate = useNavigate()
  const { scheduleId, theaterName, startTime, endTime, availableSeats, totalSeats } = schedule

  const isSoldOut = availableSeats === 0
  const isLimited = !isSoldOut && availableSeats <= totalSeats * 0.2 // 잔여 20% 이하면 임박 강조

  const handleClick = () => {
    const showtimeContext: ShowtimeContext = { movieTitle, theaterName, startTime, endTime }
    navigate(`/schedules/${scheduleId}/seats`, { state: showtimeContext })
  }

  return (
    <button
      type="button"
      disabled={isSoldOut}
      onClick={handleClick}
      className="flex flex-col items-start gap-2 rounded-xl border border-marquee-line bg-marquee-card
        px-5 py-4 text-left transition-colors hover:border-reel-gold
        disabled:opacity-40 disabled:hover:border-marquee-line disabled:cursor-not-allowed"
    >
      <span className="font-mono text-xs text-reel-gold">{theaterName}</span>

      <span className="font-display text-2xl tracking-marquee text-ink-light">
        {formatTime(startTime)}
        <span className="mx-1.5 text-ink-muted text-base">~</span>
        {formatTime(endTime)}
      </span>

      <div className="ticket-divider w-full" />

      <span
        className={`font-mono text-xs ${
          isSoldOut ? 'text-curtain-red' : isLimited ? 'text-curtain-red' : 'text-ink-muted'
        }`}
      >
        {isSoldOut ? '매진' : `잔여 ${availableSeats} / ${totalSeats}석`}
      </span>
    </button>
  )
}

export default ScheduleCard
