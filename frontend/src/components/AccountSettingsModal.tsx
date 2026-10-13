import { useState, type FormEvent } from 'react'
import { updateMyProfile, changeMyPassword } from '../api/user'

type SettingsTab = 'PROFILE' | 'PASSWORD'

interface AccountSettingsModalProps {
  onClose: () => void
  initialName: string
  initialPhoneNumber: string
  // 프로필 조회(GET) 전용 API가 없어서, 수정 성공 시 MyPage가 화면에 즉시
  // 반영할 수 있도록 입력했던 값을 그대로 돌려준다 (재조회 대신 상태 업데이트).
  onProfileUpdated: (values: { name: string; phoneNumber: string }) => void
}

const tabButtonClass = (isActive: boolean) =>
  `flex-1 font-body text-sm px-4 py-2 rounded-lg border transition-colors ${
    isActive
      ? 'bg-reel-gold text-marquee-bg border-reel-gold font-semibold'
      : 'border-marquee-line text-ink-light hover:border-reel-gold'
  }`

const AccountSettingsModal = ({
  onClose,
  initialName,
  initialPhoneNumber,
  onProfileUpdated,
}: AccountSettingsModalProps) => {
  const [tab, setTab] = useState<SettingsTab>('PROFILE')

  const [name, setName] = useState(initialName)
  const [phoneNumber, setPhoneNumber] = useState(initialPhoneNumber)
  const [isSavingProfile, setIsSavingProfile] = useState(false)

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [isSavingPassword, setIsSavingPassword] = useState(false)

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

  const handleSubmitProfile = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!name.trim() || !phoneNumber.trim()) return

    setIsSavingProfile(true)
    try {
      const trimmedName = name.trim()
      const trimmedPhone = phoneNumber.trim()
      await updateMyProfile({ name: trimmedName, phoneNumber: trimmedPhone })
      alert('내 정보가 수정되었습니다.')
      onProfileUpdated({ name: trimmedName, phoneNumber: trimmedPhone })
      onClose()
    } catch {
      // 에러 메시지 자체는 api/axios.ts의 전역 인터셉터가 alert로 이미 띄워준다.
    } finally {
      setIsSavingProfile(false)
    }
  }

  const handleSubmitPassword = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!currentPassword || !newPassword) return

    setIsSavingPassword(true)
    try {
      await changeMyPassword({ currentPassword, newPassword })
      alert('비밀번호가 변경되었습니다.')
      onClose()
    } catch {
      // 에러 메시지 자체는 api/axios.ts의 전역 인터셉터가 alert로 이미 띄워준다.
    } finally {
      setIsSavingPassword(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-6"
      onClick={onClose}
    >
      <div
        className="ticket-card w-full max-w-md px-8 py-8"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-display text-2xl tracking-marquee text-ink-light">정보 수정</h2>
          <button
            type="button"
            onClick={onClose}
            className="font-mono text-xs text-ink-muted hover:text-ink-light transition-colors"
          >
            닫기 ✕
          </button>
        </div>

        <div className="flex gap-2 mb-6">
          <button
            type="button"
            onClick={() => setTab('PROFILE')}
            className={tabButtonClass(tab === 'PROFILE')}
          >
            기본 정보 수정
          </button>
          <button
            type="button"
            onClick={() => setTab('PASSWORD')}
            className={tabButtonClass(tab === 'PASSWORD')}
          >
            비밀번호 변경
          </button>
        </div>

        {tab === 'PROFILE' ? (
          <form onSubmit={handleSubmitProfile} className="flex flex-col gap-4">
            <div>
              <label htmlFor="settings-name" className="block font-mono text-xs text-ink-muted mb-1.5">
                이름
              </label>
              <input
                id="settings-name"
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="form-input"
              />
            </div>
            <div>
              <label htmlFor="settings-phone" className="block font-mono text-xs text-ink-muted mb-1.5">
                전화번호
              </label>
              <input
                id="settings-phone"
                type="tel"
                required
                value={phoneNumber}
                onChange={handlePhoneChange}
                maxLength={13}
                placeholder="010-0000-0000"
                className="form-input"
              />
            </div>
            <button type="submit" disabled={isSavingProfile} className="btn-primary mt-2">
              {isSavingProfile ? '저장 중...' : '수정 완료'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleSubmitPassword} className="flex flex-col gap-4">
            <div>
              <label
                htmlFor="settings-current-password"
                className="block font-mono text-xs text-ink-muted mb-1.5"
              >
                현재 비밀번호
              </label>
              <input
                id="settings-current-password"
                type="password"
                required
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                className="form-input"
              />
            </div>
            <div>
              <label
                htmlFor="settings-new-password"
                className="block font-mono text-xs text-ink-muted mb-1.5"
              >
                새 비밀번호
              </label>
              <input
                id="settings-new-password"
                type="password"
                required
                minLength={8}
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="8자 이상 입력해 주세요"
                className="form-input"
              />
            </div>
            <button type="submit" disabled={isSavingPassword} className="btn-primary mt-2">
              {isSavingPassword ? '변경 중...' : '수정 완료'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}

export default AccountSettingsModal
