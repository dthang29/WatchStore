import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router'
import PageTitle from '../../components/common/PageTitle'
import ProductGrid from '../../components/product/ProductGrid'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import { PAGE_SIZE } from '../../config/constants'
import { useDebounce } from '../../hooks/useDebounce'
import { useProducts } from '../../hooks/useProducts'

export default function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Number(searchParams.get('page') ?? 0)
  const [keyword, setKeyword] = useState(searchParams.get('keyword') ?? '')
  const debouncedKeyword = useDebounce(keyword)

  // Đồng bộ ô tìm kiếm lên URL để có thể chia sẻ link / F5 không mất bộ lọc
  useEffect(() => {
    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev)
        if (debouncedKeyword) next.set('keyword', debouncedKeyword)
        else next.delete('keyword')
        if (debouncedKeyword !== (prev.get('keyword') ?? '')) next.delete('page')
        return next
      },
      { replace: true },
    )
  }, [debouncedKeyword, setSearchParams])

  const { data, isLoading, isError } = useProducts({
    page,
    size: PAGE_SIZE,
    keyword: debouncedKeyword || undefined,
  })

  const goToPage = (nextPage) =>
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev)
      next.set('page', nextPage)
      return next
    })

  return (
    <div>
      <PageTitle>Tất cả sản phẩm</PageTitle>
      <input
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        placeholder="Tìm theo tên, thương hiệu..."
        className="mb-6 w-full max-w-md rounded-md border px-3 py-2 text-sm"
      />

      {isLoading && <Spinner />}
      {isError && <p className="text-red-600">Không tải được danh sách sản phẩm.</p>}
      {data && (
        <>
          <ProductGrid products={data.content} />
          <Pagination page={page} totalPages={data.totalPages} onChange={goToPage} />
        </>
      )}
    </div>
  )
}
