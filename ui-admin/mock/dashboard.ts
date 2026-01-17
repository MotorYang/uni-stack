import { MockMethod } from 'vite-plugin-mock'

export default [
  {
    url: '/api/dashboard/stats',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'ok',
        data: {
          stats: [
            {
              key: 'userTotal',
              title: '用户总数',
              value: 2345,
              trend: 12.3
            },
            {
              key: 'onlineUsers',
              title: '在线用户',
              value: 128,
              trend: 5.4
            },
            {
              key: 'servicesOnline',
              title: '在线服务数',
              value: 26,
              trend: 3.1
            },
            {
              key: 'jobPending',
              title: '待执行任务',
              value: 8,
              trend: -2.4
            }
          ],
          chart: {
            dates: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            activeUsers: [820, 932, 901, 934, 1290, 1330, 1320]
          },
          table: [
            {
              id: '#NODE-001',
              user: '核心认证服务',
              amount: '8 节点',
              date: '2023-10-25',
              status: 'completed'
            },
            {
              id: '#NODE-002',
              user: '网关集群',
              amount: '5 节点',
              date: '2023-10-25',
              status: 'pending'
            },
            {
              id: '#USER-001',
              user: '新注册用户',
              amount: '120 人',
              date: '2023-10-24',
              status: 'completed'
            },
            {
              id: '#JOB-001',
              user: '日志归档任务',
              amount: '每日 02:00',
              date: '2023-10-24',
              status: 'failed'
            },
            {
              id: '#SEC-001',
              user: '权限变更工单',
              amount: '3 项调整',
              date: '2023-10-23',
              status: 'completed'
            }
          ]
        }
      }
    }
  },
  {
    url: '/api/dashboard/service-request-trend',
    method: 'get',
    response: () => {
      const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
      const values = [
        120, 132, 101, 134, 90, 230, 210, 280, 360, 420, 380, 350,
        340, 360, 380, 420, 460, 520, 580, 640, 600, 520, 360, 220
      ]

      return {
        code: 200,
        message: 'ok',
        data: {
          points: hours.map((time, index) => ({
            time,
            count: values[index]
          }))
        }
      }
    }
  },
  {
    url: '/api/dashboard/service-error-rate',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'ok',
        data: {
          points: [
            {
              serviceName: 'auth-service',
              errorRate: 0.4
            },
            {
              serviceName: 'gateway',
              errorRate: 1.1
            },
            {
              serviceName: 'order-service',
              errorRate: 0.9
            },
            {
              serviceName: 'job-scheduler',
              errorRate: 2.3
            },
            {
              serviceName: 'log-service',
              errorRate: 0.3
            }
          ]
        }
      }
    }
  }
] as MockMethod[]
