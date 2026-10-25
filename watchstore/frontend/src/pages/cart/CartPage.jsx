import { Link } from 'react-router'
import CartItem from '../../components/cart/CartItem'
import PageTitle from '../../components/common/PageTitle'
import { useCartStock } from '../../hooks/useCartStock'
import { selectCartTotal, useCartStore } from '../../store/cartStore'
import { formatPrice } from '../../utils/format'

export default function CartPage() {
  const items = useCartStore((state) => state.items)
  const total = useCartStore(selectCartTotal)
  const { getStock, hasStockCheckError, isCheckingStock, unavailableItems } = useCartStock(items)
  const checkoutBlocked = isCheckingStock || hasStockCheckError || unavailableItems.length > 0

  if (!items.length) {
    return (
      <div className="py-16 text-center">
        <p className="text-gray-500">Giỏ hàng đang trống.</p>
        <Link to="/products" className="mt-4 inline-block text-brand-600 hover:underline">
          Tiếp tục mua sắm
        </Link>
      </div>
    )
  }

  return (
    <div>
      <PageTitle>Giỏ hàng</PageTitle>
      <div className="rounded-lg bg-white px-6">
        {items.map((item) => (
          <CartItem
            key={item.id}
            item={item}
            stock={getStock(item)}
            checkingStock={isCheckingStock}
          />
        ))}
      </div>
      {hasStockCheckError && (
        <p className="mt-4 text-right text-sm text-red-600">
          Không thể kiểm tra tồn kho. Vui lòng tải lại trang.
        </p>
      )}
      {!hasStockCheckError && unavailableItems.length > 0 && (
        <p className="mt-4 text-right text-sm text-red-600">
          Giỏ hàng có sản phẩm đã hết hoặc không đủ số lượng. Bạn chưa thể thanh toán.
        </p>
      )}
      <div className="mt-6 flex items-center justify-end gap-6">
        <p className="text-lg">
          Tổng cộng: <span className="font-bold text-brand-700">{formatPrice(total)}</span>
        </p>
        {/* /checkout nằm trong ProtectedRoute: chưa đăng nhập sẽ được chuyển sang /login */}
        {checkoutBlocked ? (
          <span
            aria-disabled="true"
            className="cursor-not-allowed rounded-md bg-brand-600 px-6 py-3 font-medium text-white opacity-60"
          >
            {isCheckingStock ? 'Đang kiểm tra...' : 'Chưa thể thanh toán'}
          </span>
        ) : (
          <Link
            to="/checkout"
            className="rounded-md bg-brand-600 px-6 py-3 font-medium text-white hover:bg-brand-700"
          >
            Thanh toán
          </Link>
        )}
      </div>
    </div>
  )
}
