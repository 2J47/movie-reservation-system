import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { AUTH_LOGOUT_EVENT } from '../utils/authEvents'

/**
 * 화면을 그리지 않는 순수 이벤트 리스너 컴포넌트.
 * BrowserRouter 안쪽(라우팅 가능한 위치)에 한 번만 렌더링해두면,
 * axios.ts가 401을 감지해 쏘는 'auth:logout' 이벤트를 받아
 * 실제 로그아웃 처리(AuthContext) + 안내 + /login 이동을 수행한다.
 */
const AuthEventBridge = () => {
  const { logout } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    const handleAutoLogout = () => {
      logout()
      alert('세션이 만료되어 로그아웃되었습니다. 다시 로그인해 주세요.')
      navigate('/login')
    }

    window.addEventListener(AUTH_LOGOUT_EVENT, handleAutoLogout)
    return () => window.removeEventListener(AUTH_LOGOUT_EVENT, handleAutoLogout)
  }, [logout, navigate])

  return null
}

export default AuthEventBridge
