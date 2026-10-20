import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { productApi } from '../api/productApi'

export const productKeys = {
  all: ['products'],
  list: (params) => ['products', 'list', params],
  detail: (id) => ['products', 'detail', id],
}

export function useProducts(params) {
  return useQuery({
    queryKey: productKeys.list(params),
    queryFn: () => productApi.getAll(params),
    placeholderData: keepPreviousData,
  })
}

export function useProduct(id) {
  return useQuery({
    queryKey: productKeys.detail(id),
    queryFn: () => productApi.getById(id),
    enabled: Boolean(id),
  })
}

export function useDeleteProduct() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: productApi.remove,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: productKeys.all }),
  })
}
