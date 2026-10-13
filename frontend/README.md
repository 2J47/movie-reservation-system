# 🎬 CINEMOA - 영화 예매 서비스 프론트엔드

## 1. 프로젝트 세팅

이미 이 저장소 구조로 파일들이 생성되어 있습니다. 아래 순서대로 실행하세요.

```bash
# 1) 의존성 설치
cd movie-booking-frontend
npm install

# 2) 환경변수 파일 생성 (백엔드 주소 설정)
cp .env.example .env

# 3) 개발 서버 실행
npm run dev
```

- 기본 접속 주소: http://localhost:5173
- 백엔드(Spring Boot)가 http://localhost:8080 에서 실행 중이어야 API 연동이 정상 동작합니다.

> 만약 처음부터 `npm create vite`로 새로 만들고 싶다면:
> ```bash
> npm create vite@latest movie-booking-frontend -- --template react-ts
> cd movie-booking-frontend
> npm install
> npm install axios react-router-dom
> npm install -D tailwindcss postcss autoprefixer
> npx tailwindcss init -p
> ```
> 이후 아래 디렉토리 구조에 맞춰 각 파일 내용을 그대로 채워 넣으면 됩니다.

## 2. 디렉토리 구조

```
movie-booking-frontend/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tsconfig.node.json
├── tailwind.config.js
├── postcss.config.js
├── .env.example
└── src/
    ├── main.tsx                 # 앱 엔트리 포인트
    ├── App.tsx                  # 라우터 설정
    ├── index.css                # Tailwind + 공통 컴포넌트 클래스
    ├── vite-env.d.ts
    ├── api/
    │   ├── axios.ts             # axios 인스턴스, 토큰 인터셉터, 공통 에러 핸들링
    │   └── auth.ts               # 회원가입/로그인 API 함수
    ├── context/
    │   └── AuthContext.tsx      # 로그인 상태 전역 관리
    ├── components/
    │   ├── Layout.tsx           # Header + Outlet 공통 레이아웃
    │   └── Header.tsx           # 네비게이션 바
    ├── pages/
    │   ├── HomePage.tsx         # 영화 예매 (placeholder)
    │   ├── MyPage.tsx           # 마이페이지 (placeholder)
    │   ├── LoginPage.tsx        # 로그인
    │   └── SignupPage.tsx       # 회원가입
    └── types/
        └── auth.ts              # 인증 관련 타입 정의
```

## 3. 핵심 구현 포인트

### 3-1. Axios 인터셉터 (`src/api/axios.ts`)
- 요청 인터셉터: `localStorage.getItem('accessToken')`이 존재하면 모든 요청 헤더에
  `Authorization: Bearer {accessToken}`을 자동으로 붙입니다.
- 응답 인터셉터: 에러 발생 시 백엔드가 내려주는 `{ status, message }` 형태에서
  `message`를 추출해 `alert()`로 공통 처리합니다. 개별 페이지에서 별도 에러 핸들링 코드를
  작성할 필요가 없습니다. (401 응답 시 저장된 토큰은 자동 제거됩니다.)

### 3-2. 인증 상태 관리 (`src/context/AuthContext.tsx`)
- `localStorage`의 `accessToken` 존재 여부로 초기 로그인 상태를 판단합니다.
- 로그인/로그아웃 시 Context 상태가 즉시 갱신되어 `Header`의 버튼이 리렌더링 없이도
  실시간으로 [로그인/회원가입] ↔ [로그아웃]으로 전환됩니다.

### 3-3. 회원가입 (`/signup`)
- 이메일, 비밀번호, 비밀번호 확인 입력.
- 클라이언트에서 비밀번호 일치 여부를 먼저 검증합니다.
- `POST /api/v1/auth/signup` 성공(201) 시 `/login`으로 이동합니다.

### 3-4. 로그인 (`/login`)
- `POST /api/v1/auth/login` 성공(200) 시 응답의 `accessToken`을 `localStorage`에 저장하고
  전역 인증 상태를 갱신한 뒤 `/`(메인 페이지)로 이동합니다.

## 4. 다음 단계 제안
- 영화 목록 조회 / 상세 / 좌석 예매 API 연동 (`HomePage.tsx` 확장)
- 마이페이지 예매 내역 조회 API 연동
- 비로그인 상태에서 마이페이지 접근 시 로그인 페이지로 리다이렉트하는 `ProtectedRoute` 추가
- accessToken 만료 시 refreshToken을 이용한 재발급 로직 (백엔드 명세 확정 후)
