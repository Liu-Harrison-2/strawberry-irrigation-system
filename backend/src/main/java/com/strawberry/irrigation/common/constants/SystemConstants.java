package com.strawberry.irrigation.common.constants;

/**
 * 系统常量统一管理
 * 避免硬编码，便于维护和修改
 */
public class SystemConstants {

    // ===== 响应状态码 =====
    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = 500;
    public static final int BUSINESS_ERROR_CODE = 400;
    public static final int UNAUTHORIZED_CODE = 401;
    public static final int FORBIDDEN_CODE = 403;

    // ===== 响应消息 =====
    public static final String SUCCESS_MESSAGE = "操作成功";
    public static final String ERROR_MESSAGE = "系统错误";
    public static final String BUSINESS_ERROR_MESSAGE = "业务处理失败";

    // ===== 用户相关常量 =====
    public static final String USER_TYPE_ADMIN = "ADMIN";
    public static final String USER_TYPE_FARMER = "FARMER";

    public static final String USER_STATUS_ACTIVE = "ACTIVE";
    public static final String USER_STATUS_INACTIVE = "INACTIVE";
    public static final String USER_STATUS_BANNED = "BANNED";

    // ===== 用户操作消息 =====
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String USER_ALREADY_EXISTS = "用户已存在";
    public static final String USER_CREATE_SUCCESS = "用户创建成功";
    public static final String USER_UPDATE_SUCCESS = "用户信息更新成功";
    public static final String USER_DELETE_SUCCESS = "用户删除成功";
}