import { useEffect, useRef, useState } from 'react'

interface DatePickerProps {
  id?: string
  value: string // 'YYYY-MM-DD'
  onChange: (value: string) => void
  disabled?: boolean
}

const DAY_LABELS = ['일', '월', '화', '수', '목', '금', '토']

const toDateKey = (date: Date): string => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const parseDateKey = (value: string): Date => {
  // new Date('YYYY-MM-DD')는 UTC 자정으로 해석돼 로컬 타임존에 따라 하루 밀릴 수 있어
  // 로컬 자정 기준으로 직접 파싱한다.
  const [y, m, d] = value.split('-').map(Number)
  return new Date(y, (m ?? 1) - 1, d ?? 1)
}

const formatDisplay = (value: string): string => {
  const date = parseDateKey(value)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}.${m}.${d} (${DAY_LABELS[date.getDay()]})`
}

// 네이티브 <input type="date">는 브라우저마다 달력 UI가 제각각이고 다크 테마에
// 맞춰 스타일링할 수 없어서, 테마에 맞는 커스텀 달력 드롭다운을 직접 구현했다.
const DatePicker = ({ id, value, onChange, disabled }: DatePickerProps) => {
  const [isOpen, setIsOpen] = useState(false)
  const selected = parseDateKey(value)
  const [viewYear, setViewYear] = useState(selected.getFullYear())
  const [viewMonth, setViewMonth] = useState(selected.getMonth()) // 0-indexed
  const containerRef = useRef<HTMLDivElement>(null)

  // 열릴 때마다 현재 선택된 날짜가 있는 달을 보여주도록 동기화
  useEffect(() => {
    if (!isOpen) return
    const d = parseDateKey(value)
    setViewYear(d.getFullYear())
    setViewMonth(d.getMonth())
  }, [isOpen, value])

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const firstOfMonth = new Date(viewYear, viewMonth, 1)
  const startWeekday = firstOfMonth.getDay()
  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate()

  const cells: (Date | null)[] = [
    ...new Array(startWeekday).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => new Date(viewYear, viewMonth, i + 1)),
  ]
  while (cells.length % 7 !== 0) cells.push(null)

  const goPrevMonth = () => {
    const d = new Date(viewYear, viewMonth - 1, 1)
    setViewYear(d.getFullYear())
    setViewMonth(d.getMonth())
  }
  const goNextMonth = () => {
    const d = new Date(viewYear, viewMonth + 1, 1)
    setViewYear(d.getFullYear())
    setViewMonth(d.getMonth())
  }

  const handleSelectDay = (date: Date) => {
    onChange(toDateKey(date))
    setIsOpen(false)
  }

  return (
    <div className="relative" ref={containerRef}>
      <button
        type="button"
        id={id}
        disabled={disabled}
        onClick={() => setIsOpen((prev) => !prev)}
        className="form-input w-full text-left flex items-center justify-between gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span>{value ? formatDisplay(value) : '날짜 선택'}</span>
        <span className="text-ink-muted" aria-hidden>
          ▾
        </span>
      </button>

      {isOpen && (
        <div className="absolute z-20 mt-2 w-64 rounded-lg border border-marquee-line bg-marquee-card p-3 shadow-2xl shadow-black/50">
          <div className="flex items-center justify-between mb-2">
            <button
              type="button"
              onClick={goPrevMonth}
              className="w-7 h-7 rounded-md text-ink-light hover:border-reel-gold border border-transparent transition-colors"
            >
              ‹
            </button>
            <p className="font-mono text-sm text-ink-light">
              {viewYear}.{String(viewMonth + 1).padStart(2, '0')}
            </p>
            <button
              type="button"
              onClick={goNextMonth}
              className="w-7 h-7 rounded-md text-ink-light hover:border-reel-gold border border-transparent transition-colors"
            >
              ›
            </button>
          </div>

          <div className="grid grid-cols-7 gap-1 mb-1">
            {DAY_LABELS.map((label) => (
              <span key={label} className="text-center font-mono text-[10px] text-ink-muted">
                {label}
              </span>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-1">
            {cells.map((date, i) => {
              if (!date) return <span key={i} />
              const dateKey = toDateKey(date)
              const isSelected = dateKey === value
              return (
                <button
                  key={dateKey}
                  type="button"
                  onClick={() => handleSelectDay(date)}
                  className={`h-8 rounded-md font-mono text-xs transition-colors border ${
                    isSelected
                      ? 'bg-reel-gold border-reel-gold text-marquee-bg font-semibold'
                      : 'border-transparent text-ink-light hover:border-reel-gold'
                  }`}
                >
                  {date.getDate()}
                </button>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}

export default DatePicker
