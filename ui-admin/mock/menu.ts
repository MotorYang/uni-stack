import { MockMethod } from 'vite-plugin-mock'

export default [
  {
    url: '/api/menu/list',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'ok',
        data: [
          {
            label: '工作台',
            key: 'Workbench',
            icon: 'DesktopOutline',
            path: '/workbench',
            children: [
              {
                label: '仪表盘',
                key: 'Dashboard',
                icon: 'SpeedometerOutline',
                path: '/workbench/dashboard'
              },
              {
                label: '服务监控',
                key: 'ServiceMonitor',
                icon: 'PulseOutline',
                path: '/workbench/monitor'
              }
            ]
          },
          {
            label: '安全治理',
            key: 'Security',
            icon: 'ShieldCheckmarkOutline',
            path: '/security',
            children: [
              {
                label: '用户管理',
                key: 'User',
                icon: 'PersonOutline',
                path: '/security/user'
              },
              {
                label: '角色管理',
                key: 'Role',
                icon: 'PeopleOutline',
                path: '/security/role'
              },
              {
                label: '菜单配置',
                key: 'Menu',
                icon: 'MenuOutline',
                path: '/security/menu'
              },
              {
                label: '资源管理',
                key: 'Resource',
                icon: 'ServerOutline',
                path: '/security/resource'
              }
            ]
          },
          {
            label: '基础设施',
            key: 'Infrastructure',
            icon: 'ConstructOutline',
            path: '/infrastructure',
            children: [
              {
                label: '服务监控',
                key: 'Monitor',
                icon: 'PulseOutline',
                path: '/infrastructure/monitor'
              },
              {
                label: '审计日志',
                key: 'Audit',
                icon: 'DocumentTextOutline',
                path: '/infrastructure/audit'
              },
              {
                label: '任务调度',
                key: 'Job',
                icon: 'TimeOutline',
                path: '/infrastructure/job'
              }
            ]
          },
          {
            label: 'API服务',
            key: 'ApiService',
            icon: 'ServerOutline',
            path: '/api-service',
            children: [
              {
                label: '接口网关',
                key: 'ApiGateway',
                icon: 'PulseOutline',
                path: '/api-service/gateway'
              },
              {
                label: '接口管理',
                key: 'ApiDefinition',
                icon: 'DocumentTextOutline',
                path: '/api-service/definition'
              },
              {
                label: '流量控制',
                key: 'ApiTraffic',
                icon: 'SpeedometerOutline',
                path: '/api-service/traffic'
              },
              {
                label: 'Mock服务',
                key: 'ApiMock',
                icon: 'ConstructOutline',
                path: '/api-service/mock'
              }
            ]
          },
          {
            label: '日志管理',
            key: 'LogManagement',
            icon: 'ReceiptOutline',
            path: '/log-service',
            children: [
              {
                label: '日志查询',
                key: 'LogQuery',
                icon: 'SearchOutline',
                path: '/log-service/query'
              },
              {
                label: '日志配置',
                key: 'LogConfig',
                icon: 'MedalOutline',
                path: '/log-service/config'
              }
            ]
          }
        ]
      }
    }
  }
] as MockMethod[]
