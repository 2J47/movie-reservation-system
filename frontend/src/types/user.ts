// 기본 정보 수정
export interface UpdateProfileRequest {
  name: string
  phoneNumber: string
}

// 비밀번호 변경
export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

// 내 프로필(+ 잔여 포인트) 조회 응답
export interface UserProfile {
  userId: number
  email: string
  name: string
  phoneNumber: string
  balance: number
}