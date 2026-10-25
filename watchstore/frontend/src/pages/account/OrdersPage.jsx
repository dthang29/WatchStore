import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { orderApi } from '../../api/orderApi'
import PageTitle from '../../components/common/PageTitle'
import Spinner from '../../components/ui/Spinner'
import { ORDER_STATUS } from '../../config/constants'
import { useMyOrders } from '../../hooks/useOrders'
import { formatDate, formatPrice } from '../../utils/format'

export default function OrdersPage() {
  const [searchParams] = useSearchParams()
  const [paymentError, setPaymentError] = useState('')
  const [payingId, setPayingId] = useState(null)
  const { data, isLoading } = useMyOrders({ page: 0, size: 20 })
  const orders = data?.content ?? []
  const payment = searchParams.get('payment')

  const retryPayment = async (orderId) => {
    setPaymentError('')
    setPayingId(orderId)
    try {
      const { paymentUrl } = await orderApi.createVnPayPayment(orderId)
      window.location.assign(paymentUrl)
    } catch {
      setPaymentError('Không thể mở VNPay. Vui lòng thử lại.')
      setPayingId(null)
    }
  }

  return (
    <div>
      <PageTitle>Đơn hàng của tôi</PageTitle>
      {payment === 'success' && <p role="status">Thanh toán VNPay thành công.</p>}
      {payment === 'pending' && <p role="status">VNPay đang xác nhận thanh toán. Vui lòng tải lại trang sau ít phút.</p>}
      {payment === 'failed' && <p role="alert">Thanh toán VNPay chưa thành công.</p>}
      {payment === 'invalid' && <p role="alert">Kết quả thanh toán không hợp lệ.</p>}
      {payment === 'start-failed' && <p role="alert">Đơn hàng đã được tạo nhưng chưa mở được VNPay. Bạn có thể thử thanh toán lại bên dưới.</p>}
      {paymentError && <p role="alert">{paymentError}</p>}
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
            {order.paymentMethod === 'VNPAY' && order.paymentInProgress && (
              <span className="text-sm text-gray-500">Đang chờ VNPay xác nhận</span>
            )}
            {order.paymentMethod === 'VNPAY' && order.paymentStatus !== 'PAID' && !order.paymentInProgress && (
              <button type="button" disabled={payingId === order.id}
                onClick={() => retryPayment(order.id)} className="text-sm text-brand-700 underline">
                {payingId === order.id ? 'Đang mở VNPay...' : 'Thanh toán VNPay'}
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
