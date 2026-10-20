import axiosClient from './axiosClient'

// Danh sách trả về dạng Page của Spring: { content, totalPages, totalElements, number }
export const productApi = {
  getAll: (params) => axiosClient.get('/products', { params }).then((res) => res.data),
  getById: (id) => axiosClient.get(`/products/${id}`).then((res) => res.data),

  // Admin
  create: (payload) => axiosClient.post('/admin/products', payload).then((res) => res.data),
  update: (id, payload) =>
    axiosClient.put(`/admin/products/${id}`, payload).then((res) => res.data),
  remove: (id) => axiosClient.delete(`/admin/products/${id}`),
}
