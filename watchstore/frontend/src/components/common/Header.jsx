import { Link, NavLink, useNavigate } from 'react-router'
import { useLogout } from '../../hooks/useAuth'
import { selectIsAdmin, selectIsAuthenticated, useAuthStore } from '../../store/authStore'
import { selectCartCount, useCartStore } from '../../store/cartStore'

const navClass = ({ isActive }) =>
  `text-sm font-medium ${isActive ? 'text-brand-600' : 'text-gray-700 hover:text-brand-600'}`

export default function Header() {
  const isAuthenticated = useAuthStore(selectIsAuthenticated)
  const isAdmin = useAuthStore(selectIsAdmin)
  const user = useAuthStore((state) => state.user)
  const cartCount = useCartStore(selectCartCount)
  const logout = useLogout()
  const navigate = useNavigate()

  const handleLogout = () => logout.mutate(undefined, { onSettled: () => navigate('/') })

  return (
    <header className="sticky top-0 z-10 border-b bg-white">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <Link to="/" className="text-xl font-bold tracking-wide text-brand-700">
          WATCH STORE
        </Link>

        <nav className="flex items-center gap-6">
          <NavLink to="/products" className={navClass}>
            Sản phẩm
          </NavLink>
          <NavLink to="/cart" className={navClass}>
            Giỏ hàng ({cartCount})
          </NavLink>

          {isAuthenticated ? (
            <>
              {isAdmin && (
                <NavLink to="/admin" className={navClass}>
                  Quản trị
                </NavLink>
              )}
              <NavLink to="/account/orders" className={navClass}>
                {user?.fullName}
              </NavLink>
              <button onClick={handleLogout} className="text-sm text-gray-500 hover:text-red-600">
                Đăng xuất
              </button>
            </>
          ) : (
            <NavLink to="/login" className={navClass}>
              Đăng nhập
            </NavLink>
          )}
        </nav>
      </div>
    </header>
  )
}
