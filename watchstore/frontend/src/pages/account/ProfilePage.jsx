import { useEffect, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import PageTitle from '../../components/common/PageTitle'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Spinner from '../../components/ui/Spinner'
import { useMe, useUpdateProfile } from '../../hooks/useAuth'
import { profileSchema } from '../../schemas/authSchema'
import { getErrorMessage } from '../../utils/error'

export default function ProfilePage() {
  const { data: me, isLoading } = useMe()
  const updateProfile = useUpdateProfile()
  const [saved, setSaved] = useState(false)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(profileSchema),
    defaultValues: { fullName: '', phone: '', address: '' },
  })

  useEffect(() => {
    if (!me) return
    reset({
      fullName: me.fullName ?? '',
      phone: me.phone ?? '',
      address: me.address ?? '',
    })
  }, [me, reset])

  if (isLoading) return <Spinner />

  const onSubmit = (values) => {
    setSaved(false)
    updateProfile.mutate(values, { onSuccess: () => setSaved(true) })
  }

  return (
    <div className="max-w-xl">
      <PageTitle>Thông tin giao hàng</PageTitle>
      <p className="mb-6 text-sm text-gray-600">
        Thông tin này sẽ được dùng để điền nhanh khi bạn đặt hàng.
      </p>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 rounded-lg bg-white p-6 shadow-sm">
        <Input label="Email" value={me?.email ?? ''} disabled readOnly />
        <Input
          label="Họ và tên"
          autoComplete="name"
          error={errors.fullName}
          {...register('fullName')}
        />
        <Input
          label="Số điện thoại"
          type="tel"
          inputMode="tel"
          autoComplete="tel"
          placeholder="Ví dụ: 0912345678"
          error={errors.phone}
          {...register('phone')}
        />
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-gray-700">Địa chỉ giao hàng</span>
          <textarea
            rows={3}
            autoComplete="street-address"
            className={`w-full rounded-md border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-brand-500 ${
              errors.address ? 'border-red-500' : 'border-gray-300'
            }`}
            {...register('address')}
          />
          {errors.address && <span className="mt-1 block text-xs text-red-600">{errors.address.message}</span>}
        </label>

        {updateProfile.isError && (
          <p className="text-sm text-red-600">{getErrorMessage(updateProfile.error)}</p>
        )}
        {saved && <p className="text-sm text-green-700">Đã lưu thông tin giao hàng.</p>}

        <Button type="submit" loading={updateProfile.isPending}>
          Lưu thay đổi
        </Button>
      </form>
    </div>
  )
}
