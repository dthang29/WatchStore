import { Navigate, Outlet, useLocation } from 'react-router'
import Spinner from '../components/ui/Spinner'
import { useAuthStore } from '../store/authStore'

// Trang login/register: đã đăng nhập thì chuyển về trang trước đó
export default function GuestRoute() {
  const status = useAuthStore((state) => state.status)
  const location = useLocation()

  if (status === 'loading') return <Spinner fullScreen />
  if (status === 'authenticated') {
    return <Navigate to={location.state?.from?.pathname ?? '/'} replace />
  }

  return <Outlet />
}
