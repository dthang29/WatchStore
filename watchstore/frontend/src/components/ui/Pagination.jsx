// `page` bắt đầu từ 0 để khớp với Pageable của Spring
export default function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null

  return (
    <div className="mt-8 flex items-center justify-center gap-2">
      <button
        className="rounded border px-3 py-1 text-sm disabled:opacity-40"
        disabled={page === 0}
        onClick={() => onChange(page - 1)}
      >
        Trước
      </button>
      <span className="text-sm">
        {page + 1} / {totalPages}
      </span>
      <button
        className="rounded border px-3 py-1 text-sm disabled:opacity-40"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Sau
      </button>
    </div>
  )
}
