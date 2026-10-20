import { useEffect } from 'react'
import { refreshAccessToken } from '../api/axiosClient'
import { useAuthStore } from '../store/authStore'

// Khi mở hoặc F5 trang: dùng cookie refresh token để lấy lại access token
export function useAuthInit() {
  useEffect(() => {
    refreshAccessToken().catch(() => useAuthStore.getState().clearAuth())
  }, [])
}
