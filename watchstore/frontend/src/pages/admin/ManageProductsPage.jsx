import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import PageTitle from '../../components/common/PageTitle'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import {
  useActivateProduct,
  useAdminProducts,
  useDeleteProduct,
  useProductCategories,
  useSaveProduct,
} from '../../hooks/useProducts'
import { getErrorMessage } from '../../utils/error'
import { formatPrice } from '../../utils/format'

export default function ManageProductsPage() {
  const [page, setPage] = useState(0)
  const [editing, setEditing] = useState(null)
  const { data, isLoading } = useAdminProducts({ page, size: 20, sort: 'createdAt,desc' })
  const { data: categories, isLoading: isLoadingCategories } = useProductCategories()
  const saveProduct = useSaveProduct()
  const deleteProduct = useDeleteProduct()
  const activateProduct = useActivateProduct()
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: {
      categoryId: '',
      name: '',
      brand: '',
      description: '',
      movementType: 'QUARTZ',
      price: '',
      stock: 0,
      imageUrl: '',
    },
  })

  useEffect(() => {
    reset(
      editing
        ? {
            categoryId: editing.categoryId,
            name: editing.name,
            brand: editing.brand,
            description: editing.description ?? '',
            movementType: editing.movementType,
            price: editing.price,
            stock: editing.stock,
            imageUrl: editing.imageUrl ?? '',
          }
        : {
            categoryId: categories?.[0]?.id ?? '',
            name: '',
            brand: '',
            description: '',
            movementType: 'QUARTZ',
            price: '',
            stock: 0,
            imageUrl: '',
          },
    )
  }, [editing, categories, reset])

  const handleDelete = (product) => {
    if (window.confirm(`Ẩn sản phẩm "${product.name}" khỏi cửa hàng?`)) deleteProduct.mutate(product.id)
  }

  const onSubmit = (values) => {
    const payload = {
      ...values,
      categoryId: Number(values.categoryId),
      price: Number(values.price),
      stock: Number(values.stock),
    }
    saveProduct.mutate(
      { id: editing?.id, payload },
      { onSuccess: () => setEditing(null) },
    )
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between gap-4">
        <PageTitle>Quản lý sản phẩm</PageTitle>
        <Button onClick={() => setEditing({})} disabled={isLoadingCategories}>
          Thêm sản phẩm
        </Button>
      </div>

      {editing && (
        <form onSubmit={handleSubmit(onSubmit)} className="mb-8 space-y-4 rounded-lg bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">{editing.id ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới'}</h2>
          <div className="grid gap-4 md:grid-cols-2">
            <label className="block">
              <span className="mb-1 block text-sm font-medium text-gray-700">Danh mục</span>
              <select
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
                {...register('categoryId', { required: 'Vui lòng chọn danh mục' })}
              >
                {categories?.map((category) => (
                  <option key={category.id} value={category.id}>{category.name}</option>
                ))}
              </select>
              {errors.categoryId && <span className="mt-1 block text-xs text-red-600">{errors.categoryId.message}</span>}
            </label>
            <Input label="Tên sản phẩm" error={errors.name} {...register('name', { required: 'Vui lòng nhập tên sản phẩm' })} />
            <Input label="Thương hiệu" error={errors.brand} {...register('brand', { required: 'Vui lòng nhập thương hiệu' })} />
            <label className="block">
              <span className="mb-1 block text-sm font-medium text-gray-700">Loại máy</span>
              <select className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" {...register('movementType')}>
                <option value="QUARTZ">Quartz</option>
                <option value="AUTOMATIC">Automatic</option>
                <option value="SMART">Smart</option>
              </select>
            </label>
            <Input label="Giá (VND)" type="number" min="1" error={errors.price} {...register('price', { required: 'Vui lòng nhập giá', min: { value: 1, message: 'Giá phải lớn hơn 0' } })} />
            <Input label="Tồn kho" type="number" min="0" error={errors.stock} {...register('stock', { required: 'Vui lòng nhập tồn kho', min: { value: 0, message: 'Tồn kho không được âm' } })} />
          </div>
          <Input label="Đường dẫn ảnh" placeholder="/images/watch.jpg hoặc https://..." error={errors.imageUrl} {...register('imageUrl')} />
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-gray-700">Mô tả</span>
            <textarea rows={3} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" {...register('description')} />
          </label>
          {saveProduct.isError && <p className="text-sm text-red-600">{getErrorMessage(saveProduct.error)}</p>}
          <div className="flex gap-3">
            <Button type="submit" loading={saveProduct.isPending}>Lưu sản phẩm</Button>
            <Button type="button" variant="outline" onClick={() => setEditing(null)}>Hủy</Button>
          </div>
        </form>
      )}

      {isLoading ? (
        <Spinner />
      ) : (
        <div className="overflow-x-auto rounded-lg bg-white">
          <table className="w-full text-sm">
            <thead className="bg-gray-100 text-left">
              <tr>
                <th className="p-3">ID</th>
                <th className="p-3">Tên</th>
                <th className="p-3">Thương hiệu</th>
                <th className="p-3">Giá</th>
                <th className="p-3">Tồn kho</th>
                <th className="p-3">Trạng thái</th>
                <th className="p-3" />
              </tr>
            </thead>
            <tbody>
              {data?.content?.map((product) => (
                <tr key={product.id} className="border-t">
                  <td className="p-3">{product.id}</td>
                  <td className="p-3 font-medium">{product.name}</td>
                  <td className="p-3">{product.brand}</td>
                  <td className="p-3">{formatPrice(product.price)}</td>
                  <td className="p-3">{product.stock}</td>
                  <td className="p-3">
                    <span className={product.active ? 'text-green-700' : 'text-gray-500'}>
                      {product.active ? 'Đang bán' : 'Đã ẩn'}
                    </span>
                  </td>
                  <td className="whitespace-nowrap p-3 text-right">
                    <button onClick={() => setEditing(product)} className="mr-3 text-brand-600 hover:underline">Sửa</button>
                    {product.active ? (
                      <button onClick={() => handleDelete(product)} className="text-red-600 hover:underline">Ẩn</button>
                    ) : (
                      <button onClick={() => activateProduct.mutate(product.id)} className="text-green-700 hover:underline">Bật lại</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
    </div>
  )
}
