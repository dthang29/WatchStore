import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { useRegister } from '../../hooks/useAuth'
import { registerSchema } from '../../schemas/authSchema'
import { getErrorMessage } from '../../utils/error'

export default function RegisterPage() {
  const registerMutation = useRegister()
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(registerSchema) })

  const onSubmit = ({ fullName, email, password }) =>
    registerMutation.mutate({ fullName, email, password }, {
      onSuccess: () => navigate('/verify-email', { replace: true, state: { email } }),
    })

  return (
    <div className="mx-auto max-w-md rounded-lg bg-white p-8 shadow-sm">
      <h1 className="mb-6 text-center text-2xl font-semibold">Tạo tài khoản</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input label="Họ tên" error={errors.fullName} {...register('fullName')} />
        <Input label="Email" type="email" error={errors.email} {...register('email')} />
        <Input
          label="Mật khẩu"
          type="password"
          autoComplete="new-password"
          error={errors.password}
          {...register('password')}
        />
        <Input
          label="Xác nhận mật khẩu"
          type="password"
          autoComplete="new-password"
          error={errors.confirmPassword}
          {...register('confirmPassword')}
        />
        {registerMutation.isError && (
          <p className="text-sm text-red-600">{getErrorMessage(registerMutation.error)}</p>
        )}
        <Button type="submit" loading={registerMutation.isPending} className="w-full">
          Đăng ký
        </Button>
      </form>
      <p className="mt-6 text-center text-sm">
        Đã có tài khoản?{' '}
        <Link to="/login" className="text-brand-600 hover:underline">
          Đăng nhập
        </Link>
      </p>
    </div>
  )
}
