import axios, { AxiosError } from 'axios'
import type { ApiErrorResponse } from '../types/auth'
import { emitAuthLogout } from '../utils/authEvents'

// 결제 흐름처럼 자체 에러 모달을 따로 보여주는 화면에서
// 전역 alert()과 중복 노출되지 않도록 opt-out 할 수 있는 플래그
declare module 'axios' {
  export interface AxiosRequestConfig {
    skipGlobalErrorAlert?: boolean
  }
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 요청 인터셉터: localStorage에 accessToken이 있으면 Authorization 헤더 자동 첨부
api.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken')
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 응답 인터셉터: 공통 에러 핸들링
// 백엔드 에러 응답 형태: { "status": 400, "message": "에러 내용" }
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorResponse>) => {
    // 401(토큰 만료/무효) - 로그인 상태였다면 자동 로그아웃 이벤트를 쏜다.
    // 실제 로그아웃 처리(AuthContext)와 안내 alert, /login 이동은
    // components/AuthEventBridge.tsx가 이 이벤트를 구독해서 수행한다.
    // 여기서 일반 alert까지 같이 띄우면 "세션 만료" 안내와 중복되므로 바로 반환한다.
    if (error.response?.status === 401) {
      const hadToken = !!localStorage.getItem('accessToken')
      if (hadToken) {
        emitAuthLogout()
        return Promise.reject(error)
      }
    }

    const message =
      error.response?.data?.message ??
      error.message ??
      '알 수 없는 오류가 발생했습니다.'

    if (!error.config?.skipGlobalErrorAlert) {
      alert(message)
    }
    return Promise.reject(error)
  },
)

export default api
