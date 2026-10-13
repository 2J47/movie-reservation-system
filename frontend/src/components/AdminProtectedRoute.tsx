import { useEffect, type ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getCurrentUserRole } from '../utils/jwt'

interface AdminProtectedRouteProps {
  children: ReactNode
}

/**
 * '/admin' 하위 라우트를 감싸는 가드.
 * - 비로그인: 로그인 페이지로 보낸다 (ProtectedRoute와 동일한 사유).
 * - 로그인은 했지만 role !== 'ADMIN': 홈으로 튕기고 경고창을 띄운다.
 *
 * ⚠️ "내 role 조회" 전용 API가 없어 accessToken(JWT)의 role 클레임에 의존한다.
 * 클라이언트에서의 이 검사는 UX 차원의 1차 방어일 뿐이며, 실제 보안 경계는
 * 반드시 백엔드가 각 /api/v1/admin/** 엔드포인트에서 role을 검증해야 한다.
 * (토큰을 조작하면 이 가드는 우회될 수 있다.)
 *
 * alert()는 렌더링 도중이 아니라 useEffect에서 호출한다. 렌더 함수는 React가
 * (특히 개발 모드 StrictMode에서) 여러 번 호출할 수 있어서, 렌더 중 직접
 * alert를 부르면 같은 시도에 대해 중복으로 뜨거나 타이밍이 꼬일 수 있다.
 */
const AdminProtectedRoute = ({ children }: AdminProtectedRouteProps) => {
  const { isLoggedIn } = useAuth()
  const role = isLoggedIn ? getCurrentUserRole() : null
  const isAdmin = role === 'ADMIN'

  useEffect(() => {
    if (isLoggedIn && !isAdmin) {
      alert('관리자만 접근할 수 있는 페이지입니다.')
    }
  }, [isLoggedIn, isAdmin])

  if (!isLoggedIn) {
    return <Navigate to="/login" replace />
  }

  if (!isAdmin) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}

export default AdminProtectedRoute
