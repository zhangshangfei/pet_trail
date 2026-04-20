package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.exception.UnauthorizedException;
import com.pettrail.pettrailbackend.util.UserContext;

public class BaseController {

    protected Long requireLogin() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }
        return userId;
    }
}
