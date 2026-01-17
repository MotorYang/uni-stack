-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `frame` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 创建用户（指定身份验证插件以确保兼容性）
CREATE USER 'motoryang'@'%' IDENTIFIED WITH caching_sha2_password BY '745700Yxy@';

-- 3. 授予对 'frame' 数据库的完全控制权
GRANT ALL PRIVILEGES ON `frame`.* TO 'motoryang'@'%';

-- 4. 刷新权限使配置立即生效
FLUSH PRIVILEGES;