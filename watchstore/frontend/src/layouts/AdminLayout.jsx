import { Link, NavLink, Outlet } from 'react-router'

const MENU = [
  { to: '/admin', label: 'Tổng quan', end: true },
  { to: '/admin/products', label: 'Sản phẩm' },
  { to: '/admin/orders', label: 'Đơn hàng' },
  { to: '/admin/users', label: 'Người dùng' },
]

export default function AdminLayout() {
  return (
    <div className="flex min-h-screen">
      <aside className="w-60 shrink-0 bg-gray-900 text-gray-200">
        <Link to="/" className="block px-6 py-5 text-lg font-bold text-white">
          WATCH ADMIN
        </Link>
        <nav className="flex flex-col">
          {MENU.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `px-6 py-3 text-sm ${isActive ? 'bg-gray-800 text-white' : 'hover:bg-gray-800'}`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="flex-1 p-8">
        <Outlet />
      </main>
    </div>
  )
}
