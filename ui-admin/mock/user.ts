import { MockMethod } from 'vite-plugin-mock'

export default [
  {
    url: '/api/auth/login',
    method: 'post',
    response: ({ body }) => {
      return {
        code: 200,
        message: 'ok',
        data: {
          token: 'mock-token-' + Math.random().toString(36).substring(7),
          user: {
            username: 'admin',
            nickName: '管理员',
            avatar: 'https://img.somake.ai/cdn-cgi/image/width=800,quality=80,format=auto,fit=scale-down/tools/anime-avatar-maker_cover_1752568414_8087.jpg',
            email: 'admin@unistack.com',
            phone: '138-0000-0000',
            department: '技术研发部',
            position: '平台架构师',
            github: 'https://github.com/admin'
          }
        }
      }
    }
  },
  {
    url: '/api/user/info',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'ok',
        data: {
          username: 'admin',
          nickName: '管理员',
          avatar: 'https://img.somake.ai/cdn-cgi/image/width=800,quality=80,format=auto,fit=scale-down/tools/anime-avatar-maker_cover_1752568414_8087.jpg',
          email: 'admin@unistack.com',
          phone: '138-0000-0000',
          department: '技术研发部',
          position: '平台架构师',
          github: 'https://github.com/admin'
        }
      }
    }
  },
  {
    url: '/api/auth/logout',
    method: 'post',
    response: () => {
      return {
        code: 200,
        message: 'ok',
        data: null
      }
    }
  },
  {
    url: '/api/user/update',
    method: 'post',
    response: ({ body }) => {
      return {
        code: 200,
        message: 'ok',
        data: {
          username: 'admin',
          nickName: body.nickName || '管理员',
          avatar: 'https://img.somake.ai/cdn-cgi/image/width=800,quality=80,format=auto,fit=scale-down/tools/anime-avatar-maker_cover_1752568414_8087.jpg',
          email: body.email || 'admin@unistack.com',
          phone: body.phone || '138-0000-0000',
          department: body.department || '技术研发部',
          position: body.position || '平台架构师',
          github: body.github || 'https://github.com/admin'
        }
      }
    }
  }
] as MockMethod[]
