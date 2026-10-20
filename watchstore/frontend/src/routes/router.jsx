import { createBrowserRouter } from 'react-router'
import { ROLES } from '../config/constants'
import AdminLayout from '../layouts/AdminLayout'
import MainLayout from '../layouts/MainLayout'
import OrdersPage from '../pages/account/OrdersPage'
import ProfilePage from '../pages/account/ProfilePage'
import LoginPage from '../pages/auth/LoginPage'
import OAuth2CallbackPage from '../pages/auth/OAuth2CallbackPage'
import RegisterPage from '../pages/auth/RegisterPage'
import CartPage from '../pages/cart/CartPage'
import CheckoutPage from '../pages/checkout/CheckoutPage'
import ForbiddenPage from '../pages/errors/ForbiddenPage'
import NotFoundPage from '../pages/errors/NotFoundPage'
import HomePage from '../pages/home/HomePage'
import ProductDetailPage from '../pages/products/ProductDetailPage'
import ProductListPage from '../pages/products/ProductListPage'
import GuestRoute from './GuestRoute'
import ProtectedRoute from './ProtectedRoute'

// Trang admin tách chunk riêng: khách thường không phải tải code quản trị
const lazyPage = (importer) => async () => ({ Component: (await importer()).default })

export const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [
      // Public
      { index: true, element: <HomePage /> },
      { path: 'products', element: <ProductListPage /> },
      { path: 'products/:id', element: <ProductDetailPage /> },
      { path: 'cart', element: <CartPage /> },
      { path: 'oauth2/callback', element: <OAuth2CallbackPage /> },
      { path: '403', element: <ForbiddenPage /> },

      // Chỉ dành cho khách chưa đăng nhập
      {
        element: <GuestRoute />,
        children: [
          { path: 'login', element: <LoginPage /> },
          { path: 'register', element: <RegisterPage /> },
        ],
      },

      // Cần đăng nhập
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'checkout', element: <CheckoutPage /> },
          { path: 'account/profile', element: <ProfilePage /> },
          { path: 'account/orders', element: <OrdersPage /> },
        ],
      },

      { path: '*', element: <NotFoundPage /> },
    ],
  },

  // Khu quản trị: cần ROLE_ADMIN
  {
    path: 'admin',
    element: <ProtectedRoute roles={[ROLES.ADMIN]} />,
    children: [
      {
        element: <AdminLayout />,
        children: [
          { index: true, lazy: lazyPage(() => import('../pages/admin/DashboardPage')) },
          { path: 'products', lazy: lazyPage(() => import('../pages/admin/ManageProductsPage')) },
          { path: 'orders', lazy: lazyPage(() => import('../pages/admin/ManageOrdersPage')) },
          { path: 'users', lazy: lazyPage(() => import('../pages/admin/ManageUsersPage')) },
        ],
      },
    ],
  },
])
