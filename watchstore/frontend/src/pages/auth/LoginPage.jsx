import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { OAUTH2_LOGIN_URL } from '../../config/constants'
import { useLogin } from '../../hooks/useAuth'
import { loginSchema } from '../../schemas/authSchema'
import { getErrorMessage } from '../../utils/error'

export default function LoginPage() {
  const login = useLogin()
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const from = location.state?.from?.pathname ?? '/'

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(loginSchema) })

  const onSubmit = (values) =>
    login.mutate(values, { onSuccess: () => navigate(from, { replace: true }) })

  return (
    <div className="mx-auto max-w-md rounded-lg bg-white p-8 shadow-sm">
      <h1 className="mb-6 text-center text-2xl font-semibold">Đăng nhập</h1>

      {searchParams.get('error') && (
        <p className="mb-4 rounded bg-red-50 p-3 text-sm text-red-600">
          Đăng nhập bằng mạng xã hội thất bại, vui lòng thử lại.
        </p>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input label="Email" type="email" error={errors.email} {...register('email')} />
        <Input
          label="Mật khẩu"
          type="password"
          autoComplete="current-password"
          error={errors.password}
          {...register('password')}
        />
        {login.isError && (
          <p className="text-sm text-red-600">
            {getErrorMessage(login.error, 'Email hoặc mật khẩu không đúng')}
          </p>
        )}
        <Button type="submit" loading={login.isPending} className="w-full">
          Đăng nhập
        </Button>
      </form>

      <div className="my-6 text-center text-xs uppercase text-gray-400">hoặc</div>
      <a
        href={OAUTH2_LOGIN_URL.google}
        className="block w-full rounded-md border border-gray-300 bg-white px-4 py-2 text-center text-sm font-medium hover:bg-gray-100"
      >
        Đăng nhập với Google
      </a>

      <p className="mt-6 text-center text-sm">
        Chưa có tài khoản?{' '}
        <Link to="/register" className="text-brand-600 hover:underline">
          Đăng ký
        </Link>
      </p>
    </div>
  )
}
