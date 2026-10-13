import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getCurrentUserRole } from '../utils/jwt'

const Header = () => {
  const { isLoggedIn, logout } = useAuth()
  const navigate = useNavigate()
  // isLoggedIn이 바뀔 때(로그인/로그아웃)마다 같이 다시 계산된다.
  const isAdmin = isLoggedIn && getCurrentUserRole() === 'ADMIN'

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="sticky top-0 z-50 bg-marquee-bg/95 backdrop-blur border-b border-marquee-line">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        {/* 로고 */}
        <Link
          to="/"
          className="font-display text-2xl tracking-marquee text-reel-gold select-none"
        >
          CINEMOA
        </Link>

        {/* 네비게이션 */}
        <nav className="hidden sm:flex items-center gap-8 font-body text-sm text-ink-light">
          <Link to="/" className="hover:text-reel-gold transition-colors">
            영화 예매
          </Link>
          <Link to="/events" className="hover:text-reel-gold transition-colors">
            이벤트/쿠폰
          </Link>
          <Link to="/me" className="hover:text-reel-gold transition-colors">
            마이페이지
          </Link>
          {isLoggedIn && (
            <Link to="/tickets" className="hover:text-reel-gold transition-colors">
              스마트 티켓
            </Link>
          )}
          {isAdmin && (
            <Link to="/admin" className="hover:text-reel-gold transition-colors">
              관리자 페이지
            </Link>
          )}
        </nav>

        {/* 우측 인증 버튼 */}
        <div className="flex items-center gap-3">
          {isLoggedIn ? (
            <button
              onClick={handleLogout}
              className="font-body text-sm px-4 py-2 rounded-lg border border-marquee-line
                text-ink-light hover:border-curtain-red hover:text-curtain-red transition-colors"
            >
              로그아웃
            </button>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="font-body text-sm px-4 py-2 rounded-lg text-ink-light
                  hover:text-reel-gold transition-colors"
              >
                로그인
              </Link>
              <Link
                to="/signup"
                className="font-body text-sm px-4 py-2 rounded-lg bg-reel-gold text-marquee-bg
                  font-semibold hover:bg-reel-goldDark transition-colors"
              >
                회원가입
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header
