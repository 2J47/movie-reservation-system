import { useState, type FormEvent } from 'react'
import { createMasterCoupon, grantCouponToUser } from '../api/admin'
import type { CouponDiscountType } from '../types/admin'
import AdminMovieSchedule from '../components/AdminMovieSchedule'

type AdminTab = 'COUPON' | 'MOVIE_SYNC' | 'USER'

const AdminPage = () => {
  const [activeTab, setActiveTab] = useState<AdminTab>('COUPON')

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      <p className="font-mono text-xs tracking-marquee text-reel-gold mb-2">BACKOFFICE</p>
      <h1 className="font-display text-3xl tracking-marquee text-ink-light mb-8">관리자 페이지</h1>

      <div className="flex flex-col sm:flex-row gap-8">
        {/* 좌측 사이드바 */}
        <aside className="sm:w-52 shrink-0">
          <nav className="flex sm:flex-col gap-2">
            <button
              type="button"
              onClick={() => setActiveTab('COUPON')}
              className={`text-left font-body text-sm px-4 py-2.5 rounded-lg border transition-colors ${
                activeTab === 'COUPON'
                  ? 'bg-reel-gold text-marquee-bg border-reel-gold font-semibold'
                  : 'border-marquee-line text-ink-light hover:border-reel-gold'
              }`}
            >
              쿠폰 관리
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('MOVIE_SYNC')}
              className={`text-left font-body text-sm px-4 py-2.5 rounded-lg border transition-colors ${
                activeTab === 'MOVIE_SYNC'
                  ? 'bg-reel-gold text-marquee-bg border-reel-gold font-semibold'
                  : 'border-marquee-line text-ink-light hover:border-reel-gold'
              }`}
            >
              영화/스케줄 관리
            </button>
          </nav>
        </aside>

        {/* 메인 영역 */}
        <div className="flex-1 min-w-0">
          {activeTab === 'COUPON' && <CouponManagementPanel />}
          {activeTab === 'MOVIE_SYNC' && <AdminMovieSchedule />}
        </div>
      </div>
    </div>
  )
}

const DISCOUNT_TYPE_OPTIONS: { value: CouponDiscountType; label: string }[] = [
  { value: 'AMOUNT', label: '정액 할인 (원)' },
  { value: 'PERCENT', label: '정률 할인 (%)' },
]

const CouponManagementPanel = () => {
  // 쿠폰 생성 폼 상태
  const [name, setName] = useState('')
  const [discountType, setDiscountType] = useState<CouponDiscountType>('AMOUNT')
  const [discountValue, setDiscountValue] = useState('')
  const [expireDays, setExpireDays] = useState('')
  const [isCreating, setIsCreating] = useState(false)

  // 쿠폰 지급 폼 상태
  const [grantCouponId, setGrantCouponId] = useState('')
  const [grantUserId, setGrantUserId] = useState('')
  const [isGranting, setIsGranting] = useState(false)

  const handleCreateCoupon = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!name.trim() || !discountValue || !expireDays) return

    // 정률(PERCENT) 할인은 100%를 초과할 수 없다.
    if (discountType === 'PERCENT' && Number(discountValue) > 100) {
      alert('정률 할인은 100%를 초과할 수 없습니다.')
      return
    }

    setIsCreating(true)
    try {
      const result = await createMasterCoupon({
        name: name.trim(),
        discountType,
        discountValue: Number(discountValue),
        expireDays: Number(expireDays),
      })

      alert('쿠폰이 생성되었습니다.')
      setName('')
      setDiscountValue('')
      setExpireDays('')

      // 생성 응답에 couponId가 내려오면, 바로 아래 지급 폼에 편의상 채워준다.
      // (응답 스펙이 명세에 없어 존재할 때만 best-effort로 채움 — types/admin.ts 참고)
      if (typeof result.couponId === 'number') {
        setGrantCouponId(String(result.couponId))
      }
    } catch {
      // 에러 메시지 자체는 api/axios.ts의 전역 인터셉터가 alert로 이미 띄워준다.
      // 여기서는 콘솔에 "Uncaught (in promise)"로 새어나가지 않도록 잡아주기만 한다.
    } finally {
      setIsCreating(false)
    }
  }

  const handleGrantCoupon = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!grantCouponId.trim() || !grantUserId.trim()) return

    setIsGranting(true)
    try {
      await grantCouponToUser(grantUserId.trim(), grantCouponId.trim())
      alert('쿠폰이 발급되었습니다.')
      setGrantUserId('')
    } catch {
      // 에러 메시지 자체는 api/axios.ts의 전역 인터셉터가 alert로 이미 띄워준다.
    } finally {
      setIsGranting(false)
    }
  }

  return (
    <div className="flex flex-col gap-8">
      {/* 쿠폰 생성 섹션 */}
      <section className="rounded-xl border border-marquee-line bg-marquee-card px-8 py-8">
        <p className="font-mono text-xs tracking-marquee text-reel-gold mb-1">CREATE COUPON</p>
        <h2 className="font-display text-xl tracking-marquee text-ink-light mb-6">쿠폰 생성</h2>

        <form onSubmit={handleCreateCoupon} className="grid sm:grid-cols-2 gap-4">
          <div className="sm:col-span-2">
            <label htmlFor="coupon-name" className="block font-mono text-xs text-ink-muted mb-1.5">
              쿠폰 이름
            </label>
            <input
              id="coupon-name"
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="예: 여름 휴가 할인"
              className="form-input"
            />
          </div>

          <div>
            <label htmlFor="discount-type" className="block font-mono text-xs text-ink-muted mb-1.5">
              할인 타입
            </label>
            <select
              id="discount-type"
              value={discountType}
              onChange={(e) => {
                const nextType = e.target.value as CouponDiscountType
                setDiscountType(nextType)
                // AMOUNT -> PERCENT로 바꿀 때 기존에 입력해둔 값이 100을 넘으면 잘라준다.
                if (nextType === 'PERCENT' && Number(discountValue) > 100) {
                  setDiscountValue('100')
                }
              }}
              className="form-input"
            >
              {DISCOUNT_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="discount-value" className="block font-mono text-xs text-ink-muted mb-1.5">
              할인 값{discountType === 'PERCENT' ? ' (%)' : ' (원)'}
            </label>
            <input
              id="discount-value"
              type="number"
              min={1}
              max={discountType === 'PERCENT' ? 100 : undefined}
              required
              value={discountValue}
              onChange={(e) => setDiscountValue(e.target.value)}
              placeholder={discountType === 'PERCENT' ? '예: 10' : '예: 3000'}
              className="form-input"
            />
          </div>

          <div className="sm:col-span-2">
            <label htmlFor="expire-days" className="block font-mono text-xs text-ink-muted mb-1.5">
              만료 기한 (오늘로부터 N일 뒤)
            </label>
            <input
              id="expire-days"
              type="number"
              min={1}
              required
              value={expireDays}
              onChange={(e) => setExpireDays(e.target.value)}
              placeholder="예: 30"
              className="form-input sm:max-w-xs"
            />
          </div>

          <div className="sm:col-span-2">
            <button
              type="submit"
              disabled={isCreating}
              className="btn-primary sm:w-auto sm:px-8"
            >
              {isCreating ? '생성 중...' : '생성하기'}
            </button>
          </div>
        </form>
      </section>

      {/* 쿠폰 지급 섹션 */}
      <section className="rounded-xl border border-marquee-line bg-marquee-card px-8 py-8">
        <p className="font-mono text-xs tracking-marquee text-reel-gold mb-1">GRANT COUPON</p>
        <h2 className="font-display text-xl tracking-marquee text-ink-light mb-6">쿠폰 지급</h2>

        <form onSubmit={handleGrantCoupon} className="grid sm:grid-cols-2 gap-4">
          <div>
            <label htmlFor="grant-coupon-id" className="block font-mono text-xs text-ink-muted mb-1.5">
              쿠폰 ID
            </label>
            <input
              id="grant-coupon-id"
              type="text"
              inputMode="numeric"
              required
              value={grantCouponId}
              onChange={(e) => setGrantCouponId(e.target.value)}
              placeholder="예: 12"
              className="form-input"
            />
          </div>

          <div>
            <label htmlFor="grant-user-id" className="block font-mono text-xs text-ink-muted mb-1.5">
              유저 ID
            </label>
            <input
              id="grant-user-id"
              type="text"
              inputMode="numeric"
              required
              value={grantUserId}
              onChange={(e) => setGrantUserId(e.target.value)}
              placeholder="예: 7"
              className="form-input"
            />
          </div>

          <div className="sm:col-span-2">
            <button
              type="submit"
              disabled={isGranting}
              className="btn-primary sm:w-auto sm:px-8"
            >
              {isGranting ? '지급 중...' : '특정 유저에게 지급하기'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default AdminPage
