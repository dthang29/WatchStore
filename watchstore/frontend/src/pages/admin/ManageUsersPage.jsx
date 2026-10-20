import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { userApi } from '../../api/userApi'
import PageTitle from '../../components/common/PageTitle'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'

export default function ManageUsersPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'users', page],
    queryFn: () => userApi.getAll({ page, size: 20 }),
  })

  return (
    <div>
      <PageTitle>Quản lý người dùng</PageTitle>
      {isLoading ? (
        <Spinner />
      ) : (
        <table className="w-full overflow-hidden rounded-lg bg-white text-sm">
          <thead className="bg-gray-100 text-left">
            <tr>
              <th className="p-3">ID</th>
              <th className="p-3">Họ tên</th>
              <th className="p-3">Email</th>
              <th className="p-3">Quyền</th>
            </tr>
          </thead>
          <tbody>
            {data?.content?.map((user) => (
              <tr key={user.id} className="border-t">
                <td className="p-3">{user.id}</td>
                <td className="p-3">{user.fullName}</td>
                <td className="p-3">{user.email}</td>
                <td className="p-3">{user.roles?.join(', ')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
    </div>
  )
}
