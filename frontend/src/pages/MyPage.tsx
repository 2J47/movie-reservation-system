import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AxiosError } from 'axios'
import { getMyAssets } from '../api/asset'
import { getMyReservations, cancelReservation } from '../api/reservation'
import { deleteMyAccount, getMyProfile } from '../api/user'
import type { UserProfile } from '../types/user'
import type { UserAssets } from '../types/asset'
import type { ReservationHistoryItem, ReservationStatus } from '../types/reservationHistory'
import type { ApiErrorResponse } from '../types/auth'
import { formatDateTime } from '../utils/date'
import { useAuth } from '../context/AuthContext'
import AccountSettingsModal from '../components/AccountSettingsModal'
import ReceiptPanel from '../components/ReceiptPanel'
import AsciiRule from '../components/AsciiRule'

type FilterTab = 'ALL' | ReservationStatus

const FILTER_TABS: { key: FilterTab; label: string }[] = [
  { key: 'ALL', label: '전체' },
  { key: 'CONFIRMED', label: '예매 완료' },
  { key: 'CANCELED', label: '취소됨' },
]

const getErrorMessage = (error: unknown, fallback: string): string => {
  if (error instanceof AxiosError) {
    const data = error.response?.data as ApiErrorResponse | undefined
    return data?.message ?? error.message ?? fallback
  }
  if (error instanceof Error) return error.message
  return fallback
}

const MyPage = () => {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [assets, setAssets] = useState<UserAssets | null>(null)
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [reservations, setReservations] = useState<ReservationHistoryItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [filter, setFilter] = useState<FilterTab>('ALL')
  const [cancelingNo, setCancelingNo] = useState<string | null>(null)
  const [isSettingsOpen, setIsSettingsOpen] = useState(false)
  const [isDeletingAccount, setIsDeletingAccount] = useState(false)

//전화번호 입력 핸들러 함수
const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
  // 숫자만 남기고 모두 제거
  const rawValue = e.target.value.replace(/[^0-9]/g, '');
  let formatted = rawValue;

  // 길이에 따라 자동으로 하이픈(-) 추가
  if (rawValue.length > 3 && rawValue.length <= 7) {
    formatted = `${rawValue.slice(0, 3)}-${rawValue.slice(3)}`;
  } else if (rawValue.length > 7) {
    formatted = `${rawValue.slice(0, 3)}-${rawValue.slice(3, 7)}-${rawValue.slice(7, 11)}`;
  }

  // 상태 업데이트 (최대 13글자: 010-0000-0000)
  setPhoneNumber(formatted);
};

  const fetchAll = async () => {
    setIsLoading(true)
    try {
      const [profileData, assetData, reservationData] = await Promise.all([
          getMyProfile(),
          getMyAssets(),
          getMyReservations(),
      ])
      setProfile(profileData)
      setAssets(assetData)
      setReservations(reservationData)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchAll()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // 최신순 정렬 (상영일시 기준 내림차순 - 예매일시 필드가 명세에 없어 상영일시로 대체)
  const sortedReservations = useMemo(() => {
    return [...reservations].sort(
      (a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime(),
    )
  }, [reservations])

  const filteredReservations = useMemo(() => {
    if (filter === 'ALL') return sortedReservations
    return sortedReservations.filter((r) => r.status === filter)
  }, [sortedReservations, filter])

  const handleCancel = async (reservationNo: string) => {
    const confirmed = window.confirm(
      '정말 취소하시겠습니까? 결제 금액이 환불되고 사용한 포인트/쿠폰이 복구됩니다.',
    )
    if (!confirmed) return

    setCancelingNo(reservationNo)
    try {
      await cancelReservation(reservationNo)
      await fetchAll() // 취소 성공 시 목록 재조회
    } catch (error) {
      alert(getErrorMessage(error, '예매 취소에 실패했습니다. 잠시 후 다시 시도해 주세요.'))
    } finally {
      setCancelingNo(null)
    }
  }

  const handleDeleteAccount = async () => {
    const confirmed = window.confirm(
      '정말 탈퇴하시겠습니까? 보유한 포인트와 쿠폰, 예매 내역이 모두 삭제되며 복구할 수 없습니다.',
    )
    if (!confirmed) return

    setIsDeletingAccount(true)
    try {
      await deleteMyAccount()
      logout() // 로컬 토큰 제거 + 로그인 상태 갱신
      navigate('/')
    } catch (error) {
      alert(getErrorMessage(error, '회원 탈퇴에 실패했습니다. 잠시 후 다시 시도해 주세요.'))
    } finally {
      setIsDeletingAccount(false)
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-6 py-12">
      {/* 프로필 + 자산 */}
      <div className="ticket-card px-8 pt-8 pb-8 mb-10">
        <div className="ticket-notch-left" />
        <div className="ticket-notch-right" />

        <div className="flex items-start justify-between mb-1">
          <p className="font-mono text-xs tracking-marquee text-reel-gold">MY PAGE</p>
          {!isLoading && (
            <button
              type="button"
              onClick={() => setIsSettingsOpen(true)}
              className="font-mono text-xs px-3 py-1.5 rounded-md border border-marquee-line
                text-ink-light hover:border-reel-gold transition-colors"
            >
              수정
            </button>
          )}
        </div>

        {isLoading ? (
          <div className="animate-pulse flex flex-col gap-3 mt-2">
            <div className="h-6 w-1/3 rounded bg-marquee-bg" />
            <div className="h-4 w-1/2 rounded bg-marquee-bg" />
          </div>
        ) : !profile ? (
          <p className="font-mono text-sm text-ink-muted py-4">
            내 정보를 불러오지 못했습니다. 새로고침해 주세요.
          </p>
        ) : (
          <>
            <h1 className="font-display text-3xl tracking-marquee text-ink-light mb-1">
              {profile.name}
            </h1>
            <p className="font-mono text-xs text-ink-muted mb-6">{profile.email}</p>

            <div className="ticket-divider mb-6" />

            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="font-mono text-xs text-ink-muted mb-1">보유 포인트</p>
                <p className="font-display text-2xl tracking-marquee text-reel-gold">
                  {profile.balance.toLocaleString('ko-KR')}P
                </p>
              </div>
              <div>
                <p className="font-mono text-xs text-ink-muted mb-1">보유 쿠폰</p>
                <p className="font-display text-2xl tracking-marquee text-reel-gold">
                  {(assets?.coupons.length ?? 0).toLocaleString('ko-KR')}장
                </p>
              </div>
            </div>
          </>
        )}
      </div>

      {/* 예매 내역 */}
      <div>
        <p className="font-mono text-xs tracking-marquee text-reel-gold mb-2">MY TICKETS</p>
        <h2 className="font-display text-2xl tracking-marquee text-ink-light mb-5">예매 내역</h2>

        <div className="flex gap-2 mb-6">
          {FILTER_TABS.map((tab) => {
            const isActive = tab.key === filter
            return (
              <button
                key={tab.key}
                type="button"
                onClick={() => setFilter(tab.key)}
                className={`font-body text-sm px-4 py-2 rounded-lg border transition-colors ${
                  isActive
                    ? 'bg-reel-gold text-marquee-bg border-reel-gold font-semibold'
                    : 'border-marquee-line text-ink-light hover:border-reel-gold'
                }`}
              >
                {tab.label}
              </button>
            )
          })}
        </div>

        {isLoading ? (
          <div className="flex flex-col gap-4">
            {Array.from({ length: 2 }).map((_, i) => (
              <div key={i} className="h-40 rounded-xl bg-marquee-card border border-marquee-line animate-pulse" />
            ))}
          </div>
        ) : filteredReservations.length === 0 ? (
          <p className="font-body text-sm text-ink-muted py-16 text-center border border-dashed border-marquee-line rounded-xl">
            예매 내역이 없습니다.
          </p>
        ) : (
          <div className="flex flex-col gap-4">
            {filteredReservations.map((reservation) => (
              <ReservationCard
                key={reservation.reservationNo}
                reservation={reservation}
                isCanceling={cancelingNo === reservation.reservationNo}
                onCancel={() => handleCancel(reservation.reservationNo)}
              />
            ))}
          </div>
        )}
      </div>

      {/* 회원 탈퇴 - 눈에 띄지 않게 페이지 맨 하단에 배치 */}
      <div className="mt-16 text-center">
        <button
          type="button"
          disabled={isDeletingAccount}
          onClick={handleDeleteAccount}
          className="font-mono text-xs text-ink-muted/40 hover:text-ink-muted transition-colors
            disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isDeletingAccount ? '탈퇴 처리 중...' : '회원 탈퇴'}
        </button>
      </div>

      {isSettingsOpen && (
        <AccountSettingsModal
          onClose={() => setIsSettingsOpen(false)}
          initialName={profile.name}
          initialPhoneNumber={profile.phoneNumber}
          onProfileUpdated={({ name, phoneNumber }) => {
            setProfile((prev) => ({ ...prev, name, phoneNumber }))
          }}
        />
      )}
    </div>
  )
}

const STATUS_LABEL: Record<ReservationStatus, string> = {
  CONFIRMED: '예매 완료',
  CANCELED: '결제 취소됨',
}

const ReservationCard = ({
  reservation,
  isCanceling,
  onCancel,
}: {
  reservation: ReservationHistoryItem
  isCanceling: boolean
  onCancel: () => void
}) => {
  const { reservationNo, movieTitle, theaterName, startTime, seatNums, totalPrice, discountAmount, finalAmount, status } =
    reservation

  // 상영 시간이 아직 지나지 않고, 예매 완료 상태일 때만 취소 가능
  const isUpcoming = new Date(startTime).getTime() > Date.now()
  const canCancel = status === 'CONFIRMED' && isUpcoming

  // ⚠️ 명세상 status는 CONFIRMED/CANCELED 둘 뿐이지만, 결제가 완료되지 않은 채
  // 남아있는 PENDING 예매 등 예상 밖의 값이 내려올 수 있어 방어적으로 처리한다.
  // finalAmount/seatNums도 그런 미확정 건에서는 비어있을 수 있어 안전하게 폴백한다.
  const statusLabel = STATUS_LABEL[status] ?? status ?? '알 수 없음'
  const seatNumsText = seatNums && seatNums.length > 0 ? seatNums.join(', ') : '-'
  const finalAmountText =
    typeof finalAmount === 'number' ? `${finalAmount.toLocaleString('ko-KR')}원` : '금액 확정 전'

  return (
    <ReceiptPanel>
      <div className="flex items-start justify-between gap-4 mb-3">
        <div className="min-w-0">
          <p className="font-mono text-xs text-ink-muted mb-1">{reservationNo}</p>
          <h3 className="font-mono text-lg font-bold text-ink-light truncate">
            {movieTitle}
          </h3>
        </div>
        <span
          className={`shrink-0 font-mono text-xs rounded-md px-2 py-1 border ${
            status === 'CONFIRMED'
              ? 'text-reel-gold border-reel-gold/60'
              : 'text-ink-muted border-marquee-line'
          }`}
        >
          {statusLabel}
        </span>
      </div>

      <p className="font-mono text-xs text-ink-muted mb-4">
        {theaterName} · {formatDateTime(startTime)}
      </p>

      <AsciiRule char="-" className="text-xs text-ink-muted/50 mb-4" />

      <div className="flex items-center justify-between font-mono text-sm mb-2">
        <span className="text-ink-muted">SEAT</span>
        <span className="text-ink-light font-medium">{seatNumsText}</span>
      </div>

      <div className="bg-marquee-bg p-4 rounded-lg mt-2 mb-4 font-mono text-sm border border-marquee-line">
        {/* 1. 상품 금액 (원가) */}
        <div className="flex justify-between text-ink-muted mb-2">
          <span>상품 금액</span>
          <span>{typeof totalPrice === 'number' ? `${totalPrice.toLocaleString('ko-KR')}원` : '금액 확정 전'}</span>
        </div>

        {/* 2. 할인 금액 (할인이 있을 때만 표시) */}
        {typeof discountAmount === 'number' && discountAmount > 0 && (
          <div className="flex justify-between text-curtain-red mb-2">
            <span>할인 금액</span>
            <span>-{discountAmount.toLocaleString('ko-KR')}원</span>
          </div>
        )}

        <AsciiRule char="-" className="text-xs text-ink-muted/40 my-2" />

        {/* 3. 총 결제 금액 */}
        <div className="flex justify-between font-bold text-ink-light pt-1">
          <span>총 결제 금액</span>
          <span className="text-reel-gold text-base">
            {typeof finalAmount === 'number' ? `${finalAmount.toLocaleString('ko-KR')}원` : '금액 확정 전'}
          </span>
        </div>
      </div>

      {canCancel && (
        <button
          type="button"
          disabled={isCanceling}
          onClick={onCancel}
          className="w-full font-mono text-sm px-4 py-2.5 rounded-lg border border-curtain-red
            text-curtain-red hover:bg-curtain-red hover:text-ink-light transition-colors
            disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {isCanceling ? '취소 처리 중...' : '예매 취소'}
        </button>
      )}
    </ReceiptPanel>
  )
}

export default MyPage
