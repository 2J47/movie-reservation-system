import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { signup } from '../api/auth'

const SignupPage = () => {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [name, setName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handlePhoneChange = (e: ChangeEvent<HTMLInputElement>) => {
    const rawValue = e.target.value.replace(/[^0-9]/g, '')
    let formatted = rawValue
    if (rawValue.length > 3 && rawValue.length <= 7) {
      formatted = `${rawValue.slice(0, 3)}-${rawValue.slice(3)}`
    } else if (rawValue.length > 7) {
      formatted = `${rawValue.slice(0, 3)}-${rawValue.slice(3, 7)}-${rawValue.slice(7, 11)}`
    }
    setPhoneNumber(formatted)
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (password !== passwordConfirm) {
      alert('비밀번호가 일치하지 않습니다.')
      return
    }

    setIsSubmitting(true)
    try {
      await signup({ email, password, name, phoneNumber })
      alert('회원가입이 완료되었습니다. 로그인해 주세요.')
      navigate('/login')
    } catch (error: any) {
    alert(error.response?.data?.message || '회원가입에 실패했습니다.')
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
              ADMIT ONE · NEW MEMBER
            </p>
            <h1 className="font-display text-3xl tracking-marquee text-ink-light">
              회원가입
            </h1>
          </div>

          <div className="ticket-divider mb-6" />

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <label htmlFor="name" className="block font-mono text-xs text-ink-muted mb-1.5">
                NAME
              </label>
              <input
                id="name"
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="홍길동"
                className="form-input"
              />
            </div>

            <div>
              <label
                htmlFor="phoneNumber"
                className="block font-mono text-xs text-ink-muted mb-1.5"
              >
                PHONE NUMBER
              </label>
              <input
                id="phoneNumber"
                type="tel"
                required
                value={phoneNumber}
                onChange={handlePhoneChange}
                maxLength={13}
                placeholder="010-1234-5678"
                className="form-input"
              />
            </div>

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
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="8자 이상 입력해 주세요"
                className="form-input"
              />
            </div>

            <div>
              <label
                htmlFor="passwordConfirm"
                className="block font-mono text-xs text-ink-muted mb-1.5"
              >
                CONFIRM PASSWORD
              </label>
              <input
                id="passwordConfirm"
                type="password"
                required
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
                placeholder="비밀번호를 다시 입력해 주세요"
                className="form-input"
              />
            </div>

            <button type="submit" disabled={isSubmitting} className="btn-primary mt-2">
              {isSubmitting ? '처리 중...' : '가입하기'}
            </button>
          </form>

          <p className="text-center font-body text-sm text-ink-muted mt-6">
            이미 계정이 있으신가요?{' '}
            <Link to="/login" className="text-reel-gold hover:underline">
              로그인
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default SignupPage
