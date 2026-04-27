package com.pettrail.pettrailbackend.enums;

import lombok.Getter;

/**
 * 错误码枚举
 * 
 * 错误码规范：
 * - 1xxx: 用户相关错误
 * - 2xxx: 宠物相关错误
 * - 3xxx: 健康记录相关错误
 * - 4xxx: 社区相关错误
 * - 45xx: 文件上传相关
 * - 5xxx: 系统通用错误
 */
@Getter
public enum ErrorCodeEnum {

    // ==================== 用户相关 (1xxx) ====================
    USER_NOT_LOGIN(1001, "用户未登录"),
    USER_NOT_FOUND(1002, "用户不存在"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),
    LOGIN_FAILED(1004, "登录失败"),
    TOKEN_EXPIRED(1005, "登录已过期，请重新登录"),
    PASSWORD_ERROR(1006, "密码错误"),
    NICKNAME_DUPLICATE(1007, "昵称已存在"),

    // ==================== 宠物相关 (2xxx) ====================
    PET_NOT_FOUND(2001, "宠物不存在"),
    PET_NOT_OWNED(2002, "无权限操作该宠物"),
    PET_NAME_DUPLICATE(2003, "宠物昵称已存在"),
    PET_STATUS_ERROR(2004, "宠物状态异常"),

    // ==================== 健康记录相关 (3xxx) ====================
    WEIGHT_RECORD_DUPLICATE(3001, "当天已记录体重"),
    WEIGHT_RECORD_NOT_FOUND(3002, "体重记录不存在"),
    HEALTH_RECORD_NOT_FOUND(3005, "健康记录不存在"),

    // ==================== 社区相关 (4xxx) ====================
    POST_NOT_FOUND(4001, "帖子不存在"),
    COMMENT_NOT_FOUND(4002, "评论不存在"),
    POST_DELETED(4003, "帖子已删除"),
    COMMENT_LIMIT(4004, "评论频率过高，请稍后再试"),

    // ==================== 文件上传相关 (45xx) ====================
    FILE_EMPTY(4501, "文件不能为空"),
    FILE_SIZE_EXCEEDED(4502, "文件大小超出限制"),
    FILE_TYPE_ERROR(4503, "不支持的文件类型"),
    FILE_UPLOAD_FAILED(4504, "文件上传失败"),

    // ==================== 系统通用 (5xxx) ====================
    SYSTEM_ERROR(5000, "系统内部错误，请稍后重试"),
    PARAM_ERROR(5001, "参数错误"),
    PARAM_MISSING(5002, "缺少必要参数"),
    REQUEST_METHOD_NOT_SUPPORTED(5003, "不支持该请求方法"),
    SERVICE_UNAVAILABLE(5004, "服务暂时不可用"),
    RATE_LIMIT_EXCEEDED(5005, "请求过于频繁，请稍后再试");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    ErrorCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
