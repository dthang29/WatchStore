import ProductCard from './ProductCard'

export default function ProductGrid({ products }) {
  if (!products?.length) {
    return <p className="py-10 text-center text-gray-500">Không có sản phẩm nào.</p>
  }

  return (
    <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  )
}
