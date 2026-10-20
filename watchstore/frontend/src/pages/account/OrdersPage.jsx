import PageTitle from '../../components/common/PageTitle'
import Spinner from '../../components/ui/Spinner'
import { ORDER_STATUS } from '../../config/constants'
import { useMyOrders } from '../../hooks/useOrders'
import { formatDate, formatPrice } from '../../utils/format'

export default function OrdersPage() {
  const { data, isLoading } = useMyOrders({ page: 0, size: 20 })
  const orders = data?.content ?? []

  return (
    <div>
      <PageTitle>Đơn hàng của tôi</PageTitle>
      {isLoading && <Spinner />}
      {!isLoading && !orders.length && <p className="text-gray-500">Bạn chưa có đơn hàng nào.</p>}
      <div className="space-y-3">
        {orders.map((order) => (
          <div key={order.id} className="flex items-center justify-between rounded-lg bg-white p-4">
            <div>
              <p className="font-medium">Đơn #{order.id}</p>
              <p className="text-sm text-gray-500">{formatDate(order.createdAt)}</p>
            </div>
            <span className="text-sm">{ORDER_STATUS[order.status] ?? order.status}</span>
            <span className="font-semibold text-brand-700">{formatPrice(order.totalAmount)}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
