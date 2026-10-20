import { useCartStore } from '../../store/cartStore'
import { formatPrice } from '../../utils/format'

export default function CartItem({ item }) {
  const updateQuantity = useCartStore((state) => state.updateQuantity)
  const removeItem = useCartStore((state) => state.removeItem)

  return (
    <div className="flex items-center gap-4 border-b py-4">
      <img src={item.imageUrl} alt={item.name} className="size-20 rounded object-cover" />
      <div className="flex-1">
        <p className="font-medium">{item.name}</p>
        <p className="text-sm text-gray-500">{formatPrice(item.price)}</p>
      </div>
      <input
        type="number"
        min={1}
        value={item.quantity}
        onChange={(e) => updateQuantity(item.id, Number(e.target.value))}
        className="w-16 rounded border px-2 py-1 text-center"
      />
      <p className="w-32 text-right font-semibold">{formatPrice(item.price * item.quantity)}</p>
      <button onClick={() => removeItem(item.id)} className="text-sm text-red-600 hover:underline">
        Xoá
      </button>
    </div>
  )
}
