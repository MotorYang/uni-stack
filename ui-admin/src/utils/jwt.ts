/**
 * 解析 JWT Token payload（不验证签名）
 */
export function parseJwtPayload(token: string): Record<string, any> | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

/**
 * 从 Token 中获取用户 ID
 */
export function getUserIdFromToken(token: string): string | null {
  const payload = parseJwtPayload(token)
  return payload?.userId ?? null
}

/**
 * 从 Token 中获取用户名
 */
export function getUsernameFromToken(token: string): string | null {
  const payload = parseJwtPayload(token)
  return payload?.username ?? null
}
