// Lấy message lỗi từ response của Spring Boot (ProblemDetail hoặc { message })
export function getErrorMessage(error, fallback = 'Đã có lỗi xảy ra') {
  const data = error?.response?.data
  return data?.message ?? data?.detail ?? error?.message ?? fallback
}
