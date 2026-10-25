import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { useForgotPassword, useResetPassword } from '../../hooks/useAuth'
import { resetPasswordSchema } from '../../schemas/authSchema'
import { getErrorMessage } from '../../utils/error'

export default function ResetPasswordPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const reset = useResetPassword()
  const resend = useForgotPassword()
  const [resendMessage, setResendMessage] = useState('')
  const {
    register,
    handleSubmit,
    getValues,
    trigger,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { email: location.state?.email ?? '', code: '', newPassword: '', confirmPassword: '' },
  })

  const onSubmit = ({ email, code, newPassword }) =>
    reset.mutate({ email, code, newPassword }, {
      onSuccess: () => navigate('/login', { replace: true, state: { passwordReset: true } }),
    })

  const onResend = async () => {
    setResendMessage('')
    if (!(await trigger('email'))) return
    resend.mutate({ email: getValues('email') }, {
      onSuccess: () => setResendMessage('Nếu email có tài khoản dùng mật khẩu, mã OTP mới đã được gửi.'),
    })
  }

  return (
    <div className="mx-auto max-w-md rounded-lg bg-white p-8 shadow-sm">
      <h1 className="mb-3 text-center text-2xl font-semibold">Đặt lại mật khẩu</h1>
      <p className="mb-6 text-center text-sm text-gray-600">
        Nếu email có tài khoản dùng mật khẩu, hãy kiểm tra hộp thư để lấy mã OTP. Mã có hiệu lực 10 phút.
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input label="Email" type="email" autoComplete="email" error={errors.email} {...register('email')} />
        <Input label="Mã OTP" inputMode="numeric" autoComplete="one-time-code" maxLength={6}
          error={errors.code} {...register('code')} />
        <Input label="Mật khẩu mới" type="password" autoComplete="new-password"
          error={errors.newPassword} {...register('newPassword')} />
        <Input label="Xác nhận mật khẩu mới" type="password" autoComplete="new-password"
          error={errors.confirmPassword} {...register('confirmPassword')} />
        {reset.isError && <p role="alert" className="text-sm text-red-600">{getErrorMessage(reset.error)}</p>}
        <Button type="submit" loading={reset.isPending} className="w-full">Đặt lại mật khẩu</Button>
      </form>
      <div className="mt-4 text-center text-sm">
        <button type="button" onClick={onResend} disabled={resend.isPending}
          className="text-brand-600 hover:underline disabled:opacity-50">
          {resend.isPending ? 'Đang gửi...' : 'Gửi lại mã OTP'}
        </button>
        {resendMessage && <p role="status" className="mt-2 text-green-700">{resendMessage}</p>}
        {resend.isError && <p role="alert" className="mt-2 text-red-600">{getErrorMessage(resend.error)}</p>}
      </div>
      <p className="mt-6 text-center text-sm">
        <Link to="/login" className="text-brand-600 hover:underline">Quay lại đăng nhập</Link>
      </p>
    </div>
  )
}
