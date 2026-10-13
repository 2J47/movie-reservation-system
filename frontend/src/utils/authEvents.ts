// axios 인터셉터는 React 트리 바깥의 순수 모듈이라 useAuth()를 직접 쓸 수 없다.
// 401을 만나면 이 이벤트를 쏘고, React 쪽(AuthEventBridge)이 이를 구독해서
// AuthContext.logout()과 라우팅을 처리하는 방식으로 느슨하게 연결한다.
export const AUTH_LOGOUT_EVENT = 'auth:logout'

export const emitAuthLogout = () => {
  window.dispatchEvent(new Event(AUTH_LOGOUT_EVENT))
}
