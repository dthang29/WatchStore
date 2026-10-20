// Dùng với react-hook-form: <Input label="Email" error={errors.email} {...register('email')} />
export default function Input({ label, error, className = '', ...props }) {
  return (
    <label className={`block ${className}`}>
      {label && <span className="mb-1 block text-sm font-medium text-gray-700">{label}</span>}
      <input
        className={`w-full rounded-md border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-brand-500 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
        aria-invalid={Boolean(error)}
        {...props}
      />
      {error && <span className="mt-1 block text-xs text-red-600">{error.message}</span>}
    </label>
  )
}
