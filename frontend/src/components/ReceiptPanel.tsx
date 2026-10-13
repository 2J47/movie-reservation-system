import type { ReactNode } from 'react'
import AsciiRule from './AsciiRule'

interface ReceiptPanelProps {
  children: ReactNode
  className?: string
}

// 실제 영수증 위아래의 삐죽삐죽 뜯긴 단면을 문자로 흉내낸다.
// (모노스페이스 폰트라도 한글/영문 혼용 시 폭이 완전히 균일하진 않지만,
//  이 줄들은 순수 장식이라 정렬이 깨질 일이 없다 - 내용 줄과는 분리돼 있음.)
const ReceiptPanel = ({ children, className = '' }: ReceiptPanelProps) => (
  <div className={`bg-marquee-card border border-marquee-line font-mono ${className}`}>
    <AsciiRule char="^" className="text-[10px] text-ink-muted/40 tracking-[-0.05em] px-2 pt-2" />
    <div className="px-6 py-6">{children}</div>
    <AsciiRule char="v" className="text-[10px] text-ink-muted/40 tracking-[-0.05em] px-2 pb-2" />
  </div>
)

export default ReceiptPanel
