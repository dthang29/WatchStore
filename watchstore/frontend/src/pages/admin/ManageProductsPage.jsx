import { useState } from 'react'
import PageTitle from '../../components/common/PageTitle'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import { useDeleteProduct, useProducts } from '../../hooks/useProducts'
import { formatPrice } from '../../utils/format'

export default function ManageProductsPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useProducts({ page, size: 20 })
  const deleteProduct = useDeleteProduct()

  const handleDelete = (product) => {
    if (window.confirm(`Xoá sản phẩm "${product.name}"?`)) deleteProduct.mutate(product.id)
  }

  return (
    <div>
      <PageTitle>Quản lý sản phẩm</PageTitle>
      {isLoading ? (
        <Spinner />
      ) : (
        <table className="w-full overflow-hidden rounded-lg bg-white text-sm">
          <thead className="bg-gray-100 text-left">
            <tr>
              <th className="p-3">ID</th>
              <th className="p-3">Tên</th>
              <th className="p-3">Thương hiệu</th>
              <th className="p-3">Giá</th>
              <th className="p-3">Tồn kho</th>
              <th className="p-3" />
            </tr>
          </thead>
          <tbody>
            {data?.content?.map((product) => (
              <tr key={product.id} className="border-t">
                <td className="p-3">{product.id}</td>
                <td className="p-3">{product.name}</td>
                <td className="p-3">{product.brand}</td>
                <td className="p-3">{formatPrice(product.price)}</td>
                <td className="p-3">{product.stock}</td>
                <td className="p-3 text-right">
                  <button
                    onClick={() => handleDelete(product)}
                    className="text-red-600 hover:underline"
                  >
                    Xoá
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
    </div>
  )
}
