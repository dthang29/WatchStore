// Chỉ decode payload để hiển thị UI, KHÔNG dùng để xác thực.
// Việc verify chữ ký luôn do backend (Spring Security) đảm nhận.
export function decodeJwt(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0))
    return JSON.parse(new TextDecoder().decode(bytes))
  } catch {
    return null
  }
}

export function getUserFromToken(token) {
  const claims = decodeJwt(token)
  if (!claims) return null
  return {
    email: claims.sub,
    fullName: claims.fullName ?? claims.name ?? claims.sub,
    roles: claims.roles ?? [],
  }
}
