import { useState } from 'react'
import PageTitle from '../../components/common/PageTitle'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import { ORDER_STATUS } from '../../config/constants'
import { useAllOrders, useUpdateOrderStatus } from '../../hooks/useOrders'
import { formatDate, formatPrice } from '../../utils/format'

export default function ManageOrdersPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useAllOrders({ page, size: 20, sort: 'createdAt,desc' })
  const updateStatus = useUpdateOrderStatus()

  return (
    <div>
      <PageTitle>Quản lý đơn hàng</PageTitle>
      {isLoading ? (
        <Spinner />
      ) : (
        <table className="w-full overflow-hidden rounded-lg bg-white text-sm">
          <thead className="bg-gray-100 text-left">
            <tr>
              <th className="p-3">Mã</th>
              <th className="p-3">Khách hàng</th>
              <th className="p-3">Ngày đặt</th>
              <th className="p-3">Tổng tiền</th>
              <th className="p-3">Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {data?.content?.map((order) => (
              <tr key={order.id} className="border-t">
                <td className="p-3">#{order.id}</td>
                <td className="p-3">{order.receiverName}</td>
                <td className="p-3">{formatDate(order.createdAt)}</td>
                <td className="p-3">{formatPrice(order.totalAmount)}</td>
                <td className="p-3">
                  <select
                    value={order.status}
                    onChange={(e) => updateStatus.mutate({ id: order.id, status: e.target.value })}
                    className="rounded border px-2 py-1"
                  >
                    {Object.entries(ORDER_STATUS).map(([value, label]) => (
                      <option key={value} value={value}>
                        {label}
                      </option>
                    ))}
                  </select>
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
