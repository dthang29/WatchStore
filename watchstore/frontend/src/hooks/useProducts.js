import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { productApi } from '../api/productApi'

export const productKeys = {
  all: ['products'],
  list: (params) => ['products', 'list', params],
  newArrivals: (params) => ['products', 'new-arrivals', params],
  detail: (id) => ['products', 'detail', id],
  admin: (params) => ['admin', 'products', params],
  categories: ['admin', 'product-categories'],
}

export function useProducts(params) {
  return useQuery({
    queryKey: productKeys.list(params),
    queryFn: () => productApi.getAll(params),
    placeholderData: keepPreviousData,
  })
}

export function useNewArrivals(params) {
  return useQuery({
    queryKey: productKeys.newArrivals(params),
    queryFn: () => productApi.getNewArrivals(params),
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

export function useAdminProducts(params) {
  return useQuery({
    queryKey: productKeys.admin(params),
    queryFn: () => productApi.getAllForAdmin(params),
    placeholderData: keepPreviousData,
  })
}

export function useProductCategories() {
  return useQuery({ queryKey: productKeys.categories, queryFn: productApi.getCategories })
}

function useInvalidateProducts() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: productKeys.all })
    queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
  }
}

export function useSaveProduct() {
  const invalidate = useInvalidateProducts()
  return useMutation({
    mutationFn: ({ id, payload }) => (id ? productApi.update(id, payload) : productApi.create(payload)),
    onSuccess: invalidate,
  })
}

export function useDeleteProduct() {
  const invalidate = useInvalidateProducts()
  return useMutation({
    mutationFn: productApi.remove,
    onSuccess: invalidate,
  })
}

export function useActivateProduct() {
  const invalidate = useInvalidateProducts()
  return useMutation({ mutationFn: productApi.activate, onSuccess: invalidate })
}
