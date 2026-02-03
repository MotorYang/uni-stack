package com.github.motoryang.common.core.context;

import java.util.Collections;
import java.util.Set;

/**
 * 登录用户上下文信息
 *
 * @param userId      用户ID
 * @param username    用户名
 * @param roles       角色编码集合
 * @param permissions 权限编码集合
 */
public record LoginUser(
        String userId,
        String username,
        Set<String> roles,
        Set<String> permissions
) {

    /**
     * 是否拥有指定角色
     *
     * @param role 角色编码
     * @return true-拥有，false-没有
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 是否拥有任意一个角色
     *
     * @param roles 角色编码数组
     * @return true-拥有至少一个，false-都没有
     */
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }
        for (String role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否拥有指定权限
     *
     * @param permission 权限编码
     * @return true-拥有，false-没有
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 是否拥有任意一个权限
     *
     * @param permissions 权限编码数组
     * @return true-拥有至少一个，false-都没有
     */
    public boolean hasAnyPermission(String... permissions) {
        if (this.permissions == null || this.permissions.isEmpty()) {
            return false;
        }
        for (String permission : permissions) {
            if (this.permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是管理员
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 创建空的 LoginUser（用于匿名访问场景）
     */
    public static LoginUser anonymous() {
        return new LoginUser(null, null, Collections.emptySet(), Collections.emptySet());
    }
}
