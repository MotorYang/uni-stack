package com.github.motoryang.common.utils;

import com.github.motoryang.common.core.context.LoginUser;
import com.github.motoryang.common.core.context.UserContextHolder;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.common.core.result.ResultCode;

/**
 * 安全工具类
 * <p>
 * 用于获取当前登录用户信息、判断角色和权限
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前用户ID，如果未登录则抛出异常
     */
    public static String getUserId() {
        return UserContextHolder.getUserId();
    }

    /**
     * 获取当前用户ID，未登录时返回 null
     */
    public static String getUserIdOrNull() {
        return UserContextHolder.getUserIdOrNull();
    }

    /**
     * 获取当前登录用户信息
     */
    public static LoginUser getLoginUser() {
        return UserContextHolder.getContext();
    }

    /**
     * 获取当前登录用户信息，未登录时返回 null
     */
    public static LoginUser getLoginUserOrNull() {
        return UserContextHolder.getContextOrNull();
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return UserContextHolder.getUsername();
    }

    /**
     * 判断当前用户是否拥有指定角色
     *
     * @param role 角色编码
     */
    public static boolean hasRole(String role) {
        return UserContextHolder.hasRole(role);
    }

    /**
     * 判断当前用户是否拥有任意一个角色
     *
     * @param roles 角色编码数组
     */
    public static boolean hasAnyRole(String... roles) {
        LoginUser loginUser = UserContextHolder.getContextOrNull();
        return loginUser != null && loginUser.hasAnyRole(roles);
    }

    /**
     * 判断当前用户是否拥有指定权限
     *
     * @param permission 权限编码
     */
    public static boolean hasPermission(String permission) {
        return UserContextHolder.hasPermission(permission);
    }

    /**
     * 判断当前用户是否拥有任意一个权限
     *
     * @param permissions 权限编码数组
     */
    public static boolean hasAnyPermission(String... permissions) {
        LoginUser loginUser = UserContextHolder.getContextOrNull();
        return loginUser != null && loginUser.hasAnyPermission(permissions);
    }

    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        return UserContextHolder.isAdmin();
    }

    /**
     * 校验当前用户是否拥有指定角色，没有则抛出异常
     *
     * @param role 角色编码
     */
    public static void checkRole(String role) {
        if (!hasRole(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    /**
     * 校验当前用户是否拥有指定权限，没有则抛出异常
     *
     * @param permission 权限编码
     */
    public static void checkPermission(String permission) {
        if (!hasPermission(permission)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}

