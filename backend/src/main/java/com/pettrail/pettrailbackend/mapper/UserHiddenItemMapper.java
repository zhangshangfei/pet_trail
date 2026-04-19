package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.UserHiddenItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserHiddenItemMapper extends BaseMapper<UserHiddenItem> {

    List<Long> selectHiddenItemIdsByUserId(Long userId);
}
