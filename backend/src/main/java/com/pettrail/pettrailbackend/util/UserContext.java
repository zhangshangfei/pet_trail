package com.pettrail.pettrailbackend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @program: pet-trail
 * @description:
 * @author: zsf
 * @create: 2026-03-27 14:11
 **/
public class UserContext {

    /**
     * 获取当前登录用户的 ID
     * 这里利用了 Spring Security 的机制：
     * 我们在过滤器中把 userId 设置为了 Authentication 的 principal 对象
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 如果需要获取更复杂的用户对象，也可以在这里扩展
     */
    public static UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        return null;
    }

}
