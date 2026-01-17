package com.github.motoryang.common.core.result;

import java.io.Serializable;

/**
 * 统一响应结果封装
 */
public record RestResult<T>(
        int code,
        String message,
        T data
) implements Serializable {

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;

    public static <T> RestResult<T> ok() {
        return new RestResult<>(SUCCESS_CODE, "操作成功", null);
    }

    public static <T> RestResult<T> ok(T data) {
        return new RestResult<>(SUCCESS_CODE, "操作成功", data);
    }

    public static <T> RestResult<T> ok(String message, T data) {
        return new RestResult<>(SUCCESS_CODE, message, data);
    }

    public static <T> RestResult<T> fail() {
        return new RestResult<>(FAIL_CODE, "操作失败", null);
    }

    public static <T> RestResult<T> fail(String message) {
        return new RestResult<>(FAIL_CODE, message, null);
    }

    public static <T> RestResult<T> fail(int code, String message) {
        return new RestResult<>(code, message, null);
    }

    public static <T> RestResult<T> fail(ResultCode resultCode) {
        return new RestResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public boolean isSuccess() {
        return this.code == SUCCESS_CODE;
    }
}

