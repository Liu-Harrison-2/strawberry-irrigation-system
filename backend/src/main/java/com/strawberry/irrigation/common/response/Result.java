package com.strawberry.irrigation.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 说明：通用 Result<T> 提供 success / fail 静态工厂方法，
 * 保证所有 API 响应遵循 { code, message, data } 结构，便于前端统一处理。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;

    /* ---------- 静态构造便捷方法 ---------- */
    public static <T> Result<T> success() {
        return success(0, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return success(0, "success", data);
    }

    public static <T> Result<T> success(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> fail() {
        return fail(500, "error", null);
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message, null);
    }

    // ✅ 新增双参方法，简化调用
    public static <T> Result<T> fail(int code, String message) {
        return fail(code, message, null);
    }

    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
