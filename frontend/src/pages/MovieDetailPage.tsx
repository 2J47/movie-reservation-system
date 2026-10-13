import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getMovieDetail } from '../api/movie'
import { getSchedulesByMovie } from '../api/schedule'
import type { MovieDetail, MovieSchedule } from '../types/movie'
import { getDateTabs } from '../utils/date'
import { getRatingTextClass } from '../utils/rating'
import ScheduleCard from '../components/ScheduleCard'

const POSTER_FALLBACK =
  'data:image/svg+xml;charset=UTF-8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="300" height="450">
      <rect width="100%" height="100%" fill="#1A1A1A"/>
      <text x="50%" y="50%" fill="#9A9A9A" font-family="sans-serif" font-size="16"
        text-anchor="middle" dominant-baseline="middle">NO IMAGE</text>
    </svg>`,
  )

const dateTabs = getDateTabs()

const MovieDetailPage = () => {
  const { id } = useParams<{ id: string }>()

  const [movie, setMovie] = useState<MovieDetail | null>(null)
  const [schedules, setSchedules] = useState<MovieSchedule[]>([])
  const [isLoading, setIsLoading] = useState(true) // 영화 상세 정보 로딩 (최초 1회)
  const [isLoadingSchedules, setIsLoadingSchedules] = useState(true) // 날짜별 스케줄 로딩
  const [selectedDateKey, setSelectedDateKey] = useState<string>(dateTabs[0].key)

  // 영화 상세 정보는 id가 바뀔 때 한 번만 조회
  useEffect(() => {
    if (!id) return
    let isMounted = true

    const fetchMovie = async () => {
      setIsLoading(true)
      try {
        const movieData = await getMovieDetail(id)
        if (isMounted) setMovie(movieData)
      } finally {
        if (isMounted) setIsLoading(false)
      }
    }

    fetchMovie()
    return () => {
      isMounted = false
    }
  }, [id])

  // 상영 시간표는 date 파라미터가 필수이므로, id 또는 선택된 날짜가 바뀔 때마다 재조회
  useEffect(() => {
    if (!id) return
    let isMounted = true

    const fetchSchedules = async () => {
      setIsLoadingSchedules(true)
      try {
        const scheduleData = await getSchedulesByMovie(id, selectedDateKey)
        if (isMounted) setSchedules(scheduleData)
      } finally {
        if (isMounted) setIsLoadingSchedules(false)
      }
    }

    fetchSchedules()
    return () => {
      isMounted = false
    }
  }, [id, selectedDateKey])

  // 필터링은 이제 서버가 date 파라미터로 처리하므로, 프론트에서는 정렬만 담당
  const sortedSchedules = useMemo(() => {
    return [...schedules].sort(
      (a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime(),
    )
  }, [schedules])

  if (isLoading) {
    return (
      <div className="max-w-6xl mx-auto px-6 py-12">
        <div className="animate-pulse flex flex-col sm:flex-row gap-8">
          <div className="w-full sm:w-64 aspect-[2/3] rounded-xl bg-marquee-card border border-marquee-line shrink-0" />
          <div className="flex-1 flex flex-col gap-3 pt-2">
            <div className="h-8 w-2/3 rounded bg-marquee-card" />
            <div className="h-4 w-1/3 rounded bg-marquee-card" />
            <div className="h-24 w-full rounded bg-marquee-card mt-4" />
          </div>
        </div>
      </div>
    )
  }

  if (!movie) {
    return (
      <div className="max-w-6xl mx-auto px-6 py-24 text-center">
        <p className="font-body text-ink-muted">영화 정보를 찾을 수 없습니다.</p>
      </div>
    )
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      {/* 상세 정보 */}
      <div className="flex flex-col sm:flex-row gap-8">
        <div className="w-full sm:w-64 shrink-0">
          <img
            src={movie.posterUrl}
            alt={`${movie.title} 포스터`}
            onError={(e) => {
              e.currentTarget.onerror = null
              e.currentTarget.src = POSTER_FALLBACK
            }}
            className="w-full aspect-[2/3] object-cover rounded-xl border border-marquee-line"
          />
        </div>

        <div className="flex-1 pt-1">
          <span className={`inline-block font-mono text-xs border border-marquee-line rounded-md px-2 py-0.5 mb-3 ${getRatingTextClass(movie.rating)}`}>
            {movie.rating}
          </span>
          <h1 className="font-display text-4xl text-ink-light mb-2">
            {movie.title}
          </h1>
          <p className="font-mono text-sm text-ink-muted mb-6">{movie.duration}분</p>
          <p className="font-body text-sm text-ink-light/90 leading-relaxed max-w-2xl">
            {/* 백엔드 상세 API 명세에 줄거리 필드가 아직 확정되지 않아 optional로 받고 있습니다.
                필드가 내려오면 그대로 표시되고, 없으면 안내 문구로 대체됩니다. */}
            {movie.description ?? '줄거리 정보가 곧 제공될 예정입니다.'}
          </p>
        </div>
      </div>

      {/* 날짜 탭 + 시간표 */}
      <div className="mt-12">
        <p className="font-mono text-xs tracking-marquee text-reel-gold mb-2">SHOWTIMES</p>
        <h2 className="font-display text-2xl tracking-marquee text-ink-light mb-5">
          상영 시간표
        </h2>

        <div className="flex gap-2 mb-6">
          {dateTabs.map((tab) => {
            const isActive = tab.key === selectedDateKey
            return (
              <button
                key={tab.key}
                type="button"
                onClick={() => setSelectedDateKey(tab.key)}
                className={`font-body text-sm px-4 py-2 rounded-lg border transition-colors ${
                  isActive
                    ? 'bg-reel-gold text-marquee-bg border-reel-gold font-semibold'
                    : 'border-marquee-line text-ink-light hover:border-reel-gold'
                }`}
              >
                {tab.label}
              </button>
            )
          })}
        </div>

        {isLoadingSchedules ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div
                key={i}
                className="h-28 rounded-xl bg-marquee-card border border-marquee-line animate-pulse"
              />
            ))}
          </div>
        ) : sortedSchedules.length === 0 ? (
          <p className="font-body text-sm text-ink-muted py-10 text-center border border-dashed border-marquee-line rounded-xl">
            해당 날짜에 상영 예정인 시간표가 없습니다.
          </p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {sortedSchedules.map((schedule) => (
              <ScheduleCard key={schedule.scheduleId} schedule={schedule} movieTitle={movie.title} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default MovieDetailPage
