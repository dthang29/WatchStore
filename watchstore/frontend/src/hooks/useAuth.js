import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { authApi } from '../api/authApi'
import { userApi } from '../api/userApi'
import { selectIsAuthenticated, useAuthStore } from '../store/authStore'

export function useLogin() {
  const setAuth = useAuthStore((state) => state.setAuth)
  return useMutation({ mutationFn: authApi.login, onSuccess: setAuth })
}

export function useRegister() {
  return useMutation({ mutationFn: authApi.register })
}

export function useVerifyRegistration() {
  return useMutation({ mutationFn: authApi.verifyRegistration })
}

export function useResendRegistrationCode() {
  return useMutation({ mutationFn: authApi.resendRegistrationCode })
}

export function useForgotPassword() {
  return useMutation({ mutationFn: authApi.forgotPassword })
}

export function useResetPassword() {
  return useMutation({ mutationFn: authApi.resetPassword })
}

export function useLogout() {
  const clearAuth = useAuthStore((state) => state.clearAuth)
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: authApi.logout,
    // Dù backend lỗi vẫn xoá phiên phía client
    onSettled: () => {
      clearAuth()
      queryClient.clear()
    },
  })
}

export function useMe() {
  const isAuthenticated = useAuthStore(selectIsAuthenticated)
  return useQuery({ queryKey: ['me'], queryFn: userApi.getMe, enabled: isAuthenticated })
}

export function useUpdateProfile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: userApi.updateMe,
    onSuccess: (user) => queryClient.setQueryData(['me'], user),
  })
}
