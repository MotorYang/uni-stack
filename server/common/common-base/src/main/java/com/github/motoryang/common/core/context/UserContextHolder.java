package com.github.motoryang.common.core.context;

import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.common.core.result.ResultCode;

/**
 * 用户上下文持有者
 * <p>
 * 使用 ThreadLocal 存储当前线程的用户信息，
 * 由 UserContextFilter 在请求进入时设置，请求结束时清除
 */
public final class UserContextHolder {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    /**
     * 设置当前用户上下文
     */
    public static void setContext(LoginUser loginUser) {
        CONTEXT.set(loginUser);
    }

    /**
     * 获取当前用户上下文，如果不存在则抛出异常
     *
     * @return 当前登录用户
     * @throws BusinessException 未登录时抛出
     */
    public static LoginUser getContext() {
        LoginUser loginUser = CONTEXT.get();
        if (loginUser == null || loginUser.userId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    /**
     * 获取当前用户上下文，可能为 null
     *
     * @return 当前登录用户，未登录时返回 null
     */
    public static LoginUser getContextOrNull() {
        return CONTEXT.get();
    }

    /**
     * 清除当前用户上下文
     */
    public static void clearContext() {
        CONTEXT.remove();
    }

    /**
     * 获取当前用户ID，如果不存在则抛出异常
     */
    public static String getUserId() {
        return getContext().userId();
    }

    /**
     * 获取当前用户ID，可能为 null
     */
    public static String getUserIdOrNull() {
        LoginUser loginUser = CONTEXT.get();
        return loginUser != null ? loginUser.userId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return getContext().username();
    }

    /**
     * 判断当前用户是否拥有指定角色
     */
    public static boolean hasRole(String role) {
        LoginUser loginUser = CONTEXT.get();
        return loginUser != null && loginUser.hasRole(role);
    }

    /**
     * 判断当前用户是否拥有指定权限
     */
    public static boolean hasPermission(String permission) {
        LoginUser loginUser = CONTEXT.get();
        return loginUser != null && loginUser.hasPermission(permission);
    }

    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        LoginUser loginUser = CONTEXT.get();
        return loginUser != null && loginUser.isAdmin();
    }
}
