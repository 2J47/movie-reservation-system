// 회원가입 요청 바디
export interface SignupRequest {
  email: string
  password: string
  name: string
  phoneNumber: string
}

// 로그인 요청 바디
export interface LoginRequest {
  email: string
  password: string
}

// 로그인 응답 바디
export interface LoginResponse {
  accessToken: string
}

// 백엔드 공통 에러 응답 형태: { "status": 400, "message": "에러 내용" }
export interface ApiErrorResponse {
  status: number
  message: string
}
