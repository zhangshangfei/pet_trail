package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.CheckinItem;

import java.util.List;

/**
 * 打卡�?Mapper
 */
public interface CheckinItemMapper extends BaseMapper<CheckinItem> {

    /**
     * 查询默认打卡项
     */
    List<CheckinItem> selectDefaultItems();

    /**
     * 查询用户自定义打卡项
     */
    List<CheckinItem> selectByUserId(Long userId);
}
