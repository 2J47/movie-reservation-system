// ⚠️ "내 프로필(이름/이메일) 조회" API가 명세에 없어서, 로그인 시 발급받은
// accessToken(JWT)의 payload에 담긴 클레임을 임시로 읽어와 표시합니다.
// 실제 토큰에 email/name 클레임이 없으면 화면엔 자동으로 대체 문구가 뜨고,
// 백엔드에 프로필 조회 API가 추가되면 이 유틸 대신 그 API로 교체하는 게 안전합니다.
export interface DecodedTokenPayload {
  email?: string
  name?: string
  sub?: string
  role?: string
  phoneNumber?: string
  [key: string]: unknown
}

export const decodeJwtPayload = (token: string): DecodedTokenPayload | null => {
  try {
    const base64Url = token.split('.')[1]
    if (!base64Url) return null

    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((char) => '%' + char.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    )
    return JSON.parse(json) as DecodedTokenPayload
  } catch {
    return null
  }
}

/**
 * 현재 로그인한 사용자의 role 클레임을 읽어온다.
 * ⚠️ "내 role 조회" 전용 API가 없어 accessToken(JWT)의 role 클레임에 의존한다.
 * 토큰에 role 클레임이 없다면 항상 null이 반환되므로, 그 경우 AdminProtectedRoute는
 * 관리자 판별을 못 하고 전부 차단한다 (안전한 기본값 = 비관리자 취급).
 */
export const getCurrentUserRole = (): string | null => {
  const token = localStorage.getItem('accessToken')
  if (!token) return null
  return decodeJwtPayload(token)?.role ?? null
}
