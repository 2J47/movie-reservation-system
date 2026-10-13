import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate, type Location } from 'react-router-dom'
import { login as loginApi } from '../api/auth'
import { useAuth } from '../context/AuthContext'

interface LocationState {
  from?: Location
}

const LoginPage = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    setIsSubmitting(true)
    try {
      const { accessToken } = await loginApi({ email, password })
      login(accessToken)

      // ProtectedRoute에서 넘겨준 원래 목적지가 있으면 그곳으로, 없으면 홈으로.
      // from.state까지 그대로 살려주면 결제 페이지처럼 state 기반으로 동작하는
      // 화면(좌석 정보 등)도 로그인 후 자연스럽게 이어진다.
      const { from } = (location.state as LocationState) ?? {}
      if (from) {
        navigate(`${from.pathname}${from.search}`, { replace: true, state: from.state })
      } else {
        navigate('/')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-6 py-16">
      <div className="w-full max-w-sm">
        <div className="ticket-card px-8 pt-8 pb-10">
          <div className="ticket-notch-left" />
          <div className="ticket-notch-right" />

          <div className="text-center mb-6">
            <p className="font-mono text-xs tracking-marquee text-ink-muted mb-1">
              ADMIT ONE · WELCOME BACK
            </p>
            <h1 className="font-display text-3xl tracking-marquee text-ink-light">로그인</h1>
          </div>

          <div className="ticket-divider mb-6" />

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <label htmlFor="email" className="block font-mono text-xs text-ink-muted mb-1.5">
                EMAIL
              </label>
              <input
                id="email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="user@test.com"
                className="form-input"
              />
            </div>

            <div>
              <label htmlFor="password" className="block font-mono text-xs text-ink-muted mb-1.5">
                PASSWORD
              </label>
              <input
                id="password"
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="비밀번호를 입력해 주세요"
                className="form-input"
              />
            </div>

            <button type="submit" disabled={isSubmitting} className="btn-primary mt-2">
              {isSubmitting ? '로그인 중...' : '로그인'}
            </button>
          </form>

          <p className="text-center font-body text-sm text-ink-muted mt-6">
            아직 계정이 없으신가요?{' '}
            <Link to="/signup" className="text-reel-gold hover:underline">
              회원가입
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
