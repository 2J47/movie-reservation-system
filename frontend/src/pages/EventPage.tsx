import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { getAvailableCoupons, claimCoupon } from '../api/coupon'
import type { MasterCoupon } from '../types/coupon'
import { useAuth } from '../context/AuthContext'
import { toDateKey } from '../utils/date'
import { formatDiscountLabel } from '../utils/coupon'

const EventPage = () => {
  const { isLoggedIn } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [coupons, setCoupons] = useState<MasterCoupon[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [claimingId, setClaimingId] = useState<number | null>(null)

  useEffect(() => {
    let isMounted = true

    const fetchCoupons = async () => {
      setIsLoading(true)
      try {
        const data = await getAvailableCoupons()
        if (isMounted) setCoupons(data)
      } finally {
        if (isMounted) setIsLoading(false)
      }
    }

    fetchCoupons()
    return () => {
      isMounted = false
    }
  }, [])

  const handleClaim = async (coupon: MasterCoupon) => {
    // 목록 조회는 비로그인도 가능하지만, 발급은 로그인이 필요하다.
    // 여기서 막지 않으면 401이 나고, ProtectedRoute와 다르게 이 페이지 자체는
    // 비로그인 사용자도 둘러볼 수 있어야 하므로 버튼 클릭 시점에만 검사한다.
    if (!isLoggedIn) {
      navigate('/login', { state: { from: location } })
      return
    }

    setClaimingId(coupon.id)
    try {
      await claimCoupon(coupon.id)
      alert('쿠폰이 발급되었습니다!')
    } catch {
      // 에러 메시지 자체는 api/axios.ts의 전역 인터셉터가 alert로 이미 띄워준다
      // (예: 400 "이미 발급받은 쿠폰입니다."). 여기서는 콘솔에 "Uncaught (in
      // promise)"로 새어나가지 않도록 잡아주기만 한다.
    } finally {
      setClaimingId(null)
    }
  }

  return (
    <div className="max-w-5xl mx-auto px-6 py-12">
      <p className="font-mono text-xs tracking-marquee text-reel-gold mb-2">COUPON EVENT</p>
      <h1 className="font-display text-2xl tracking-marquee text-ink-light mb-5">이벤트/쿠폰</h1>

      {isLoading ? (
        <div className="grid sm:grid-cols-2 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-28 rounded-xl bg-marquee-card border border-marquee-line animate-pulse" />
          ))}
        </div>
      ) : coupons.length === 0 ? (
        <p className="font-body text-sm text-ink-muted py-16 text-center border border-dashed border-marquee-line rounded-xl">
          현재 발급 가능한 쿠폰이 없습니다.
        </p>
      ) : (
        <div className="grid sm:grid-cols-2 gap-4">
          {coupons.map((coupon) => (
            <CouponEventCard
              key={coupon.id}
              coupon={coupon}
              isClaiming={claimingId === coupon.id}
              onClaim={() => handleClaim(coupon)}
            />
          ))}
        </div>
      )}
    </div>
  )
}

const CouponEventCard = ({
  coupon,
  isClaiming,
  onClaim,
}: {
  coupon: MasterCoupon
  isClaiming: boolean
  onClaim: () => void
}) => {
  return (
    <div className="ticket-card px-6 py-6 flex items-center justify-between gap-4">
      <div className="ticket-notch-left" />
      <div className="ticket-notch-right" />

      <div className="min-w-0">
        <h3 className="font-display text-xl tracking-marquee text-ink-light truncate mb-1">
          {coupon.name}
        </h3>
        <p className="font-mono text-sm text-reel-gold mb-1">{formatDiscountLabel(coupon)}</p>
        <p className="font-mono text-xs text-ink-muted">
          만료일: {toDateKey(new Date(coupon.expiresAt))}
        </p>
      </div>

      <button
        type="button"
        disabled={isClaiming}
        onClick={onClaim}
        className="shrink-0 font-body text-sm px-5 py-2.5 rounded-lg bg-reel-gold hover:bg-reel-goldDark
          text-marquee-bg font-semibold transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
      >
        {isClaiming ? '처리 중...' : '쿠폰 받기'}
      </button>
    </div>
  )
}

export default EventPage
