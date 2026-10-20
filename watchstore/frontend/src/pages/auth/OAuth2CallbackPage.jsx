import { useEffect } from 'react'
import { useNavigate } from 'react-router'
import { refreshAccessToken } from '../../api/axiosClient'
import Spinner from '../../components/ui/Spinner'
import { useAuthStore } from '../../store/authStore'

// OAuth2SuccessHandler ở backend set cookie refresh token rồi redirect về /oauth2/callback.
// Token KHÔNG được truyền qua URL (tránh lộ trong history/log), frontend tự gọi /auth/refresh.
export default function OAuth2CallbackPage() {
  const navigate = useNavigate()

  useEffect(() => {
    refreshAccessToken()
      .then(() => navigate('/', { replace: true }))
      .catch(() => {
        useAuthStore.getState().clearAuth()
        navigate('/login?error=oauth2', { replace: true })
      })
  }, [navigate])

  return <Spinner fullScreen />
}
