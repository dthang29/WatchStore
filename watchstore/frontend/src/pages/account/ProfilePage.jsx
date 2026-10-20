import { Link } from 'react-router'
import PageTitle from '../../components/common/PageTitle'
import Spinner from '../../components/ui/Spinner'
import { useMe } from '../../hooks/useAuth'

export default function ProfilePage() {
  const { data: me, isLoading } = useMe()

  if (isLoading) return <Spinner />

  return (
    <div className="max-w-xl">
      <PageTitle>Tài khoản của tôi</PageTitle>
      <dl className="space-y-3 rounded-lg bg-white p-6">
        <div>
          <dt className="text-sm text-gray-500">Họ tên</dt>
          <dd className="font-medium">{me?.fullName}</dd>
        </div>
        <div>
          <dt className="text-sm text-gray-500">Email</dt>
          <dd className="font-medium">{me?.email}</dd>
        </div>
      </dl>
      <Link to="/account/orders" className="mt-4 inline-block text-brand-600 hover:underline">
        Xem đơn hàng
      </Link>
    </div>
  )
}
