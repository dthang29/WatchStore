const VARIANTS = {
  primary: 'bg-brand-600 text-white hover:bg-brand-700',
  outline: 'border border-gray-300 bg-white text-gray-800 hover:bg-gray-100',
  danger: 'bg-red-600 text-white hover:bg-red-700',
}

export default function Button({
  variant = 'primary',
  loading = false,
  className = '',
  disabled,
  children,
  ...props
}) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-md px-4 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-60 ${VARIANTS[variant]} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <span className="size-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
      )}
      {children}
    </button>
  )
}
