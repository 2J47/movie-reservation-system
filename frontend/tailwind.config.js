/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // 모노톤 테마 (흰색/회색/검은색만 사용)
        marquee: {
          bg: '#0A0A0A',      // 페이지 배경 (거의 검정)
          card: '#1A1A1A',    // 카드/입력창 배경
          line: '#3A3A3A',    // 구분선, 보더
        },
        reel: {
          gold: '#FFFFFF',    // 포인트/CTA (기존 골드 자리 - 흰색으로 대체)
          goldDark: '#D4D4D4', // 위 색상의 hover/active 톤 (연한 회색)
        },
        curtain: {
          red: '#707070',     // 경고/취소/로그아웃 (기존 레드 자리 - 중간 회색으로 대체)
        },
        ink: {
          light: '#F5F5F5',   // 기본 텍스트 (거의 흰색)
          muted: '#9A9A9A',   // 보조 텍스트 (회색)
        },
        // 관람 등급 전용 색상 (모노톤 예외 - 메인 화면 등급 배지에만 사용)
        rating: {
          all: '#4ADE80',      // 전체관람가 - 초록
          twelve: '#FACC15',   // 12세이상관람가 - 노랑
          fifteen: '#FB923C',  // 15세이상관람가 - 주황
          adult: '#F87171',    // 청소년관람불가 - 빨강
        },
      },
      fontFamily: {
        display: ['"Bebas Neue"', 'sans-serif'],
        body: ['"Inter"', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
      letterSpacing: {
        marquee: '0.08em',
      },
    },
  },
  plugins: [],
}
