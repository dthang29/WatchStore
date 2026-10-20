import { Link } from 'react-router'
import Spinner from '../../components/ui/Spinner'
import ProductGrid from '../../components/product/ProductGrid'
import { useProducts } from '../../hooks/useProducts'

export default function HomePage() {
  const { data, isLoading } = useProducts({ page: 0, size: 8, sort: 'createdAt,desc' })

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
        <h2 className="mb-4 text-2xl font-semibold">Sản phẩm mới</h2>
        {isLoading ? <Spinner /> : <ProductGrid products={data?.content} />}
      </section>
    </div>
  )
}
