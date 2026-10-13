import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'

interface AuthContextValue {
  isLoggedIn: boolean
  login: (accessToken: string) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(
    () => !!localStorage.getItem('accessToken'),
  )

  const login = (accessToken: string) => {
    localStorage.setItem('accessToken', accessToken)
    setIsLoggedIn(true)
  }

  const logout = () => {
    localStorage.removeItem('accessToken')
    setIsLoggedIn(false)
  }

  const value = useMemo(() => ({ isLoggedIn, login, logout }), [isLoggedIn])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.')
  }
  return ctx
}
