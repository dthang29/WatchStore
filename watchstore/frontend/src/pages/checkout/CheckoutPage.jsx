import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Navigate, useNavigate } from 'react-router'
import PageTitle from '../../components/common/PageTitle'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { useCreateOrder } from '../../hooks/useOrders'
import { checkoutSchema } from '../../schemas/checkoutSchema'
import { selectCartTotal, useCartStore } from '../../store/cartStore'
import { getErrorMessage } from '../../utils/error'
import { formatPrice } from '../../utils/format'

export default function CheckoutPage() {
  const items = useCartStore((state) => state.items)
  const total = useCartStore(selectCartTotal)
  const clearCart = useCartStore((state) => state.clear)
  const createOrder = useCreateOrder()
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(checkoutSchema),
    defaultValues: { paymentMethod: 'COD' },
  })

  if (!items.length && !createOrder.isSuccess) return <Navigate to="/cart" replace />

  const onSubmit = (values) => {
    // Chỉ gửi id + số lượng; giá phải do backend tính lại, không tin giá từ client
    const payload = {
      ...values,
      items: items.map(({ id, quantity }) => ({ productId: id, quantity })),
    }
    createOrder.mutate(payload, {
      onSuccess: () => {
        clearCart()
        navigate('/account/orders', { replace: true })
      },
    })
  }

  return (
    <div className="grid gap-10 md:grid-cols-[1fr_360px]">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 rounded-lg bg-white p-6">
        <PageTitle>Thông tin giao hàng</PageTitle>
        <Input label="Người nhận" error={errors.receiverName} {...register('receiverName')} />
        <Input label="Số điện thoại" error={errors.phone} {...register('phone')} />
        <Input label="Địa chỉ" error={errors.address} {...register('address')} />
        <Input label="Ghi chú" error={errors.note} {...register('note')} />

        <fieldset className="space-y-2">
          <legend className="text-sm font-medium text-gray-700">Phương thức thanh toán</legend>
          <label className="flex items-center gap-2 text-sm">
            <input type="radio" value="COD" {...register('paymentMethod')} /> Thanh toán khi nhận
            hàng
          </label>
          <label className="flex items-center gap-2 text-sm">
            <input type="radio" value="BANK_TRANSFER" {...register('paymentMethod')} /> Chuyển
            khoản
          </label>
        </fieldset>

        {createOrder.isError && (
          <p className="text-sm text-red-600">{getErrorMessage(createOrder.error)}</p>
        )}
        <Button type="submit" loading={createOrder.isPending} className="w-full">
          Đặt hàng
        </Button>
      </form>

      <aside className="h-fit rounded-lg bg-white p-6">
        <h2 className="mb-4 font-semibold">Đơn hàng ({items.length} sản phẩm)</h2>
        {items.map((item) => (
          <div key={item.id} className="flex justify-between py-1 text-sm">
            <span>
              {item.name} × {item.quantity}
            </span>
            <span>{formatPrice(item.price * item.quantity)}</span>
          </div>
        ))}
        <div className="mt-4 flex justify-between border-t pt-4 font-bold">
          <span>Tổng</span>
          <span className="text-brand-700">{formatPrice(total)}</span>
        </div>
      </aside>
    </div>
  )
}
