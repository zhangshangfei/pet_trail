package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.UserHiddenItem;

import java.util.List;

public interface UserHiddenItemMapper extends BaseMapper<UserHiddenItem> {

    List<Long> selectHiddenItemIdsByUserId(Long userId);
}
