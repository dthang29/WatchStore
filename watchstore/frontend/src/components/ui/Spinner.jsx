export default function Spinner({ fullScreen = false }) {
  const spinner = (
    <div className="size-8 animate-spin rounded-full border-4 border-gray-200 border-t-brand-500" />
  )

  if (!fullScreen) return <div className="flex justify-center py-10">{spinner}</div>
  return <div className="flex min-h-screen items-center justify-center">{spinner}</div>
}
