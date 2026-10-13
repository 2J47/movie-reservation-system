import * as PortOne from '@portone/browser-sdk/v2'

// ⚠️ 더미 값입니다. 포트원 콘솔에서 발급받은 실제 storeId / channelKey로 교체하세요.
const PORTONE_STORE_ID = 'store-081453cc-8b66-4728-829b-7dedb1cb3558'
const PORTONE_CHANNEL_KEY = 'channel-key-a4b7069f-4060-40ea-95bf-9fd1760a0a87'

export interface RequestPortOnePaymentParams {
  paymentId: string
  orderName: string
  totalAmount: number
}

/**
 * 포트원 V2 결제창 호출
 * 성공/실패 여부는 반환값의 `code` 필드로 판별합니다.
 * - code가 없으면(undefined) 성공 → paymentId로 백엔드 검증 진행
 * - code가 있으면 실패/취소 → message를 사용자에게 안내
 */
export const requestPortOnePayment = (params: RequestPortOnePaymentParams) => {
  return PortOne.requestPayment({
    storeId: PORTONE_STORE_ID,
    channelKey: PORTONE_CHANNEL_KEY,
    paymentId: params.paymentId,
    orderName: params.orderName,
    totalAmount: params.totalAmount,
    currency: 'CURRENCY_KRW',
    payMethod: 'CARD',
  })
}
