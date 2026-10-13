// 첫 영화 시작 시각: 오전 09:00 (분 단위로 환산: 9 * 60 = 540)
export const SCHEDULE_START_MINUTES = 9 * 60
// 영화 사이 쉬는 시간(분)
export const BREAK_MINUTES = 20
// 마지노선: 자정 23:59 (분 단위)
export const MIDNIGHT_LIMIT_MINUTES = 23 * 60 + 59

export interface ScheduleSlot {
  movieId: number
  title: string
  duration: number
  startMinutes: number
  endMinutes: number
}

interface ScheduleSlotInput {
  id: number
  title: string
  duration: number
}

// 09:00부터 (duration + 20분)을 차례로 누적해 각 영화의 시작/종료 시각(분 단위)을 계산한다.
export const buildScheduleSlots = (movies: ScheduleSlotInput[]): ScheduleSlot[] => {
  let cursor = SCHEDULE_START_MINUTES

  return movies.map((movie) => {
    const startMinutes = cursor
    const endMinutes = startMinutes + movie.duration
    cursor = endMinutes + BREAK_MINUTES

    return {
      movieId: movie.id,
      title: movie.title,
      duration: movie.duration,
      startMinutes,
      endMinutes,
    }
  })
}

// 분 단위 값을 "HH:mm"으로 표시한다. (24시를 넘어가면 다음날로 표시하지 않고 그냥 24h 기준으로 보여줌)
export const formatMinutesAsTime = (totalMinutes: number): string => {
  const clamped = Math.max(totalMinutes, 0)
  const hours = Math.floor(clamped / 60) % 24
  const minutes = clamped % 60
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
}
