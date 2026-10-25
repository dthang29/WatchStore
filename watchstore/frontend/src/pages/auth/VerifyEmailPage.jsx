import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { useResendRegistrationCode, useVerifyRegistration } from '../../hooks/useAuth'
import { verifyRegistrationSchema } from '../../schemas/authSchema'
import { getErrorMessage } from '../../utils/error'

export default function VerifyEmailPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const verify = useVerifyRegistration()
  const resend = useResendRegistrationCode()
  const [resendMessage, setResendMessage] = useState('')
  const {
    register,
    handleSubmit,
    getValues,
    trigger,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(verifyRegistrationSchema),
    defaultValues: { email: location.state?.email ?? '', code: '' },
  })

  const onSubmit = (values) =>
    verify.mutate(values, {
      onSuccess: () => navigate('/login', { replace: true, state: { emailVerified: true } }),
    })

  const onResend = async () => {
    setResendMessage('')
    if (!(await trigger('email'))) return
    resend.mutate({ email: getValues('email') }, {
      onSuccess: () => setResendMessage('Mã OTP mới đã được gửi. Vui lòng kiểm tra email.'),
    })
  }

  return (
    <div className="mx-auto max-w-md rounded-lg bg-white p-8 shadow-sm">
      <h1 className="mb-3 text-center text-2xl font-semibold">Xác thực email</h1>
      <p className="mb-6 text-center text-sm text-gray-600">
        Nhập mã OTP 6 chữ số đã gửi tới email của bạn. Mã có hiệu lực trong 10 phút.
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input label="Email" type="email" error={errors.email} {...register('email')} />
        <Input label="Mã OTP" inputMode="numeric" autoComplete="one-time-code"
          maxLength={6} error={errors.code} {...register('code')} />
        {verify.isError && <p role="alert" className="text-sm text-red-600">{getErrorMessage(verify.error)}</p>}
        <Button type="submit" loading={verify.isPending} className="w-full">Xác thực tài khoản</Button>
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
