import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

interface ProtectedRouteProps {
  children: ReactNode
}

/**
 * 로그인이 필요한 페이지(좌석 선택, 결제, 마이페이지 등)를 감싸는 가드.
 * 비로그인 상태면 API 호출이 아예 일어나기 전에 /login으로 리다이렉트한다.
 * (여기서 막지 않으면 각 API가 401을 반환하고, 그게 전역 axios 인터셉터의
 *  alert()로 노출되는 방식으로 "새어 나가서" 사용자 경험이 나빠진다.)
 */
const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { isLoggedIn } = useAuth()
  const location = useLocation()

  if (!isLoggedIn) {
    // 로그인 후 원래 가려던 페이지로 돌아올 수 있도록 목적지를 함께 넘긴다.
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <>{children}</>
}

export default ProtectedRoute
