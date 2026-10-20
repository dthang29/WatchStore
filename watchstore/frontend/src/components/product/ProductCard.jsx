import { Link } from 'react-router'
import { formatPrice } from '../../utils/format'

export default function ProductCard({ product }) {
  return (
    <Link
      to={`/products/${product.id}`}
      className="group overflow-hidden rounded-lg border bg-white transition hover:shadow-md"
    >
      <div className="aspect-square overflow-hidden bg-gray-100">
        <img
          src={product.imageUrl}
          alt={product.name}
          loading="lazy"
          className="size-full object-cover transition group-hover:scale-105"
        />
      </div>
      <div className="p-4">
        <p className="text-xs uppercase text-gray-500">{product.brand}</p>
        <h3 className="mt-1 line-clamp-2 font-medium">{product.name}</h3>
        <p className="mt-2 font-semibold text-brand-700">{formatPrice(product.price)}</p>
      </div>
    </Link>
  )
}
