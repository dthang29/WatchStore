const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
})

export const formatPrice = (value) => currencyFormatter.format(value ?? 0)

export const formatDate = (value) => (value ? new Date(value).toLocaleString('vi-VN') : '')
