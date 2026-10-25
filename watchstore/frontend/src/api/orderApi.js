import axiosClient from './axiosClient'

export const orderApi = {
  create: (payload) => axiosClient.post('/orders', payload).then((res) => res.data),
  createVnPayPayment: (orderId) =>
    axiosClient.post(`/vnpay/orders/${orderId}/payment-url`).then((res) => res.data),
  getMine: (params) => axiosClient.get('/orders/me', { params }).then((res) => res.data),

  // Admin
  getAll: (params) => axiosClient.get('/admin/orders', { params }).then((res) => res.data),
  updateStatus: (id, status) =>
    axiosClient.patch(`/admin/orders/${id}/status`, { status }).then((res) => res.data),
}
