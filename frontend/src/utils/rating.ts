// 관람 등급 표시 색상은 전체 모노톤(흰/회/검) 테마의 유일한 예외.
// 백엔드가 rating을 자유 문자열로 내려주므로("전체관람가", "12세이상관람가" 등)
// 키워드 포함 여부로 판별한다. 순서가 중요: "청소년" 체크를 "15"보다 먼저 해야
// "청소년관람불가"가 잘못 분류되지 않는다.
export type RatingColorKey = 'all' | 'twelve' | 'fifteen' | 'adult' | 'unknown'

export const getRatingColorKey = (rating: string): RatingColorKey => {
  if (rating.includes('전체')) return 'all'
  if (rating.includes('청소년') || rating.includes('불가') || rating.includes('19')) return 'adult'
  if (rating.includes('15')) return 'fifteen'
  if (rating.includes('12')) return 'twelve'
  return 'unknown'
}

// tailwind.config.js의 rating.* 토큰과 짝을 이룬다.
export const RATING_TEXT_CLASS: Record<RatingColorKey, string> = {
  all: 'text-rating-all',
  twelve: 'text-rating-twelve',
  fifteen: 'text-rating-fifteen',
  adult: 'text-rating-adult',
  unknown: 'text-ink-light',
}

export const getRatingTextClass = (rating: string): string => RATING_TEXT_CLASS[getRatingColorKey(rating)]
