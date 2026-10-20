import { create } from 'zustand'
import { ROLES } from '../config/constants'
import { getUserFromToken } from '../utils/jwt'

// Access token chỉ nằm trong bộ nhớ (không localStorage) để giảm rủi ro XSS.
// Refresh token nằm trong cookie HttpOnly do backend set.
export const useAuthStore = create((set) => ({
  accessToken: null,
  user: null,
  // 'loading' khi đang khôi phục phiên, sau đó 'authenticated' | 'unauthenticated'
  status: 'loading',

  setAuth: ({ accessToken, user }) =>
    set({
      accessToken,
      user: user ?? getUserFromToken(accessToken),
      status: 'authenticated',
    }),

  clearAuth: () => set({ accessToken: null, user: null, status: 'unauthenticated' }),
}))

export const selectIsAuthenticated = (state) => state.status === 'authenticated'
export const selectIsAdmin = (state) => state.user?.roles?.includes(ROLES.ADMIN) ?? false
