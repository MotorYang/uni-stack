-- ============================================
-- UniStack RBAC 数据库初始化脚本
-- 数据库: MySQL 9.x
-- 字符集: utf8mb4
-- ============================================

-- 创建数据库 (如果需要)
-- CREATE DATABASE IF NOT EXISTS uni_stack DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- USE uni_stack;

-- ============================================
-- 1. 部门表
-- ============================================
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id CHAR(36) NOT NULL COMMENT '部门ID',
    parent_id CHAR(36) DEFAULT '0' COMMENT '父部门ID',
    ancestors VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    dept_type char(1) DEFAULT 'D' COMMENT '类别（C公司 D部门）',
    sort INT DEFAULT 0 COMMENT '显示顺序',
    leader VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
    create_time DATETIME(6) DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME(6) DEFAULT NULL COMMENT '更新时间',
    create_by CHAR(36) DEFAULT NULL COMMENT '创建者',
    update_by CHAR(36) DEFAULT NULL COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (id),
    KEY idx_sys_dept_parent_id (parent_id),
    KEY idx_sys_dept_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ============================================
-- 2. 用户表
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id CHAR(36) NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像地址',
    gender TINYINT DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
    status TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
    dept_id CHAR(36) DEFAULT NULL COMMENT '部门ID',
    create_time DATETIME(6) DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME(6) DEFAULT NULL COMMENT '更新时间',
    create_by CHAR(36) DEFAULT NULL COMMENT '创建者',
    update_by CHAR(36) DEFAULT NULL COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_sys_user_dept_id (dept_id),
    KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ============================================
-- 3. 角色表
-- ============================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id CHAR(36) NOT NULL COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL COMMENT '角色权限字符串',
    sort INT DEFAULT 0 COMMENT '显示顺序',
    status TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME(6) DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME(6) DEFAULT NULL COMMENT '更新时间',
    create_by CHAR(36) DEFAULT NULL COMMENT '创建者',
    update_by CHAR(36) DEFAULT NULL COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key),
    KEY idx_sys_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ============================================
-- 4. 菜单表
-- ============================================
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id CHAR(36) NOT NULL COMMENT '菜单ID',
    parent_id CHAR(36) DEFAULT '0' COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type CHAR(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    path VARCHAR(255) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    sort INT DEFAULT 0 COMMENT '显示顺序',
    visible TINYINT DEFAULT 0 COMMENT '显示状态（0显示 1隐藏）',
    status TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
    create_time DATETIME(6) DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME(6) DEFAULT NULL COMMENT '更新时间',
    create_by CHAR(36) DEFAULT NULL COMMENT '创建者',
    update_by CHAR(36) DEFAULT NULL COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (id),
    KEY idx_sys_menu_parent_id (parent_id),
    KEY idx_sys_menu_menu_type (menu_type),
    KEY idx_sys_menu_visible (visible),
    KEY idx_sys_menu_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ============================================
-- 5. 用户角色关联表
-- ============================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id CHAR(36) NOT NULL COMMENT '用户ID',
    role_id CHAR(36) NOT NULL COMMENT '角色ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- ============================================
-- 6. 角色菜单关联表
-- ============================================
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id CHAR(36) NOT NULL COMMENT '角色ID',
    menu_id CHAR(36) NOT NULL COMMENT '菜单ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (role_id, menu_id),
    KEY idx_sys_role_menu_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

-- ============================================
-- 7. 创建权限表
-- ============================================
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
                                id CHAR(36) NOT NULL COMMENT '权限ID',
                                perm_name VARCHAR(50) NOT NULL COMMENT '权限名称',
                                perm_code VARCHAR(100) NOT NULL COMMENT '权限编码（如 user:read, user:write）',
                                description VARCHAR(200) DEFAULT NULL COMMENT '权限描述',
                                sort INT DEFAULT 0 COMMENT '显示顺序',
                                status TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
                                create_time DATETIME(6) DEFAULT NULL COMMENT '创建时间',
                                update_time DATETIME(6) DEFAULT NULL COMMENT '更新时间',
                                create_by CHAR(36) DEFAULT NULL COMMENT '创建者',
                                update_by CHAR(36) DEFAULT NULL COMMENT '更新者',
                                deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_perm_code (perm_code),
                                KEY idx_sys_permission_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表';

-- ============================================
-- 8. 创建角色权限关联表
-- ============================================
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
                                     role_id CHAR(36) NOT NULL COMMENT '角色ID',
                                     permission_id CHAR(36) NOT NULL COMMENT '权限ID',
                                     PRIMARY KEY (role_id, permission_id),
                                     KEY idx_sys_role_permission_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

-- ============================================
-- 9. 创建权限和资源相关表单
-- ============================================
-- 资源组表
DROP TABLE IF EXISTS sys_resource_group;
CREATE TABLE sys_resource_group (
                                    id CHAR(36) NOT NULL COMMENT '资源组ID',
                                    res_group_name VARCHAR(50) NOT NULL COMMENT '资源组名称',
                                    res_group_code VARCHAR(50) NOT NULL COMMENT '资源组编码',
                                    description VARCHAR(200) DEFAULT NULL COMMENT '资源组描述',
                                    service_name VARCHAR(50) DEFAULT NULL COMMENT '所属微服务标识', -- 对应 spring.application.name
                                    sort INT DEFAULT 0 COMMENT '显示顺序',
                                    status TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
                                    create_time DATETIME(6) DEFAULT NULL,
                                    update_time DATETIME(6) DEFAULT NULL,
                                    create_by CHAR(36) DEFAULT NULL,
                                    update_by CHAR(36) DEFAULT NULL,
                                    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_res_group_code (res_group_code),
                                    KEY idx_service_name (service_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源组表';

-- 资源表
DROP TABLE IF EXISTS sys_resource;
CREATE TABLE sys_resource (
                              id CHAR(36) NOT NULL COMMENT '资源ID',
                              group_id CHAR(36) NOT NULL COMMENT '资源组ID',
                              res_name VARCHAR(50) NOT NULL COMMENT '资源名称',
                              res_type VARCHAR(10) NOT NULL COMMENT '资源类型（API、BUTTON）',
                              res_path VARCHAR(255) NOT NULL COMMENT '资源路径（API路径或组件ID）',
                              res_code VARCHAR(100) NOT NULL COMMENT '资源编码',
                              res_method VARCHAR(20) DEFAULT '*' COMMENT '请求方式（GET/POST/*）', -- 默认 * 代表不限
                              description VARCHAR(200) DEFAULT NULL,
                              sort INT DEFAULT 0,
                              status TINYINT DEFAULT 0,
                              create_time DATETIME(6) DEFAULT NULL,
                              update_time DATETIME(6) DEFAULT NULL,
                              create_by CHAR(36) DEFAULT NULL,
                              update_by CHAR(36) DEFAULT NULL,
                              deleted TINYINT DEFAULT 0,
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_res_code (res_code),
                              UNIQUE KEY uk_path_method (res_path, res_method, deleted),
                              KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源表';

-- 权限资源关联表
DROP TABLE IF EXISTS sys_permission_resource;
CREATE TABLE sys_permission_resource (
                                         id CHAR(36) NOT NULL, -- 增加独立ID
                                         perm_id CHAR(36) NOT NULL COMMENT '权限标识表ID',
                                         res_id CHAR(36) NOT NULL COMMENT '资源表ID',
                                         PRIMARY KEY (id),
                                         UNIQUE KEY uk_perm_res (perm_id, res_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限资源关联表';

-- ============================================
-- 10. 创建用户和部门的关联表
-- ============================================
-- 用户部门关联表
DROP TABLE IF EXISTS sys_user_dept;
CREATE TABLE sys_user_dept
(
    id          CHAR(36) NOT NULL PRIMARY KEY COMMENT 'id',
    tenant_id   CHAR(36) NOT NULL COMMENT '租户id',
    user_id     CHAR(36) NOT NULL COMMENT '用户id',
    dept_id     CHAR(36) NOT NULL COMMENT '部门id',
    is_primary  int      NOT NULL DEFAULT 1 COMMENT '是否主职部门（0否 1是）',
    position    varchar(100) COMMENT '职务/岗位',
    create_time DATETIME(6)       DEFAULT NULL,
    update_time DATETIME(6)       DEFAULT NULL,
    create_by   CHAR(36)          DEFAULT NULL,
    update_by   CHAR(36)          DEFAULT NULL,
    deleted     TINYINT           DEFAULT 0,
    UNIQUE KEY uk_tenant_user_dept (tenant_id, user_id, dept_id),
    KEY idx_tenant_user (tenant_id, user_id),
    KEY idx_tenant_dept (tenant_id, dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户部门关联表';