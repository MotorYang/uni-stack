# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

UniStack is a full-stack microservices platform with:
- **Backend**: Spring Cloud microservices (JDK 21) - `server/` (scaffolding in progress)
- **Frontend**: Vue 3 + TypeScript admin dashboard - `ui-admin/`

## Build Commands

### Frontend (ui-admin/)
```bash
cd ui-admin
npm install          # Install dependencies
npm run dev          # Start dev server (Vite)
npm run build        # Production build (vue-tsc + vite build)
npm run preview      # Preview production build
```

### Backend (when scaffolded)
```bash
mvn clean install              # Build all modules
mvn spring-boot:run            # Run specific service
mvn test                       # Run tests
mvn test -Dtest=ClassName      # Run single test class
```

## Architecture

### Backend Microservices (Planned)
- **gateway**: API routing, auth forwarding, rate limiting. NO database access
- **auth**: JWT dual-token management. Fetches user data from system via Feign. NO direct user table access
- **system**: Core admin module (Users, Roles, Menus, Depts). Primary database module
- **common**: Shared DTOs, utilities, `RestResult<T>` response wrapper, MyBatis-Plus base configs
- **config**: Configuration center

### Frontend Structure
```
ui-admin/src/
├── api/           # Axios clients (request.ts has interceptors)
├── layouts/       # DefaultLayout (dashboard), BlankLayout (login)
├── stores/        # Pinia stores (user, theme)
├── views/         # Page components
└── mock/          # vite-plugin-mock data
```

### Key Integration Patterns
- API responses use `{ code: 200, data: T, message: string }` format
- Frontend stores Bearer token in localStorage, auto-injects via Axios interceptor
- Inter-service calls use OpenFeign with fallback factories

## Tech Stack Versions

| Component | Version |
|-----------|---------|
| JDK | 21 (Virtual Threads enabled) |
| Spring Boot | 3.3.x |
| Spring Cloud | 2023.0.x (Leyton) |
| MyBatis-Plus | 3.5.7 |
| MySQL | 9.x |
| Vue | 3.5.x |
| Vite | 7.x |
| TypeScript | 5.9.x |
| Naive UI | 2.43.x |

## Coding Conventions

### Backend
- Never hardcode dependency versions in submodules; use parent BOM
- Use `LambdaQueryWrapper`/`LambdaUpdateWrapper` for type-safe queries
- Use `record` classes for DTOs/VOs (JDK 21 feature)
- All responses wrapped in `RestResult<T>` from common module
- Entities extend BaseEntity with audit fields (id, createTime, updateTime, createBy, updateBy, deleted)
- MySQL connector: `com.mysql:mysql-connector-j` (not deprecated mysql-connector-java)

### Frontend
- Use `<script setup>` syntax with Composition API
- State management via Pinia with localStorage persistence
- API calls centralized in `src/api/` directory
- Naive UI for components, Tailwind CSS for styling
