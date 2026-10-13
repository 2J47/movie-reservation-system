import { useEffect, useMemo, useState, type FormEvent } from 'react'
import Select from 'react-select'
import { syncMoviesFromBoxOffice, autoGenerateSchedule, getMovieOptions } from '../api/admin'
import { getTheaters } from '../api/theater'
import type { MovieOption } from '../types/admin'
import type { Theater } from '../types/theater'
import {
  buildScheduleSlots,
  formatMinutesAsTime,
  MIDNIGHT_LIMIT_MINUTES,
} from '../utils/schedulePlanner'
import DatePicker from './DatePicker'

// 응답 바디 스펙이 없는 API들을 위한 best-effort 메시지 추출 (message 필드가 있으면 우선 사용)
const describeResult = (result: unknown, fallback: string): string => {
  if (result && typeof result === 'object') {
    const maybeMessage = (result as Record<string, unknown>).message
    if (typeof maybeMessage === 'string' && maybeMessage.trim()) return maybeMessage
  }
  return fallback
}

const today = () => new Date().toISOString().slice(0, 10) // YYYY-MM-DD

const getYesterday = () => {
  const date = new Date();
  date.setDate(date.getDate() - 1);
  return date.toISOString().split('T')[0]; // ex: "2026-09-08"
}

const AdminMovieSchedule = () => {
  // 영화 DB 동기화 폼 상태
  const [syncDate, setSyncDate] = useState<string>(getYesterday())
  const [isSyncing, setIsSyncing] = useState(false)

  // 영화/상영관 선택지 (드롭다운용)
  const [movieOptions, setMovieOptions] = useState<MovieOption[]>([])
  const [theaters, setTheaters] = useState<Theater[]>([])
  const [isLoadingOptions, setIsLoadingOptions] = useState(true)
  const [theaterListUnavailable, setTheaterListUnavailable] = useState(false)

  // 스케줄 자동 배정(순차/교차 상영) 폼 상태
  const [theaterId, setTheaterId] = useState('')
  const [scheduleDate, setScheduleDate] = useState(today())
  // 0 = 아직 영화를 고르지 않은 빈 슬롯. 제출 시 0보다 큰 값만 걸러서 보낸다.
  const [movieIds, setMovieIds] = useState<number[]>([0])
  const [isGenerating, setIsGenerating] = useState(false)

  useEffect(() => {
    let isMounted = true

    const fetchOptions = async () => {
      setIsLoadingOptions(true)
      try {
        const options = await getMovieOptions()
        if (isMounted) setMovieOptions(options)
      } catch {
        // 영화 옵션 조회 실패 시에도 패널 자체는 계속 쓸 수 있어야 하므로 무시한다.
      }

      try {
        const theaterList = await getTheaters()
        if (isMounted) setTheaters(theaterList)
      } catch {
        if (isMounted) setTheaterListUnavailable(true)
      }

      if (isMounted) setIsLoadingOptions(false)
    }

    fetchOptions()
    return () => {
      isMounted = false
    }
  }, [])

  // 현재 선택된 영화들(빈 슬롯 제외)만 순서대로 뽑아 예상 시간표를 계산한다.
  const selectedSlots = useMemo(() => {
    const selectedMovies = movieIds
      .filter((id) => id > 0)
      .map((id) => movieOptions.find((movie) => movie.id === id))
      .filter((movie): movie is MovieOption => !!movie)

    return buildScheduleSlots(selectedMovies)
  }, [movieIds, movieOptions])

  const lastSlot = selectedSlots[selectedSlots.length - 1] ?? null

  const isGenerateDisabled =
    isGenerating || selectedSlots.length === 0 || !theaterId || !scheduleDate

  const formattedMovieOptions = useMemo(() => {
      return movieOptions.map((movie) => ({
        value: movie.id,
        label: `${movie.title} (${movie.duration}분)`,
      }))
    }, [movieOptions])

  const handleAddMovieRow = () => {
    setMovieIds((prev) => [...prev, 0])
  }

  const handleRemoveMovieRow = (index: number) => {
    setMovieIds((prev) => prev.filter((_, i) => i !== index))
  }

  const handleChangeMovieAt = (index: number, value: string) => {
    const nextMovieId = Number(value)

    // 빈 슬롯으로 되돌리는 경우(선택 해제)는 자정 검증 없이 그대로 반영한다.
    if (nextMovieId === 0) {
      setMovieIds((prev) => prev.map((id, i) => (i === index ? 0 : id)))
      return
    }

    // 이 선택을 반영했을 때 전체 시퀀스가 어떻게 되는지 미리 계산해서
    // 마지막 영화의 종료 시각이 자정(23:59)을 넘는지 먼저 확인한다.
    const candidateMovieIds = movieIds.map((id, i) => (i === index ? nextMovieId : id))
    const candidateMovies = candidateMovieIds
      .filter((id) => id > 0)
      .map((id) => movieOptions.find((movie) => movie.id === id))
      .filter((movie): movie is MovieOption => !!movie)
    const candidateSlots = buildScheduleSlots(candidateMovies)
    const candidateLastSlot = candidateSlots[candidateSlots.length - 1]

    if (candidateLastSlot && candidateLastSlot.endMinutes > MIDNIGHT_LIMIT_MINUTES) {
      alert('해당 영화를 추가하면 자정을 초과하여 배정할 수 없습니다.')
      return // 상태를 바꾸지 않으므로 select는 이전 선택값으로 그대로 남는다.
    }

    setMovieIds(candidateMovieIds)
  }

  const handleSync = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!syncDate) return

    setIsSyncing(true)
    try {
      // 쿼리 파라미터는 명세대로 YYYYMMDD(하이픈 없는) 형태로 변환해서 보낸다.
      const targetDate = syncDate.replace(/-/g, '')
      const result = await syncMoviesFromBoxOffice(targetDate)
      alert(describeResult(result, '박스오피스 동기화가 완료되었습니다.'))
    } catch {
      // 403(권한 없음) 등 에러 메시지는 api/axios.ts의 전역 인터셉터가 alert로 이미 띄워준다.
    } finally {
      setIsSyncing(false)
    }
  }

  const handleGenerate = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (isGenerateDisabled) return

    setIsGenerating(true)
    try {
      const result = await autoGenerateSchedule({
        movieIds: selectedSlots.map((slot) => slot.movieId),
        theaterId: Number(theaterId),
        targetDate: scheduleDate.replace(/-/g, ''), // YYYYMMDD - 하이픈 제거 필수
      })
      alert(describeResult(result, '스케줄이 자동으로 배정되었습니다.'))
    } catch {
      // 403(권한 없음) 등 에러 메시지는 api/axios.ts의 전역 인터셉터가 alert로 이미 띄워준다.
    } finally {
      setIsGenerating(false)
    }
  }

  return (
    <div className="flex flex-col gap-8">
      {/* 영화 DB 동기화 섹션 */}
      <section className="rounded-xl border border-marquee-line bg-[#111111] px-8 py-8">
        <p className="font-mono text-xs tracking-marquee text-reel-gold mb-1">BOX OFFICE SYNC</p>
        <h2 className="font-display text-xl tracking-marquee text-ink-light mb-6">영화 DB 동기화</h2>

        <form onSubmit={handleSync} className="flex flex-col sm:flex-row sm:items-end gap-4">
          <div className="flex-1 sm:max-w-xs">
            <label htmlFor="sync-date" className="block font-mono text-xs text-ink-muted mb-1.5">
              기준 일자
            </label>
            <DatePicker id="sync-date" value={syncDate} onChange={setSyncDate} />
          </div>

          <button
            type="submit"
            disabled={isSyncing}
            className="btn-primary sm:w-auto sm:px-8 flex items-center justify-center gap-2"
          >
            {isSyncing && (
              <span
                className="h-3.5 w-3.5 rounded-full border-2 border-marquee-bg/40 border-t-marquee-bg animate-spin"
                aria-hidden
              />
            )}
            {isSyncing ? '동기화 중...' : '박스오피스 동기화'}
          </button>
        </form>
      </section>

      {/* 스케줄 자동 배정(순차/교차 상영) 섹션 */}
      <section className="rounded-xl border border-marquee-line bg-[#111111] px-8 py-8">
        <p className="font-mono text-xs tracking-marquee text-reel-gold mb-1">AUTO SCHEDULE</p>
        <h2 className="font-display text-xl tracking-marquee text-ink-light mb-6">스케줄 배정</h2>

        <form onSubmit={handleGenerate} className="flex flex-col gap-6">
          {/* 상영관 + 날짜: 상단에 나란히 */}
          <div className="grid sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="schedule-theater-id" className="block font-mono text-xs text-ink-muted mb-1.5">
                상영관
              </label>
              {theaterListUnavailable ? (
                <input
                  id="schedule-theater-id"
                  type="text"
                  inputMode="numeric"
                  required
                  value={theaterId}
                  onChange={(e) => setTheaterId(e.target.value)}
                  placeholder="상영관 ID (예: 3)"
                  className="form-input"
                />
              ) : (
                <select
                  id="schedule-theater-id"
                  required
                  disabled={isLoadingOptions}
                  value={theaterId}
                  onChange={(e) => setTheaterId(e.target.value)}
                  className="form-input"
                >
                  <option value="">
                    {isLoadingOptions
                      ? '불러오는 중...'
                      : theaters.length > 0
                        ? '상영관을 선택하세요'
                        : '등록된 상영관이 없습니다'}
                  </option>
                  {theaters.map((theater) => (
                    <option key={theater.id} value={theater.id}>
                      {theater.name}
                    </option>
                  ))}
                </select>
              )}
              {theaterListUnavailable && (
                <p className="font-mono text-[11px] text-ink-muted mt-1.5">
                  상영관 목록을 불러오지 못해 ID 직접 입력으로 대체했습니다.
                </p>
              )}
            </div>

            <div>
              <label htmlFor="schedule-date" className="block font-mono text-xs text-ink-muted mb-1.5">
                상영 일자
              </label>
              <DatePicker id="schedule-date" value={scheduleDate} onChange={setScheduleDate} />
            </div>
          </div>

          {/* 상영할 영화 순서 - 동적으로 추가/삭제 가능한 리스트 */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-body text-sm text-ink-light">
                상영할 영화 순서 (아침 9시부터 순차 배정)
              </h3>
              <button
                type="button"
                onClick={handleAddMovieRow}
                className="font-mono text-xs px-3 py-1.5 rounded-md border border-marquee-line
                  text-ink-light hover:border-reel-gold transition-colors"
              >
                + 추가
              </button>
            </div>

            <div className="flex flex-col gap-3">
              {movieIds.map((movieId, index) => {
                // 이 슬롯의 예상 시작/종료 시각 - selectedSlots는 "선택된 영화만" 순서대로
                // 담겨있으므로, 이 행이 몇 번째로 선택된 영화인지 세어서 매칭한다.
                const selectedCountBeforeThisRow = movieIds.slice(0, index).filter((id) => id > 0).length
                const slotForThisRow = movieId > 0 ? selectedSlots[selectedCountBeforeThisRow] : null

                return (
                  <div
                    key={index}
                    className="flex items-center gap-3 rounded-lg border border-marquee-line bg-marquee-bg px-4 py-3"
                  >
                    <span className="shrink-0 w-6 h-6 rounded-full bg-marquee-card border border-marquee-line
                      flex items-center justify-center font-mono text-xs text-reel-gold">
                      {index + 1}
                    </span>
                    <div className="w-[500px] shrink-0 text-sm font-body">
                      <Select
                      options={formattedMovieOptions}
                      isLoading={isLoadingOptions}
                      // 현재 선택된 값을 { value, label } 객체로 찾아 넣어줌
                      value={formattedMovieOptions.find((opt) => opt.value === movieId) || null}
                      // 선택/변경 시 기존 handleChangeMovieAt 호출
                      onChange={(selected) => handleChangeMovieAt(index, selected ? String(selected.value) : '0')}
                      placeholder={isLoadingOptions ? '불러오는 중...' : '영화 검색 (초성/단어 입력)'}
                      noOptionsMessage={() => '검색 결과가 없습니다'}
                      isClearable // x 버튼 눌러서 선택 해제 가능

                      // 💡 다크 테마에 맞춘 스타일 커스터마이징
                      styles={{
                      control: (base, state) => ({
                      ...base,
                      backgroundColor: '#111111',
                      borderColor: state.isFocused ? '#B3915F' : '#333333', // 포커스 시 릴 골드 색상
                      boxShadow: state.isFocused ? '0 0 0 1px #B3915F' : 'none',
                      '&:hover': { borderColor: '#B3915F' }
                      }),
                      singleValue: (base) => ({ ...base, color: '#e5e7eb' }),
                      input: (base) => ({ ...base, color: '#e5e7eb' }), // 타이핑 글씨색
                      menu: (base) => ({ ...base, backgroundColor: '#1a1a1a', border: '1px solid #333333' }),
                      option: (base, state) => ({
                      ...base,
                      backgroundColor: state.isFocused ? '#333333' : 'transparent',
                      color: state.isSelected ? '#B3915F' : '#e5e7eb', // 선택된 항목 골드색
                      cursor: 'pointer',
                      '&:active': { backgroundColor: '#444444' }
                      }),
                      }}
                      />
                    </div>
                    {slotForThisRow && (
                      <span className="shrink-0 font-mono text-xs text-ink-muted whitespace-nowrap">
                        {formatMinutesAsTime(slotForThisRow.startMinutes)} ~{' '}
                        {formatMinutesAsTime(slotForThisRow.endMinutes)}
                      </span>
                    )}

                    <button
                      type="button"
                      onClick={() => handleRemoveMovieRow(index)}
                      disabled={movieIds.length === 1}
                      aria-label={`${index + 1}번째 영화 삭제`}
                      className="shrink-0 font-mono text-xs w-7 h-7 rounded-md border border-marquee-line
                        text-curtain-red hover:bg-curtain-red hover:text-ink-light transition-colors
                        disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:bg-transparent disabled:hover:text-curtain-red"
                    >
                      ✕
                    </button>
                  </div>
                )
              })}
            </div>

            {/* 예상 마지막 스케줄 종료 시간 안내 */}
            <p className="font-mono text-xs text-ink-muted mt-3">
              예상 마지막 스케줄 종료 시간:{' '}
              {lastSlot ? formatMinutesAsTime(lastSlot.endMinutes) : '-'}
            </p>
          </div>

          <div>
            <button
              type="submit"
              disabled={isGenerateDisabled}
              className="btn-primary sm:w-auto sm:px-8 flex items-center justify-center gap-2"
            >
              {isGenerating && (
                <span
                  className="h-3.5 w-3.5 rounded-full border-2 border-marquee-bg/40 border-t-marquee-bg animate-spin"
                  aria-hidden
                />
              )}
              {isGenerating ? '배정 중...' : '스케줄 배정'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default AdminMovieSchedule