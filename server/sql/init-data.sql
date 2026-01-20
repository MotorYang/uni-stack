SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

/* =====================================================
   1. 部门
   ===================================================== */
INSERT INTO sys_dept
(id, parent_id, ancestors, dept_name, sort, leader, status, create_time)
VALUES
    (@dept_root := UUIDV7(), '0', '0', '总部', 1, '系统', 0, NOW(6));

/* =====================================================
   2. 用户admin，默认密码123456
   ===================================================== */
INSERT INTO sys_user
(id, username, password, nickname, status, dept_id, create_time)
VALUES
    (@user_admin := UUIDV7(),
     'admin',
     '$2a$10$aS8CsbwlTto/BqiBl5M.oepC6AimQeSrKNfXnRzczlcI9Ah.HBhOG',
     '超级管理员',
     0,
     @dept_root,
     NOW(6));

/* =====================================================
   3. 角色
   ===================================================== */
INSERT INTO sys_role
(id, role_name, role_key, sort, status, remark, create_time)
VALUES
    (@role_admin := UUIDV7(), '超级管理员', 'ADMIN', 1, 0, '系统最高权限', NOW(6));

/* =====================================================
   4. 菜单（严格按前端结构）
   ===================================================== */

/* ---------- 工作台 ---------- */
INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, icon, sort, status, create_time)
VALUES
    (@m_workbench := UUIDV7(), '0', '工作台', 'M', '/workbench', 'DesktopOutline', 1, 0, NOW(6));

INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, status, create_time)
VALUES
    (UUIDV7(), @m_workbench, '仪表盘', 'C',
     '/workbench/dashboard',
     'workbench/dashboard/index',
     'workbench:dashboard:view',
     'SpeedometerOutline', 1, 0, NOW(6)),

    (UUIDV7(), @m_workbench, '服务监控', 'C',
     '/workbench/monitor',
     'workbench/monitor/index',
     'workbench:monitor:view',
     'PulseOutline', 2, 0, NOW(6));

/* ---------- 安全治理 ---------- */
INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, icon, sort, status, create_time)
VALUES
    (@m_security := UUIDV7(), '0', '安全治理', 'M', '/security', 'ShieldCheckmarkOutline', 2, 0, NOW(6));

INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, status, create_time)
VALUES
    (UUIDV7(), @m_security, '用户管理', 'C',
     '/security/user',
     'security/user/index',
     'security:user:view',
     'PersonOutline', 1, 0, NOW(6)),

    (UUIDV7(), @m_security, '角色管理', 'C',
     '/security/role',
     'security/role/index',
     'security:role:view',
     'PeopleOutline', 2, 0, NOW(6)),

    (UUIDV7(), @m_security, '菜单配置', 'C',
     '/security/menu',
     'security/menu/index',
     'security:menu:view',
     'MenuOutline', 3, 0, NOW(6)),

    (UUIDV7(), @m_security, '资源管理', 'C',
     '/security/resource',
     'security/resource/index',
     'security:resource:view',
     'ServerOutline', 4, 0, NOW(6));

/* ---------- 基础设施 ---------- */
INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, icon, sort, status, create_time)
VALUES
    (@m_infra := UUIDV7(), '0', '基础设施', 'M', '/infrastructure', 'ConstructOutline', 3, 0, NOW(6));

INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, status, create_time)
VALUES
    (UUIDV7(), @m_infra, '服务监控', 'C',
     '/infrastructure/monitor',
     'infrastructure/monitor/index',
     'infra:monitor:view',
     'PulseOutline', 1, 0, NOW(6)),

    (UUIDV7(), @m_infra, '审计日志', 'C',
     '/infrastructure/audit',
     'infrastructure/audit/index',
     'infra:audit:view',
     'DocumentTextOutline', 2, 0, NOW(6)),

    (UUIDV7(), @m_infra, '任务调度', 'C',
     '/infrastructure/job',
     'infrastructure/job/index',
     'infra:job:view',
     'TimeOutline', 3, 0, NOW(6));

/* ---------- API 服务 ---------- */
INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, icon, sort, status, create_time)
VALUES
    (@m_api := UUIDV7(), '0', 'API服务', 'M', '/api-service', 'ServerOutline', 4, 0, NOW(6));

INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, status, create_time)
VALUES
    (UUIDV7(), @m_api, '接口网关', 'C',
     '/api-service/gateway',
     'api/gateway/index',
     'api:gateway:view',
     'PulseOutline', 1, 0, NOW(6)),

    (UUIDV7(), @m_api, '接口管理', 'C',
     '/api-service/definition',
     'api/definition/index',
     'api:definition:view',
     'DocumentTextOutline', 2, 0, NOW(6)),

    (UUIDV7(), @m_api, '流量控制', 'C',
     '/api-service/traffic',
     'api/traffic/index',
     'api:traffic:view',
     'SpeedometerOutline', 3, 0, NOW(6)),

    (UUIDV7(), @m_api, 'Mock服务', 'C',
     '/api-service/mock',
     'api/mock/index',
     'api:mock:view',
     'ConstructOutline', 4, 0, NOW(6));

/* ---------- 日志管理 ---------- */
INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, icon, sort, status, create_time)
VALUES
    (@m_log := UUIDV7(), '0', '日志管理', 'M', '/log-service', 'ReceiptOutline', 5, 0, NOW(6));

INSERT INTO sys_menu
(id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, status, create_time)
VALUES
    (UUIDV7(), @m_log, '日志查询', 'C',
     '/log-service/query',
     'log/query/index',
     'log:query:view',
     'SearchOutline', 1, 0, NOW(6)),

    (UUIDV7(), @m_log, '日志配置', 'C',
     '/log-service/config',
     'log/config/index',
     'log:config:view',
     'MedalOutline', 2, 0, NOW(6));

/* =====================================================
   5. 用户-角色
   ===================================================== */
INSERT INTO sys_user_role (user_id, role_id)
VALUES (@user_admin, @role_admin);

/* =====================================================
   6. 角色-菜单（超级管理员：全部）
   ===================================================== */
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @role_admin, id FROM sys_menu;

-- ============================================
-- 7. 初始化权限数据示例
-- ============================================
INSERT INTO sys_permission (id, perm_name, perm_code, description, sort, status, create_time, deleted) VALUES
-- 用户管理权限
(UUID(), '用户查询', 'user:read', '查询用户列表和详情', 1, 0, NOW(6), 0),
(UUID(), '用户新增', 'user:create', '新增用户', 2, 0, NOW(6), 0),
(UUID(), '用户编辑', 'user:update', '编辑用户信息', 3, 0, NOW(6), 0),
(UUID(), '用户删除', 'user:delete', '删除用户', 4, 0, NOW(6), 0),
-- 角色管理权限
(UUID(), '角色查询', 'role:read', '查询角色列表和详情', 10, 0, NOW(6), 0),
(UUID(), '角色新增', 'role:create', '新增角色', 11, 0, NOW(6), 0),
(UUID(), '角色编辑', 'role:update', '编辑角色信息', 12, 0, NOW(6), 0),
(UUID(), '角色删除', 'role:delete', '删除角色', 13, 0, NOW(6), 0),
-- 菜单管理权限
(UUID(), '菜单查询', 'menu:read', '查询菜单列表和详情', 20, 0, NOW(6), 0),
(UUID(), '菜单新增', 'menu:create', '新增菜单', 21, 0, NOW(6), 0),
(UUID(), '菜单编辑', 'menu:update', '编辑菜单信息', 22, 0, NOW(6), 0),
(UUID(), '菜单删除', 'menu:delete', '删除菜单', 23, 0, NOW(6), 0),
-- 部门管理权限
(UUID(), '部门查询', 'dept:read', '查询部门列表和详情', 30, 0, NOW(6), 0),
(UUID(), '部门新增', 'dept:create', '新增部门', 31, 0, NOW(6), 0),
(UUID(), '部门编辑', 'dept:update', '编辑部门信息', 32, 0, NOW(6), 0),
(UUID(), '部门删除', 'dept:delete', '删除部门', 33, 0, NOW(6), 0),
-- 权限管理权限
(UUID(), '权限查询', 'permission:read', '查询权限列表和详情', 40, 0, NOW(6), 0),
(UUID(), '权限新增', 'permission:create', '新增权限', 41, 0, NOW(6), 0),
(UUID(), '权限编辑', 'permission:update', '编辑权限信息', 42, 0, NOW(6), 0),
(UUID(), '权限删除', 'permission:delete', '删除权限', 43, 0, NOW(6), 0);

SET FOREIGN_KEY_CHECKS = 1;
