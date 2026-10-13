import { useLocation } from 'react-router-dom'

interface SuccessState {
  reservationNo: string
  finalAmount: number
}

const SuccessPage = () => {
  const location = useLocation()
  const state = location.state as SuccessState | null

  return (
    <div className="max-w-2xl mx-auto px-6 py-24 text-center">
      <p className="font-mono text-xs tracking-marquee text-reel-gold mb-2">BOOKING COMPLETE</p>
      <h1 className="font-display text-4xl tracking-marquee text-ink-light mb-4">예매 완료</h1>

      {state ? (
        <div className="ticket-card inline-flex flex-col items-center gap-1 px-10 py-6">
          <span className="font-mono text-xs text-ink-muted">예매 번호</span>
          <span className="font-display text-2xl tracking-marquee text-reel-gold mb-2">
            {state.reservationNo}
          </span>
          <span className="font-mono text-sm text-ink-light">
            결제 금액 {state.finalAmount.toLocaleString('ko-KR')}원
          </span>
        </div>
      ) : (
        <p className="font-body text-ink-muted">예매 상세 화면은 다음 단계에서 구현합니다.</p>
      )}
    </div>
  )
}

export default SuccessPage
