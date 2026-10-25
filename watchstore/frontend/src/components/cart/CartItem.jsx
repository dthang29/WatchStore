import { useCartStore } from '../../store/cartStore'
import { formatPrice } from '../../utils/format'

export default function CartItem({ item, stock, checkingStock = false }) {
  const updateQuantity = useCartStore((state) => state.updateQuantity)
  const removeItem = useCartStore((state) => state.removeItem)

  return (
    <div className="flex items-center gap-4 border-b py-4">
      <img src={item.imageUrl} alt={item.name} className="size-20 rounded object-cover" />
      <div className="flex-1">
        <p className="font-medium">{item.name}</p>
        <p className="text-sm text-gray-500">{formatPrice(item.price)}</p>
        {checkingStock ? (
          <p className="text-sm text-gray-500">Đang kiểm tra tồn kho...</p>
        ) : (
          <p className={`text-sm ${stock > 0 ? 'text-green-700' : 'font-medium text-red-600'}`}>
            {stock > 0 ? `Còn ${stock} sản phẩm` : 'Hết hàng - chưa thể thanh toán'}
          </p>
        )}
      </div>
      <input
        type="number"
        min={1}
        max={stock > 0 ? stock : undefined}
        value={item.quantity}
        onChange={(e) => {
          const nextQuantity = Math.max(1, Number(e.target.value))
          updateQuantity(item.id, stock > 0 ? Math.min(stock, nextQuantity) : nextQuantity)
        }}
        className="w-16 rounded border px-2 py-1 text-center"
      />
      <p className="w-32 text-right font-semibold">{formatPrice(item.price * item.quantity)}</p>
      <button onClick={() => removeItem(item.id)} className="text-sm text-red-600 hover:underline">
        Xoá
      </button>
    </div>
  )
}
