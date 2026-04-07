package com.pettrail.pettrailbackend.dto;

import com.pettrail.pettrailbackend.enums.ErrorCodeEnum;
import com.pettrail.pettrailbackend.exception.BusinessException;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> implements Serializable {

    private Boolean success;
    private Integer code;
    private T data;
    private String message;

    public Result() {
    }

    public Result(Boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = success ? 200 : 500;
    }

    public Result(Boolean success, Integer code, T data, String message) {
        this.success = success;
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(true, null, "操作成功");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, "操作成功");
    }

    /**
     * 成功响应（带数据和消息）
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, data, message);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(false, 500, null, message);
    }

    /**
     * 失败响应（带错误码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(false, code, null, message);
    }

    /**
     * 失败响应（使用错误码枚举）
     */
    public static <T> Result<T> error(ErrorCodeEnum errorCode) {
        return new Result<>(false, errorCode.getCode(), null, errorCode.getMessage());
    }

    /**
     * 失败响应（使用错误码枚举，自定义消息）
     */
    public static <T> Result<T> error(ErrorCodeEnum errorCode, String customMessage) {
        return new Result<>(false, errorCode.getCode(), null, customMessage);
    }

    /**
     * 从业务异常创建失败响应
     */
    public static <T> Result<T> fromException(BusinessException e) {
        return new Result<>(false, e.getCode(), null, e.getMessage());
    }

    /**
     * 从普通异常创建失败响应
     */
    public static <T> Result<T> fromException(Exception e) {
        return new Result<>(false, 500, null, e.getMessage());
    }
}
