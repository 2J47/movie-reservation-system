interface AsciiRuleProps {
  char?: string
  className?: string
}

// 실제 반복 문자를 렌더링해서 "아스키 아트" 느낌을 낸다 (CSS border가 아니라 진짜 텍스트 문자).
// 컨테이너보다 훨씬 긴 문자열을 넣고 overflow-hidden으로 잘라서, 너비가 얼마든 항상 꽉 채운다.
const AsciiRule = ({ char = '-', className = '' }: AsciiRuleProps) => (
  <div
    className={`overflow-hidden whitespace-nowrap select-none font-mono leading-none ${className}`}
    aria-hidden="true"
  >
    {char.repeat(200)}
  </div>
)

export default AsciiRule
