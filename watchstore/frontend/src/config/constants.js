export const API_URL = import.meta.env.VITE_API_URL ?? '/api'
export const BACKEND_URL = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080'

// Khớp với users.role ENUM('CUSTOMER','ADMIN'), backend thêm tiền tố ROLE_ của Spring Security
export const ROLES = {
  CUSTOMER: 'ROLE_CUSTOMER',
  ADMIN: 'ROLE_ADMIN',
}

export const OAUTH2_LOGIN_URL = {
  google: `${BACKEND_URL}/oauth2/authorization/google`,
}

export const ORDER_STATUS = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  SHIPPING: 'Đang giao',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã huỷ',
}

export const PAGE_SIZE = 30
