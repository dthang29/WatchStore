import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { orderApi } from '../api/orderApi'

export const orderKeys = {
  all: ['orders'],
  mine: (params) => ['orders', 'mine', params],
  admin: (params) => ['orders', 'admin', params],
}

export function useMyOrders(params) {
  return useQuery({ queryKey: orderKeys.mine(params), queryFn: () => orderApi.getMine(params) })
}

export function useAllOrders(params) {
  return useQuery({ queryKey: orderKeys.admin(params), queryFn: () => orderApi.getAll(params) })
}

export function useCreateOrder() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: orderApi.create,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: orderKeys.all }),
  })
}

export function useUpdateOrderStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }) => orderApi.updateStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: orderKeys.all }),
  })
}
