import { Link } from 'react-router'

export default function ForbiddenPage() {
  return (
    <div className="py-20 text-center">
      <h1 className="text-5xl font-bold text-gray-300">403</h1>
      <p className="mt-4 text-gray-600">Bạn không có quyền truy cập trang này.</p>
      <Link to="/" className="mt-6 inline-block text-brand-600 hover:underline">
        Về trang chủ
      </Link>
    </div>
  )
}
