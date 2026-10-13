import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { AxiosError } from 'axios'
import { getMyAssets } from '../api/asset'
import { createReservation, cancelReservation } from '../api/reservation'
import { verifyPayment } from '../api/payment'
import { requestPortOnePayment } from '../lib/portone'
import type { CheckoutState } from '../types/checkout'
import type { UserAssets } from '../types/asset'
import type { ApiErrorResponse } from '../types/auth'
import { formatTime } from '../utils/date'
import { calculateDiscountAmount, formatDiscountLabel } from '../utils/coupon'
import { SEAT_PRICE } from '../constants/pricing'
import ReceiptPanel from '../components/ReceiptPanel'
import AsciiRule from '../components/AsciiRule'

// 백엔드 공통 에러 응답({status, message})과 그 외 예외를 모두 사람이 읽을 메시지로 변환
const getErrorMessage = (error: unknown, fallback: string): string => {
  if (error instanceof AxiosError) {
    const data = error.response?.data as ApiErrorResponse | undefined
    return data?.message ?? error.message ?? fallback
  }
  if (error instanceof Error) return error.message
  return fallback
}

const CheckoutPage = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const state = location.state as CheckoutState | null

  const [assets, setAssets] = useState<UserAssets | null>(null)
  const [assetsError, setAssetsError] = useState<string | null>(null)
  const [isLoadingMeta, setIsLoadingMeta] = useState(true)

  const [selectedCouponId, setSelectedCouponId] = useState<number | null>(null)
  const [usePointAmount, setUsePointAmount] = useState(0)
  const [isPaying, setIsPaying] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  // 좌석 선택 페이지를 거치지 않고 직접 /checkout으로 진입한 경우 방어
  useEffect(() => {
    if (!state) {
      alert('좌석 선택 정보가 없습니다. 처음부터 다시 진행해 주세요.')
      navigate('/', { replace: true })
    }
  }, [state, navigate])

  // 내 자산(포인트 잔액 + 보유 쿠폰 목록) 조회
  useEffect(() => {
    let isMounted = true

    const fetchAssets = async () => {
      setIsLoadingMeta(true)
      try {
        const assetData = await getMyAssets()
        if (isMounted) {
          setAssets(assetData)
          setAssetsError(null)
        }
      } catch (error) {
        if (isMounted) {
          // 포인트/쿠폰 조회가 실패해도 결제 자체는 진행할 수 있어야 하므로
          // 페이지를 막지 않고, 할인 없이(0P·쿠폰 없음) 진행 가능하도록 안내만 한다.
          setAssets({ balance: 0, coupons: [] })
          setAssetsError(getErrorMessage(error, '포인트/쿠폰 정보를 불러오지 못했습니다.'))
        }
      } finally {
        if (isMounted) setIsLoadingMeta(false)
      }
    }

    fetchAssets()
    return () => {
      isMounted = false
    }
  }, [])

  const seatIds = state?.seatIds ?? []
  const seatNumbers = state?.seatNumbers ?? []
  // 예매 생성(Step 1) 전이라 백엔드 확정 금액은 아직 없음 -> 좌석 수 × 단가로 우선 표시
  const totalTicketPrice = seatIds.length * SEAT_PRICE

  const coupons = assets?.coupons ?? []
  const balance = assets?.balance ?? 0

  const selectedCoupon = useMemo(
    () => coupons.find((coupon) => coupon.userCouponId === selectedCouponId) ?? null,
    [coupons, selectedCouponId],
  )
  // PERCENT 쿠폰은 티켓 총액에 비율을 곱해 원 단위로 환산하고, AMOUNT는 그대로 쓴다.
  // (calculateDiscountAmount 내부에서 총액을 넘지 않도록 상한도 같이 처리)
  const couponDiscount = selectedCoupon
    ? calculateDiscountAmount(selectedCoupon, totalTicketPrice)
    : 0

  // 쿠폰 할인 적용 후 남은 금액과 보유 포인트 중 더 작은 값이 "사용 가능한 최대 포인트"
  // (요구사항인 "티켓 총액 초과 입력 방지"를 쿠폰과 함께 적용해도 최종 금액이 음수가 되지 않도록 확장)
  const remainingAfterCoupon = Math.max(totalTicketPrice - couponDiscount, 0)
  const maxUsablePoint = Math.min(balance, remainingAfterCoupon)

  // 쿠폰을 바꿔서 사용 가능한 최대치가 줄어들면, 이미 입력해둔 포인트도 같이 줄여줌
  useEffect(() => {
    setUsePointAmount((prev) => Math.min(prev, maxUsablePoint))
  }, [maxUsablePoint])

  const finalAmount = totalTicketPrice - couponDiscount - usePointAmount

  const handlePointChange = (e: ChangeEvent<HTMLInputElement>) => {
    const digitsOnly = e.target.value.replace(/[^0-9]/g, '')
    const value = digitsOnly === '' ? 0 : Number(digitsOnly)
    setUsePointAmount(Math.min(value, maxUsablePoint))
  }

  const handleUseAllPoints = () => setUsePointAmount(maxUsablePoint)

  const handleCouponChange = (e: ChangeEvent<HTMLSelectElement>) => {
    setSelectedCouponId(e.target.value ? Number(e.target.value) : null)
  }

  const handlePayment = async () => {
    if (!state || seatIds.length === 0 || finalAmount < 0) return

    setIsPaying(true)
    //롤백을 위해 생성된 예약번호를 임시 저장할 변수
    let createdReservationNo: string | null = null;

    try {
      // Step 1: 예매 대기 생성 (선택 좌석 락)
      const reservation = await createReservation({
        scheduleId: state.scheduleId,
        seatIds,
      })
      createdReservationNo = reservation.reservationNo; //예약번호 저장

      // Step 2: 포트원 V2 결제창 호출 (더미 storeId/channelKey 사용)
      // 쿠폰/포인트로 전액 충당되어 0원이면 PG 호출 없이 바로 검증 단계로 넘어감
      // (실제 서비스에서는 PG사의 0원 결제 정책에 맞춘 별도 무료 승인 플로우를 쓰는 것이 일반적)
      let paymentId: string
      if (finalAmount === 0) {
        paymentId = `free-${crypto.randomUUID()}`
      } else {
        const paymentResult = await requestPortOnePayment({
          paymentId: `payment-${crypto.randomUUID()}`,
          orderName: state.movieTitle
            ? `${state.movieTitle} · ${seatNumbers.join(', ')}`
            : `영화 예매 · 좌석 ${seatIds.length}석`,
          totalAmount: finalAmount,
        })

        if (paymentResult.code != null) {
          // 사용자가 결제창을 닫았거나 PG 단에서 실패한 경우
          await cancelReservation(createdReservationNo).catch((err) => {
              console.error("예매 롤백 실패:", err);});

          setErrorMessage(paymentResult.message ?? '결제가 취소되었습니다.')
          return
        }
        paymentId = paymentResult.paymentId
      }

      // Step 3: 백엔드 결제 검증 (사용한 쿠폰/포인트 정보 포함)
      const verifyResult = await verifyPayment({
        reservationId: reservation.reservationId,
        paymentId,
        userCouponId: selectedCouponId,
        usePointAmount,
        payMethod: 'CARD',
      })

      // Step 4: 검증 성공 -> 예매 완료 페이지로 이동
      navigate('/success', {
        replace: true,
        state: {
          reservationNo: verifyResult.reservationNo,
          finalAmount: verifyResult.finalAmount,
        },
      })
    } catch (error) {
        //기타 에러 발생 시: 예약번호가 있다면 롤백(취소) API 즉시 호출
        if (createdReservationNo) {
            await cancelReservation(createdReservationNo).catch((err) => {
                console.error("예매 롤백 실패:", err);
            });
        }
      // Step 4: 실패 -> 에러 모달 노출 (확인 시 이전 페이지로 이동)
      setErrorMessage(getErrorMessage(error, '결제 처리 중 오류가 발생했습니다.'))
    } finally {
      setIsPaying(false)
    }
  }

  const handleErrorModalConfirm = () => {
    setErrorMessage(null)
    navigate(-1)
  }

  if (!state) return null // 위 useEffect가 홈으로 리다이렉트 처리

  const isPayDisabled = isPaying || isLoadingMeta || seatIds.length === 0 || finalAmount < 0

  return (
    <div className="max-w-2xl mx-auto px-6 py-12 pb-56 sm:pb-48">
      {/* 예매 정보 확인 - 영수증 스타일 */}
      <ReceiptPanel className="mb-8">
        <p className="font-mono text-xs tracking-widest text-reel-gold mb-1">*** BOOKING RECEIPT ***</p>

        {state.movieTitle && (
          <h1 className="font-mono text-xl font-bold text-ink-light mb-2">
            {state.movieTitle}
          </h1>
        )}

        {state.theaterName && state.startTime && state.endTime && (
          <p className="font-mono text-xs text-ink-muted mb-4">
            {state.theaterName} · {formatTime(state.startTime)} ~ {formatTime(state.endTime)}
          </p>
        )}

        <AsciiRule char="-" className="text-xs text-ink-muted/50 my-3" />

        <div className="flex items-center justify-between font-mono text-sm mb-2">
          <span className="text-ink-muted">SEAT</span>
          <span className="text-ink-light font-medium">{seatNumbers.join(', ') || '-'}</span>
        </div>
        <div className="flex items-center justify-between font-mono text-sm">
          <span className="text-ink-muted">PRICE</span>
          <span className="text-ink-light font-medium">
            {SEAT_PRICE.toLocaleString('ko-KR')}원 × {seatIds.length}
          </span>
        </div>
      </ReceiptPanel>

      {/* 할인 수단 (쿠폰 / 포인트) - 영수증 스타일 */}
      <ReceiptPanel>
        <p className="font-mono text-xs tracking-widest text-reel-gold mb-4">*** DISCOUNTS ***</p>

        {isLoadingMeta ? (
          <div className="flex flex-col gap-3">
            <div className="h-11 rounded-lg bg-marquee-bg animate-pulse" />
            <div className="h-11 rounded-lg bg-marquee-bg animate-pulse" />
          </div>
        ) : (
          <div className="flex flex-col gap-6">
            {assetsError && (
              <p className="font-mono text-xs text-curtain-red -mt-2">
                {assetsError} (할인 없이 결제를 진행할 수 있습니다)
              </p>
            )}

            {/* 쿠폰 */}
            <div>
              <label htmlFor="coupon" className="block font-mono text-xs text-ink-muted mb-1.5">
                COUPON
              </label>
              <select
                id="coupon"
                value={selectedCouponId ?? ''}
                onChange={handleCouponChange}
                className="form-input"
              >
                <option value="">
                  {coupons.length > 0
                    ? `쿠폰을 선택하세요 (${coupons.length}장 보유)`
                    : '보유한 쿠폰이 없습니다'}
                </option>
                {coupons.map((coupon) => (
                  <option key={coupon.userCouponId} value={coupon.userCouponId}>
                    {coupon.name} ({formatDiscountLabel(coupon)})
                  </option>
                ))}
              </select>
            </div>

            {/* 포인트 */}
            <div>
              <label htmlFor="point" className="block font-mono text-xs text-ink-muted mb-1.5">
                POINT (보유 {balance.toLocaleString('ko-KR')}P)
              </label>
              <div className="flex gap-2">
                <input
                  id="point"
                  type="text"
                  inputMode="numeric"
                  value={usePointAmount === 0 ? '' : usePointAmount}
                  onChange={handlePointChange}
                  placeholder="0"
                  className="form-input"
                />
                <button
                  type="button"
                  onClick={handleUseAllPoints}
                  disabled={maxUsablePoint === 0}
                  className="shrink-0 font-mono text-xs px-4 rounded-lg border border-marquee-line
                    text-ink-light hover:border-reel-gold transition-colors
                    disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  전액 사용
                </button>
              </div>
              <p className="font-mono text-[11px] text-ink-muted mt-1.5">
                최대 {maxUsablePoint.toLocaleString('ko-KR')}P까지 사용 가능
              </p>
            </div>
          </div>
        )}
      </ReceiptPanel>

      {/* 하단 고정 결제 바 */}
      <div className="fixed bottom-0 left-0 right-0 z-40 bg-marquee-card border-t border-marquee-line">
        <div className="max-w-2xl mx-auto px-6 py-4">
          <div className="flex flex-col gap-1 mb-3 font-mono text-xs text-ink-muted">
            <div className="flex items-center justify-between">
              <span>총 티켓 금액</span>
              <span>{totalTicketPrice.toLocaleString('ko-KR')}원</span>
            </div>
            {couponDiscount > 0 && (
              <div className="flex items-center justify-between text-curtain-red">
                <span>쿠폰 할인</span>
                <span>-{couponDiscount.toLocaleString('ko-KR')}원</span>
              </div>
            )}
            {usePointAmount > 0 && (
              <div className="flex items-center justify-between text-curtain-red">
                <span>포인트 사용</span>
                <span>-{usePointAmount.toLocaleString('ko-KR')}원</span>
              </div>
            )}
          </div>

          <div className="flex items-center justify-between gap-4">
            <div className="min-w-0">
              <p className="font-mono text-xs text-ink-muted mb-0.5">최종 결제 금액</p>
              <p className="font-display text-2xl tracking-marquee text-reel-gold">
                {finalAmount.toLocaleString('ko-KR')}원
              </p>
            </div>
            <button
              type="button"
              disabled={isPayDisabled}
              onClick={handlePayment}
              className="shrink-0 bg-reel-gold hover:bg-reel-goldDark text-marquee-bg font-body font-semibold
                rounded-lg px-6 sm:px-8 py-3 transition-colors disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-reel-gold"
            >
              {isPaying ? '결제 처리 중...' : `${finalAmount.toLocaleString('ko-KR')}원 결제하기`}
            </button>
          </div>
        </div>
      </div>

      {/* 결제 실패 에러 모달 */}
      {errorMessage && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-6">
          <ReceiptPanel className="w-full max-w-sm text-center">
            <p className="font-mono text-xs tracking-widest text-curtain-red mb-2">
              *** PAYMENT FAILED ***
            </p>
            <h2 className="font-mono text-xl font-bold text-ink-light mb-3">
              결제에 실패했습니다
            </h2>
            <p className="font-mono text-sm text-ink-muted mb-6 break-keep">{errorMessage}</p>
            <button
              type="button"
              onClick={handleErrorModalConfirm}
              className="btn-primary"
            >
              확인
            </button>
          </ReceiptPanel>
        </div>
      )}
    </div>
  )
}

export default CheckoutPage
