// 백엔드는 좌석 데이터(행/열/예약여부)만 내려주고 통로 위치 같은 배치 정보는 주지 않는다.
// 그래서 상영관 "이름"으로 그 상영관의 성격(대형 IMAX/프리미엄 리클라이너 등)을 유추해서
// 순수하게 화면에만 쓰는 통로 위치·좌석 크기 프리셋을 정의한다. 실제 좌석 데이터(seatId 등)
// 에는 전혀 영향을 주지 않는, 오직 렌더링용 설정이다.
export interface TheaterLayoutPreset {
  // 전체 열 수에 대한 비율(0~1). 이 비율에 해당하는 열 "뒤"에 통로(빈 공간)를 넣는다.
  aisleRatios: number[]
  // 좌석 버튼 크기 (리클라이너처럼 좌석이 적고 넓은 상영관은 더 크게)
  seatSizeClass: string
  // 통로(빈 공간)의 너비
  aisleWidthClass: string
}

const IMAX_PRESET: TheaterLayoutPreset = {
  // 608석 규모의 대형관 - 좌우 사이드 블록 + 넓은 중앙 블록, 통로 2개
  aisleRatios: [0.16, 0.84],
  seatSizeClass: 'w-7 h-7 text-[11px]',
  aisleWidthClass: 'w-5',
}

const DOLBY_PRESET: TheaterLayoutPreset = {
  // 264석 - 3블록 구성, 표준 통로 2개
  aisleRatios: [0.2, 0.8],
  seatSizeClass: 'w-8 h-8 text-xs',
  aisleWidthClass: 'w-5',
}

const RECLINER_PRESET: TheaterLayoutPreset = {
  // 70석 - 좌석 자체가 넓고 한 줄에 적게 들어가는 프리미엄관, 중앙 통로 1개만
  aisleRatios: [0.5],
  seatSizeClass: 'w-11 h-11 text-sm',
  aisleWidthClass: 'w-6',
}

const STANDARD_2D_PRESET: TheaterLayoutPreset = {
  // 169석 / 120석 - 가장 일반적인 3블록 구성
  aisleRatios: [0.25, 0.75],
  seatSizeClass: 'w-9 h-9 text-xs',
  aisleWidthClass: 'w-4',
}

// 상영관 이름 문자열에 포함된 키워드로 프리셋을 매칭한다.
// "1관 (IMAX)", "2관 (DOLBY)", "3관 (리클라이너)", "4관 (2D)", "5관 (2D)" 같은 이름을 가정.
export const resolveTheaterLayoutPreset = (theaterName?: string): TheaterLayoutPreset => {
  if (!theaterName) return STANDARD_2D_PRESET

  if (theaterName.includes('IMAX')) return IMAX_PRESET
  if (theaterName.includes('DOLBY') || theaterName.includes('돌비')) return DOLBY_PRESET
  if (theaterName.includes('리클라이너')) return RECLINER_PRESET
  return STANDARD_2D_PRESET
}

// 전체 열 수(maxColumn)와 비율을 바탕으로, "이 열 번호 뒤에 통로를 넣어라"에 해당하는
// 열 번호 집합을 계산한다. 양 끝(맨 앞/맨 뒤)에는 통로를 넣지 않는다.
export const getAisleAfterColumns = (maxColumn: number, ratios: number[]): Set<number> => {
  const columns = new Set<number>()
  ratios.forEach((ratio) => {
    const column = Math.round(maxColumn * ratio)
    if (column > 0 && column < maxColumn) {
      columns.add(column)
    }
  })
  return columns
}
