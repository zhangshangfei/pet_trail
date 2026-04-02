package com.pettrail.pettrailbackend.exception;

/**
 * 权限不足异常
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(403, message);
    }
}
