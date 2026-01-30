import request from './request'

/**
 * Log file information
 */
export interface LogFileInfo {
  fileName: string
  size: number
  lastModified: string
}

/**
 * Log content response
 */
export interface LogContentResponse {
  content: string
  nextPointer: number
  totalSize: number
  fileName: string
}

/**
 * Get service name from instance ID or registration info
 * Instance ID format is typically: UUID or serviceName:host:port
 */
const getServicePath = (serviceName: string): string => {
  // Convert service name to lowercase path (e.g., "system" -> "/system")
  return `/${serviceName.toLowerCase()}`
}

/**
 * Get list of available log files for a service
 * @param serviceName Service name (e.g., "system", "auth", "gateway")
 */
export const getLogFiles = (serviceName: string): Promise<LogFileInfo[]> => {
  const path = getServicePath(serviceName)
  return request.get<any, LogFileInfo[]>(`${path}/logs/files`)
}

/**
 * Read log content with pagination
 * @param serviceName Service name
 * @param fileName Log file name
 * @param seekPointer Byte offset to start reading from
 * @param pageSize Number of bytes to read (default 50KB)
 */
export const readLogContent = (
  serviceName: string,
  fileName: string,
  seekPointer: number = 0,
  pageSize: number = 51200
): Promise<LogContentResponse> => {
  const path = getServicePath(serviceName)
  return request.get<any, LogContentResponse>(`${path}/logs/read`, {
    params: {
      fileName,
      seekPointer,
      pageSize
    }
  })
}

/**
 * Read the last N lines of a log file (tail)
 * @param serviceName Service name
 * @param fileName Log file name
 * @param lines Number of lines to read (default 1000)
 */
export const tailLogContent = (
  serviceName: string,
  fileName: string,
  lines: number = 1000
): Promise<LogContentResponse> => {
  const path = getServicePath(serviceName)
  return request.get<any, LogContentResponse>(`${path}/logs/tail`, {
    params: {
      fileName,
      lines
    }
  })
}

/**
 * Download a log file
 * @param serviceName Service name
 * @param fileName Log file name
 */
export const downloadLogFile = async (
  serviceName: string,
  fileName: string
): Promise<void> => {
  const path = getServicePath(serviceName)
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  const url = `${baseURL}${path}/logs/download?fileName=${encodeURIComponent(fileName)}`

  // Create a temporary link and trigger download
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
