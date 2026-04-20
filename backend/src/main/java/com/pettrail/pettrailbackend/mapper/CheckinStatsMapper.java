package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.CheckinStats;
import org.apache.ibatis.annotations.Param;

/**
 * 打卡统计 Mapper
 */
public interface CheckinStatsMapper extends BaseMapper<CheckinStats> {

    /**
     * 根据用户 ID 和打卡项 ID 查询统计
     */
    CheckinStats selectByUserIdAndItemId(@Param("userId") Long userId, 
                                          @Param("itemId") Long itemId);
}
