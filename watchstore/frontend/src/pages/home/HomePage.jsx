import { Link } from 'react-router'
import Spinner from '../../components/ui/Spinner'
import ProductGrid from '../../components/product/ProductGrid'
import { useNewArrivals, useProducts } from '../../hooks/useProducts'

export default function HomePage() {
  const { data: newArrivals, isLoading: isLoadingNewArrivals } = useNewArrivals({
    page: 0,
    size: 8,
    sort: 'lastRestockedAt,desc',
  })
  const { data: products, isLoading: isLoadingProducts } = useProducts({
    page: 0,
    size: 8,
    sort: 'createdAt,desc',
  })

  return (
    <div className="space-y-12">
      <section className="rounded-2xl bg-gray-900 px-8 py-16 text-white">
        <h1 className="text-4xl font-bold">Đồng hồ chính hãng</h1>
        <p className="mt-3 max-w-xl text-gray-300">
          Thời gian là thứ quý giá nhất. Hãy chọn chiếc đồng hồ xứng đáng với nó.
        </p>
        <Link
          to="/products"
          className="mt-6 inline-block rounded-md bg-brand-500 px-6 py-3 font-medium hover:bg-brand-600"
        >
          Mua ngay
        </Link>
      </section>

      <section>
        <div className="mb-4 flex items-center justify-between gap-4">
          <h2 className="text-2xl font-semibold">Hàng mới về</h2>
          <Link to="/new-arrivals" className="text-sm font-medium text-brand-600 hover:underline">Tất cả</Link>
        </div>
        {isLoadingNewArrivals ? <Spinner /> : <ProductGrid products={newArrivals?.content} />}
      </section>

      <section>
        <div className="mb-4 flex items-center justify-between gap-4">
          <h2 className="text-2xl font-semibold">Tất cả sản phẩm</h2>
          <Link to="/products" className="text-sm font-medium text-brand-600 hover:underline">Tất cả</Link>
        </div>
        {isLoadingProducts ? <Spinner /> : <ProductGrid products={products?.content} />}
      </section>
    </div>
  )
}
