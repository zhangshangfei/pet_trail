package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.controller.BaseController;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.UserMapper;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseAdminController extends BaseController {

    protected Map<Long, String> buildNicknameMap(Set<Long> userIds, UserMapper userMapper) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> u.getNickname() != null ? u.getNickname() : "用户" + u.getId(),
                        (a, b) -> a));
    }
}
