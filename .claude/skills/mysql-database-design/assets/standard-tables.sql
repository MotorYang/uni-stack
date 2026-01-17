-- ============================================================================
-- MySQL Database Design Template - Standard Tables
-- Compatible with MySQL 9.x
-- Uses UUID7 primary keys, audit fields, and logical deletion
-- ============================================================================

-- ============================================================================
-- 1. USER TABLE (Standard Business Entity)
-- ============================================================================
CREATE TABLE `sys_user` (
  -- Primary Key (UUID7)
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Business Fields
  `username` VARCHAR(64) NOT NULL COMMENT 'Login username (unique)',
  `password` VARCHAR(128) NOT NULL COMMENT 'Encrypted password (bcrypt/argon2)',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT 'Display name',
  `email` VARCHAR(128) DEFAULT NULL COMMENT 'Email address',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT 'Phone number',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT 'Avatar URL',
  `gender` TINYINT(1) DEFAULT 0 COMMENT 'Gender: 0=unknown, 1=male, 2=female',
  
  -- Status Fields
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  `login_date` DATETIME(6) DEFAULT NULL COMMENT 'Last login timestamp',
  `login_ip` VARCHAR(128) DEFAULT NULL COMMENT 'Last login IP address',
  
  -- Logical Foreign Keys (NO physical constraints)
  `dept_id` VARCHAR(36) DEFAULT NULL COMMENT 'Department ID (references sys_dept.id)',
  
  -- Audit Fields (REQUIRED)
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  INDEX `idx_dept_id` (`dept_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_del_flag_create_time` (`del_flag`, `create_time`),
  INDEX `idx_del_flag_status` (`del_flag`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='System user table';

-- ============================================================================
-- 2. ROLE TABLE (Permission Management)
-- ============================================================================
CREATE TABLE `sys_role` (
  -- Primary Key
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Business Fields
  `role_name` VARCHAR(64) NOT NULL COMMENT 'Role name',
  `role_code` VARCHAR(32) NOT NULL COMMENT 'Role code (unique identifier)',
  `role_sort` INT DEFAULT 0 COMMENT 'Display order (smaller first)',
  `description` VARCHAR(512) DEFAULT NULL COMMENT 'Role description',
  
  -- Data Scope (Row-level permissions)
  `data_scope` TINYINT(1) DEFAULT 1 COMMENT 'Data scope: 1=all, 2=custom, 3=dept, 4=dept_and_below, 5=self',
  
  -- Status Fields
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  
  -- Audit Fields
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  INDEX `idx_status` (`status`),
  INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='System role table';

-- ============================================================================
-- 3. USER-ROLE RELATIONSHIP TABLE (Many-to-Many)
-- ============================================================================
CREATE TABLE `sys_user_role` (
  -- Primary Key
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Relationship Fields
  `user_id` VARCHAR(36) NOT NULL COMMENT 'User ID (references sys_user.id)',
  `role_id` VARCHAR(36) NOT NULL COMMENT 'Role ID (references sys_role.id)',
  
  -- Audit Fields
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  INDEX `idx_role_id` (`role_id`),
  INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User-Role relationship table';

-- ============================================================================
-- 4. DEPARTMENT TABLE (Tree Structure)
-- ============================================================================
CREATE TABLE `sys_dept` (
  -- Primary Key
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Tree Structure Fields
  `parent_id` VARCHAR(36) DEFAULT NULL COMMENT 'Parent department ID (NULL for root departments)',
  `ancestors` VARCHAR(512) DEFAULT NULL COMMENT 'Ancestor chain: comma-separated IDs for quick queries',
  
  -- Business Fields
  `dept_name` VARCHAR(64) NOT NULL COMMENT 'Department name',
  `dept_code` VARCHAR(32) NOT NULL COMMENT 'Department code (unique)',
  `sort_order` INT DEFAULT 0 COMMENT 'Display order (smaller first)',
  `leader_id` VARCHAR(36) DEFAULT NULL COMMENT 'Department leader user ID (references sys_user.id)',
  `email` VARCHAR(128) DEFAULT NULL COMMENT 'Department contact email',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT 'Department contact phone',
  
  -- Status Fields
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  
  -- Audit Fields
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_leader_id` (`leader_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Department tree structure table';

-- ============================================================================
-- 5. MENU TABLE (Permission Tree)
-- ============================================================================
CREATE TABLE `sys_menu` (
  -- Primary Key
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Tree Structure
  `parent_id` VARCHAR(36) DEFAULT NULL COMMENT 'Parent menu ID (NULL for root menus)',
  
  -- Menu Properties
  `menu_name` VARCHAR(64) NOT NULL COMMENT 'Menu name',
  `menu_type` CHAR(1) NOT NULL COMMENT 'Menu type: M=directory, C=menu, F=button',
  `path` VARCHAR(256) DEFAULT NULL COMMENT 'Route path',
  `component` VARCHAR(256) DEFAULT NULL COMMENT 'Component path',
  `perms` VARCHAR(128) DEFAULT NULL COMMENT 'Permission identifier (e.g., system:user:list)',
  `icon` VARCHAR(128) DEFAULT NULL COMMENT 'Menu icon',
  `sort_order` INT DEFAULT 0 COMMENT 'Display order',
  
  -- Visibility and Behavior
  `visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Visibility: 0=hidden, 1=visible',
  `is_frame` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Is external link: 0=no, 1=yes',
  `is_cache` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Cache page: 0=no, 1=yes',
  
  -- Status Fields
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  
  -- Audit Fields
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_menu_type` (`menu_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Menu permission table';

-- ============================================================================
-- 6. AUDIT LOG TABLE (System Logs)
-- ============================================================================
CREATE TABLE `sys_operation_log` (
  -- Primary Key
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Operation Info
  `title` VARCHAR(128) DEFAULT NULL COMMENT 'Operation title',
  `business_type` TINYINT(1) DEFAULT 0 COMMENT 'Business type: 0=other, 1=insert, 2=update, 3=delete, 4=grant, 5=export, 6=import',
  `method` VARCHAR(256) DEFAULT NULL COMMENT 'Method name',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT 'HTTP method: GET, POST, PUT, DELETE',
  `operator_type` TINYINT(1) DEFAULT 0 COMMENT 'Operator type: 0=other, 1=backend_user, 2=mobile',
  
  -- User Info
  `oper_user_id` VARCHAR(36) DEFAULT NULL COMMENT 'Operator user ID',
  `oper_name` VARCHAR(64) DEFAULT NULL COMMENT 'Operator username',
  `dept_name` VARCHAR(64) DEFAULT NULL COMMENT 'Department name',
  
  -- Request Info
  `oper_url` VARCHAR(512) DEFAULT NULL COMMENT 'Request URL',
  `oper_ip` VARCHAR(128) DEFAULT NULL COMMENT 'Request IP',
  `oper_location` VARCHAR(256) DEFAULT NULL COMMENT 'IP location',
  `oper_param` TEXT DEFAULT NULL COMMENT 'Request parameters (JSON)',
  
  -- Response Info
  `json_result` TEXT DEFAULT NULL COMMENT 'Response data (JSON)',
  `status` TINYINT(1) DEFAULT 1 COMMENT 'Status: 0=failed, 1=success',
  `error_msg` VARCHAR(2048) DEFAULT NULL COMMENT 'Error message if failed',
  
  -- Timing
  `oper_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Operation timestamp',
  `cost_time` BIGINT DEFAULT 0 COMMENT 'Execution time (milliseconds)',
  
  -- Audit Fields (Simplified for logs)
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  INDEX `idx_oper_user_id` (`oper_user_id`),
  INDEX `idx_business_type` (`business_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Operation log table';

-- ============================================================================
-- 7. DICTIONARY TABLE (System Configuration)
-- ============================================================================
CREATE TABLE `sys_dict_type` (
  -- Primary Key
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Dictionary Type
  `dict_name` VARCHAR(128) NOT NULL COMMENT 'Dictionary name',
  `dict_type` VARCHAR(128) NOT NULL COMMENT 'Dictionary type code (unique)',
  
  -- Status Fields
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  
  -- Audit Fields
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Dictionary type table';

CREATE TABLE `sys_dict_data` (
  -- Primary Key
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Dictionary Data
  `dict_type` VARCHAR(128) NOT NULL COMMENT 'Dictionary type (references sys_dict_type.dict_type)',
  `dict_label` VARCHAR(128) NOT NULL COMMENT 'Dictionary label (display value)',
  `dict_value` VARCHAR(128) NOT NULL COMMENT 'Dictionary value (stored value)',
  `dict_sort` INT DEFAULT 0 COMMENT 'Display order',
  `css_class` VARCHAR(128) DEFAULT NULL COMMENT 'CSS class name',
  `list_class` VARCHAR(128) DEFAULT NULL COMMENT 'List style class',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Is default: 0=no, 1=yes',
  
  -- Status Fields
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  
  -- Audit Fields
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  INDEX `idx_dict_type` (`dict_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Dictionary data table';

-- ============================================================================
-- 8. CONFIGURATION TABLE (System Parameters)
-- ============================================================================
CREATE TABLE `sys_config` (
  -- Primary Key
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
  
  -- Config Data
  `config_name` VARCHAR(128) NOT NULL COMMENT 'Configuration name',
  `config_key` VARCHAR(128) NOT NULL COMMENT 'Configuration key (unique)',
  `config_value` VARCHAR(512) NOT NULL COMMENT 'Configuration value',
  `config_type` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Type: 0=system_built-in, 1=user_defined',
  
  -- Audit Fields
  `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  
  -- Constraints and Indexes
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='System configuration table';

-- ============================================================================
-- End of Standard Table Templates
-- ============================================================================
