import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { useForgotPassword } from '../../hooks/useAuth'
import { forgotPasswordSchema } from '../../schemas/authSchema'
import { getErrorMessage } from '../../utils/error'

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const forgot = useForgotPassword()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(forgotPasswordSchema) })

  const onSubmit = ({ email }) =>
    forgot.mutate({ email }, {
      onSuccess: () => navigate('/reset-password', { state: { email } }),
    })

  return (
    <div className="mx-auto max-w-md rounded-lg bg-white p-8 shadow-sm">
      <h1 className="mb-3 text-center text-2xl font-semibold">Quên mật khẩu</h1>
      <p className="mb-6 text-center text-sm text-gray-600">
        Nhập email tài khoản để nhận mã OTP đặt lại mật khẩu.
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input label="Email" type="email" autoComplete="email" error={errors.email} {...register('email')} />
        {forgot.isError && <p role="alert" className="text-sm text-red-600">{getErrorMessage(forgot.error)}</p>}
        <Button type="submit" loading={forgot.isPending} className="w-full">Gửi mã OTP</Button>
      </form>
      <p className="mt-6 text-center text-sm">
        <Link to="/login" className="text-brand-600 hover:underline">Quay lại đăng nhập</Link>
      </p>
    </div>
  )
}
