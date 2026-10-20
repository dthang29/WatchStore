import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60 * 1000,
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        const status = error?.response?.status
        // Không retry lỗi phía client (400, 401, 403, 404...)
        if (status && status < 500) return false
        return failureCount < 2
      },
    },
  },
})
