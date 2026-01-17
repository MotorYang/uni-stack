import request from './request'

export interface DashboardStat {
  key: string
  title: string
  value: number
  trend: number
}

export type DashboardStatus = 'completed' | 'pending' | 'failed'

export interface DashboardTransaction {
  id: string
  user: string
  amount: string
  date: string
  status: DashboardStatus
}

export interface DashboardChart {
  dates: string[]
  activeUsers: number[]
}

export interface DashboardStatsResponse {
  stats: DashboardStat[]
  chart: DashboardChart
  table: DashboardTransaction[]
}

export interface ServiceRequestTrendPoint {
  time: string
  count: number
}

export interface ServiceRequestTrendResponse {
  points: ServiceRequestTrendPoint[]
}

export interface ServiceErrorRatePoint {
  serviceName: string
  errorRate: number
}

export interface ServiceErrorRateResponse {
  points: ServiceErrorRatePoint[]
}

export const getDashboardStats = (): Promise<DashboardStatsResponse> => {
  return request.get<any, DashboardStatsResponse>('/dashboard/stats')
}

export const getServiceRequestTrend = (): Promise<ServiceRequestTrendResponse> => {
  return request.get<any, ServiceRequestTrendResponse>('/dashboard/service-request-trend')
}

export const getServiceErrorRate = (): Promise<ServiceErrorRateResponse> => {
  return request.get<any, ServiceErrorRateResponse>('/dashboard/service-error-rate')
}
