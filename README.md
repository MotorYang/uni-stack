# UniStack

<p align="center">
  <strong>全栈微服务快速开发平台</strong>
</p>

<p align="center">
  <a href="https://github.com/motoryang/UniStack/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License">
  </a>
  <img src="https://img.shields.io/badge/JDK-21-orange.svg" alt="JDK 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.7-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2023.0.4-green.svg" alt="Spring Cloud">
  <img src="https://img.shields.io/badge/Vue-3.5-brightgreen.svg" alt="Vue 3">
</p>

---

## 项目简介

UniStack 是一个基于 **Spring Cloud** 和 **Vue 3** 的全栈微服务快速开发平台，采用最新技术栈，提供完整的企业级后台管理系统解决方案。

## 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 21 | 启用虚拟线程 (Virtual Threads) |
| Spring Boot | 3.3.7 | 基础框架 |
| Spring Cloud | 2023.0.4 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.2 | 阿里巴巴微服务组件 |
| MyBatis-Plus | 3.5.7 | ORM 增强框架 |
| MySQL | 9.x | 数据库 |
| Redis | - | 缓存 & 分布式锁 |
| Nacos | - | 注册中心 & 配置中心 |
| MinIO | 8.5.14 | 对象存储 |
| Knife4j | 4.3.0 | API 文档 |
| Spring Boot Admin | 3.3.4 | 服务监控 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 渐进式 JavaScript 框架 |
| Vite | 7.x | 下一代前端构建工具 |
| TypeScript | 5.9.x | JavaScript 超集 |
| Naive UI | 2.43.x | Vue 3 组件库 |
| Pinia | 3.x | 状态管理 |
| Tailwind CSS | 4.x | 原子化 CSS 框架 |
| ECharts | 6.x | 可视化图表库 |
| Axios | 1.x | HTTP 客户端 |

## 项目结构

```
UniStack/
├── server/                          # 后端微服务
│   ├── gateway/                     # 网关服务 - API 路由、鉴权、限流
│   ├── auth/                        # 认证服务 - JWT 双令牌管理
│   ├── system/                      # 系统服务 - 用户、角色、菜单、部门
│   ├── storage/                     # 存储服务 - 文件上传下载
│   ├── monitor/                     # 监控服务 - 服务健康监控
│   └── common/                      # 公共模块
│       ├── common-core/             # 核心工具类、响应封装
│       ├── common-mybatis/          # MyBatis-Plus 配置
│       ├── common-redis/            # Redis 配置
│       └── common-utils/            # 通用工具
│
├── ui-admin/                        # 前端管理后台
│   └── src/
│       ├── api/                     # API 接口
│       ├── components/              # 公共组件
│       ├── layouts/                 # 布局组件
│       ├── stores/                  # Pinia 状态管理
│       ├── views/                   # 页面视图
│       └── mock/                    # Mock 数据
│
└── docs/                            # 项目文档
    └── database/                    # 数据库设计文档
```

## 功能模块

### 系统管理
- 用户管理：用户新增、编辑、删除、分配角色
- 角色管理：角色权限分配、数据权限设置
- 菜单管理：动态菜单配置、按钮权限
- 部门管理：组织架构管理、多部门支持
- 资源管理：API 资源权限管理

### 系统监控
- 服务监控：微服务健康状态监控
- 日志管理：操作日志、登录日志

### 基础设施
- 文件存储：MinIO 对象存储集成
- 配置中心：Nacos 动态配置管理

## 快速开始

### 环境要求

- JDK 21+
- Node.js 20+
- Maven 3.9+
- MySQL 9.x
- Redis 7.x
- Nacos 2.x

### 后端启动

```bash
# 克隆项目
git clone https://github.com/motoryang/UniStack.git

# 进入后端目录
cd UniStack/server

# 安装依赖
mvn clean install

# 启动网关服务
cd gateway && mvn spring-boot:run

# 启动认证服务
cd ../auth && mvn spring-boot:run

# 启动系统服务
cd ../system && mvn spring-boot:run
```

### 前端启动

```bash
# 进入前端目录
cd UniStack/ui-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 生产构建
npm run build
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端管理后台 | http://localhost:5173 |
| 网关服务 | http://localhost:8080 |
| API 文档 | http://localhost:8080/doc.html |
| 服务监控 | http://localhost:9100 |

## 开发规范

### 后端规范

- 使用 `LambdaQueryWrapper` / `LambdaUpdateWrapper` 进行类型安全查询
- DTO/VO 使用 JDK 21 `record` 类
- 所有响应使用 `RestResult<T>` 统一封装
- 实体类继承 `BaseEntity`，包含审计字段
- 子模块不硬编码依赖版本，统一使用父 POM 管理

### 前端规范

- 使用 `<script setup>` 组合式 API
- 状态管理使用 Pinia，支持 localStorage 持久化
- API 请求集中在 `src/api/` 目录
- 使用 Naive UI 组件 + Tailwind CSS 样式

## 许可证

[MIT License](LICENSE)

## 贡献

欢迎提交 Issue 和 Pull Request！

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/motoryang">motoryang</a>
</p>