import { useSearchParams } from 'react-router'
import PageTitle from '../../components/common/PageTitle'
import ProductGrid from '../../components/product/ProductGrid'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import { PAGE_SIZE } from '../../config/constants'
import { useNewArrivals } from '../../hooks/useProducts'

export default function NewArrivalsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Number(searchParams.get('page') ?? 0)
  const { data, isLoading, isError } = useNewArrivals({
    page,
    size: PAGE_SIZE,
    sort: 'lastRestockedAt,desc',
  })

  const goToPage = (nextPage) =>
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev)
      next.set('page', nextPage)
      return next
    })

  return (
    <div>
      <PageTitle>Hàng mới về</PageTitle>
      <p className="mb-6 text-sm text-gray-600">Các sản phẩm được tạo mới hoặc nhập thêm trong 30 ngày gần nhất.</p>
      {isLoading && <Spinner />}
      {isError && <p className="text-red-600">Không tải được danh sách hàng mới về.</p>}
      {data && (
        <>
          <ProductGrid products={data.content} />
          <Pagination page={page} totalPages={data.totalPages} onChange={goToPage} />
        </>
      )}
    </div>
  )
}
