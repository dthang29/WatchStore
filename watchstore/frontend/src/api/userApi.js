import axiosClient from './axiosClient'

export const userApi = {
  getMe: () => axiosClient.get('/users/me').then((res) => res.data),
  updateMe: (payload) => axiosClient.put('/users/me', payload).then((res) => res.data),

  // Admin
  getAll: (params) => axiosClient.get('/admin/users', { params }).then((res) => res.data),
}
