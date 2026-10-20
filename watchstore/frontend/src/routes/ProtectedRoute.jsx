import { Navigate, Outlet, useLocation } from 'react-router'
import Spinner from '../components/ui/Spinner'
import { useAuthStore } from '../store/authStore'

// Chặn route khi chưa đăng nhập; truyền `roles` để giới hạn theo quyền (vd: admin).
// Đây chỉ là lớp UX — backend vẫn phải kiểm tra quyền bằng Spring Security.
export default function ProtectedRoute({ roles }) {
  const status = useAuthStore((state) => state.status)
  const userRoles = useAuthStore((state) => state.user?.roles)
  const location = useLocation()

  if (status === 'loading') return <Spinner fullScreen />

  if (status !== 'authenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (roles && !roles.some((role) => userRoles?.includes(role))) {
    return <Navigate to="/403" replace />
  }

  return <Outlet />
}
