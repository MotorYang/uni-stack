import request from './request'

export interface InstanceEndpoint {
  id: string
  url: string
}

export interface InstanceRegistration {
  name: string
  managementUrl: string
  healthUrl: string
  serviceUrl: string
  source: string
  metadata: Record<string, string>
}

export interface HealthComponent {
  status: 'UP' | 'DOWN' | 'UNKNOWN' | 'OUT_OF_SERVICE'
  details?: Record<string, any>
  components?: Record<string, HealthComponent>
}

export interface StatusInfo {
  status: 'UP' | 'DOWN' | 'UNKNOWN' | 'OUT_OF_SERVICE' | 'OFFLINE'
  details?: Record<string, HealthComponent>
}

export interface ServiceInstance {
  id: string
  version: number
  registration: InstanceRegistration
  registered: boolean
  statusInfo: StatusInfo
  statusTimestamp: string
  info: Record<string, any>
  endpoints: InstanceEndpoint[]
  buildVersion: string | null
  tags: Record<string, string>
}

export const getInstances = (): Promise<ServiceInstance[]> => {
  return request.get<any, ServiceInstance[]>('/monitor/instances', {
    headers: {
      'Accept': 'application/json'
    }
  })
}

export const getInstanceById = (id: string): Promise<ServiceInstance> => {
  return request.get<any, ServiceInstance>(`/monitor/instances/${id}`, {
    headers: {
      'Accept': 'application/json'
    }
  })
}

export type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'OFF' | null

export interface LoggerConfig {
  configuredLevel: LogLevel
  effectiveLevel: LogLevel
}

export interface LoggersResponse {
  levels: string[]
  loggers: Record<string, LoggerConfig>
}

export const getInstanceLoggers = (instanceId: string): Promise<LoggersResponse> => {
  return request.get<any, LoggersResponse>(`/monitor/instances/${instanceId}/actuator/loggers`, {
    headers: {
      'Accept': 'application/json'
    }
  })
}

export const setInstanceLoggerLevel = (
  instanceId: string,
  loggerName: string,
  level: LogLevel
): Promise<void> => {
  return request.post(`/monitor/instances/${instanceId}/actuator/loggers/${loggerName}`, {
    configuredLevel: level
  }, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

export interface MetricMeasurement {
  statistic: string
  value: number
}

export interface MetricTag {
  tag: string
  values: string[]
}

export interface MetricResponse {
  name: string
  description: string | null
  baseUnit: string | null
  measurements: MetricMeasurement[]
  availableTags: MetricTag[]
}

export interface MetricsListResponse {
  names: string[]
}

export interface HealthResponse {
  status: string
  components?: Record<string, HealthComponent>
  details?: Record<string, any>
}

export interface ThreadDumpThread {
  threadName: string
  threadId: number
  threadState: string
  daemon: boolean
  priority: number
  stackTrace?: Array<{
    className: string
    methodName: string
    fileName: string
    lineNumber: number
    nativeMethod: boolean
  }>
}

export interface ThreadDumpResponse {
  threads: ThreadDumpThread[]
}

export const getInstanceHealth = (instanceId: string): Promise<HealthResponse> => {
  return request.get<any, HealthResponse>(`/monitor/instances/${instanceId}/actuator/health`, {
    headers: { 'Accept': 'application/json' }
  })
}

export const getInstanceMetrics = (instanceId: string): Promise<MetricsListResponse> => {
  return request.get<any, MetricsListResponse>(`/monitor/instances/${instanceId}/actuator/metrics`, {
    headers: { 'Accept': 'application/json' }
  })
}

export const getInstanceMetric = (instanceId: string, metricName: string, tags?: string[]): Promise<MetricResponse> => {
  const params = tags && tags.length > 0 ? { tag: tags } : {}
  return request.get<any, MetricResponse>(`/monitor/instances/${instanceId}/actuator/metrics/${metricName}`, {
    params,
    headers: { 'Accept': 'application/json' }
  })
}

export const getInstanceInfo = (instanceId: string): Promise<Record<string, any>> => {
  return request.get<any, Record<string, any>>(`/monitor/instances/${instanceId}/actuator/info`, {
    headers: { 'Accept': 'application/json' }
  })
}

export const getInstanceThreadDump = (instanceId: string): Promise<ThreadDumpResponse> => {
  return request.get<any, ThreadDumpResponse>(`/monitor/instances/${instanceId}/actuator/threaddump`, {
    headers: { 'Accept': 'application/json' }
  })
}
