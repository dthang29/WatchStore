import { useQueries } from '@tanstack/react-query'
import { productApi } from '../api/productApi'
import { productKeys } from './useProducts'

export function useCartStock(items) {
  const queries = useQueries({
    queries: items.map((item) => ({
      queryKey: productKeys.detail(String(item.id)),
      queryFn: () => productApi.getById(item.id),
      staleTime: 0,
      retry: 1,
    })),
  })

  const getStock = (item) => {
    const index = items.findIndex((candidate) => candidate.id === item.id)
    return queries[index]?.data?.stock ?? item.stock
  }

  const isCheckingStock = queries.some((query) => query.isPending || query.isFetching)
  const hasStockCheckError = queries.some((query) => query.isError)
  const unavailableItems = items.filter((item) => {
    const stock = getStock(item)
    return stock !== undefined && item.quantity > stock
  })

  const refreshAndValidate = async () => {
    const results = await Promise.all(queries.map((query) => query.refetch()))
    if (results.some((result) => result.isError || !result.data)) {
      return { valid: false, reason: 'Không thể kiểm tra tồn kho. Vui lòng thử lại.' }
    }

    const unavailable = items.filter((item, index) => item.quantity > results[index].data.stock)
    if (unavailable.length > 0) {
      return { valid: false, reason: 'Giỏ hàng có sản phẩm đã hết hoặc không đủ số lượng.' }
    }

    return { valid: true }
  }

  return {
    getStock,
    hasStockCheckError,
    isCheckingStock,
    refreshAndValidate,
    unavailableItems,
  }
}
