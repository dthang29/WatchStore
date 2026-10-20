import axiosClient from './axiosClient'

// Backend trả về { accessToken, user } và set cookie HttpOnly chứa refresh token
export const authApi = {
  login: (credentials) => axiosClient.post('/auth/login', credentials).then((res) => res.data),
  register: (payload) => axiosClient.post('/auth/register', payload).then((res) => res.data),
  logout: () => axiosClient.post('/auth/logout'),
}
