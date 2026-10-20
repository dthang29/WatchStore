import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import Button from '../../components/ui/Button'
import Spinner from '../../components/ui/Spinner'
import { useProduct } from '../../hooks/useProducts'
import { useCartStore } from '../../store/cartStore'
import { formatPrice } from '../../utils/format'

export default function ProductDetailPage() {
  const { id } = useParams()
  const { data: product, isLoading, isError } = useProduct(id)
  const addItem = useCartStore((state) => state.addItem)
  const [quantity, setQuantity] = useState(1)
  const navigate = useNavigate()

  if (isLoading) return <Spinner />
  if (isError || !product) return <p className="text-red-600">Không tìm thấy sản phẩm.</p>

  const outOfStock = product.stock <= 0

  return (
    <div className="grid gap-10 md:grid-cols-2">
      <img
        src={product.imageUrl}
        alt={product.name}
        className="aspect-square w-full rounded-lg object-cover"
      />
      <div>
        <p className="text-sm uppercase text-gray-500">{product.brand}</p>
        <h1 className="mt-2 text-3xl font-semibold">{product.name}</h1>
        <p className="mt-4 text-2xl font-bold text-brand-700">{formatPrice(product.price)}</p>
        <p className="mt-6 whitespace-pre-line text-gray-700">{product.description}</p>

        <div className="mt-8 flex items-center gap-4">
          <input
            type="number"
            min={1}
            max={product.stock}
            value={quantity}
            onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
            className="w-20 rounded border px-2 py-2 text-center"
          />
          <Button disabled={outOfStock} onClick={() => addItem(product, quantity)}>
            {outOfStock ? 'Hết hàng' : 'Thêm vào giỏ'}
          </Button>
          <Button
            variant="outline"
            disabled={outOfStock}
            onClick={() => {
              addItem(product, quantity)
              navigate('/cart')
            }}
          >
            Mua ngay
          </Button>
        </div>
      </div>
    </div>
  )
}
