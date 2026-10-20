import axios from 'axios'
import { API_URL } from '../config/constants'
import { useAuthStore } from '../store/authStore'

const axiosClient = axios.create({
  baseURL: API_URL,
  withCredentials: true, // gửi kèm cookie refresh token
  headers: { 'Content-Type': 'application/json' },
})

axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Dùng chung 1 promise: nhiều request cùng bị 401 chỉ gọi refresh đúng 1 lần
let refreshPromise = null

export function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post(`${API_URL}/auth/refresh`, null, { withCredentials: true })
      .then((res) => {
        useAuthStore.getState().setAuth(res.data)
        return res.data.accessToken
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const isAuthEndpoint = original?.url?.startsWith('/auth/')

    if (error.response?.status === 401 && original && !original._retry && !isAuthEndpoint) {
      original._retry = true
      try {
        const token = await refreshAccessToken()
        original.headers.Authorization = `Bearer ${token}`
        return axiosClient(original)
      } catch (refreshError) {
        useAuthStore.getState().clearAuth()
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  },
)

export default axiosClient
