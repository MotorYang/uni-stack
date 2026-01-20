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