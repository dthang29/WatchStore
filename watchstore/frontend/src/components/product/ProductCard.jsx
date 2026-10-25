import { Link } from 'react-router'
import { formatPrice } from '../../utils/format'

export default function ProductCard({ product }) {
  return (
    <Link
      to={`/products/${product.id}`}
      className="group overflow-hidden rounded-lg border bg-white transition hover:shadow-md"
    >
      <div className="relative aspect-square overflow-hidden bg-gray-100">
        <img
          src={product.imageUrl}
          alt={product.name}
          loading="lazy"
          className="size-full object-cover transition group-hover:scale-105"
        />
        <span
          className={`absolute right-2 top-2 rounded-md px-2 py-1 text-xs font-semibold shadow-sm ${
            product.stock > 0 ? 'bg-white text-green-800' : 'bg-red-600 text-white'
          }`}
        >
          {product.stock > 0 ? `Tồn kho: ${product.stock}` : 'Hết hàng'}
        </span>
      </div>
      <div className="p-4">
        <p className="text-xs uppercase text-gray-500">{product.brand}</p>
        <h3 className="mt-1 line-clamp-2 font-medium">{product.name}</h3>
        {product.description && <p className="mt-1 line-clamp-2 text-sm text-gray-500">{product.description}</p>}
        <p className="mt-2 font-semibold text-brand-700">{formatPrice(product.price)}</p>
      </div>
    </Link>
  )
}
