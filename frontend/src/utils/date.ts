// 날짜를 'YYYY-MM-DD' 형태의 key로 변환 (로컬 타임존 기준)
export const toDateKey = (date: Date): string => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// ISO 문자열을 'HH:mm' 형태로 변환
export const formatTime = (iso: string): string => {
  const date = new Date(iso)
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

// ISO 문자열을 'YYYY.MM.DD (요일) HH:mm' 형태로 변환 (마이페이지 예매 내역용)
export const formatDateTime = (iso: string): string => {
  const date = new Date(iso)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const dayOfWeek = ['일', '월', '화', '수', '목', '금', '토'][date.getDay()]
  return `${y}.${m}.${d} (${dayOfWeek}) ${formatTime(iso)}`
}

// ISO 문자열 2개를 'MM.DD (요일) HH:mm ~ HH:mm' 형태로 변환 (스마트 티켓용)
export const formatTicketSchedule = (startIso: string, endIso: string): string => {
  const start = new Date(startIso)
  const m = String(start.getMonth() + 1).padStart(2, '0')
  const d = String(start.getDate()).padStart(2, '0')
  const dayOfWeek = ['일', '월', '화', '수', '목', '금', '토'][start.getDay()]
  return `${m}.${d} (${dayOfWeek}) ${formatTime(startIso)} ~ ${formatTime(endIso)}`
}

// 스마트 티켓의 DATE / TIME 칸을 따로 표시할 때 쓰는 버전 (formatTicketSchedule의 분리형)
export const formatTicketDateParts = (
  startIso: string,
  endIso: string,
): { date: string; time: string } => {
  const start = new Date(startIso)
  const m = String(start.getMonth() + 1).padStart(2, '0')
  const d = String(start.getDate()).padStart(2, '0')
  const dayOfWeek = ['일', '월', '화', '수', '목', '금', '토'][start.getDay()]
  return {
    date: `${m}.${d} (${dayOfWeek})`,
    time: `${formatTime(startIso)} ~ ${formatTime(endIso)}`,
  }
}

export interface DateTab {
  key: string // 'YYYY-MM-DD'
  label: string // '오늘(8/6)' 등
  date: Date
}

// 오늘부터 3일치 날짜 탭 생성
export const getDateTabs = (baseDate: Date = new Date()): DateTab[] => {
  const dayLabels = ['오늘', '내일', '모레']

  return dayLabels.map((label, offset) => {
    const date = new Date(baseDate)
    date.setDate(date.getDate() + offset)
    const md = `${date.getMonth() + 1}/${date.getDate()}`
    return {
      key: toDateKey(date),
      label: `${label}(${md})`,
      date,
    }
  })
}
