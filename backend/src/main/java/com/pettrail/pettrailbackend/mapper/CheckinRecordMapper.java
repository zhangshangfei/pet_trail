package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 打卡记录 Mapper
 */
@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

    /**
     * 根据用户 ID 和日期范围查询打卡记录
     */
    List<CheckinRecord> selectByUserIdAndDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * 查询用户某日的打卡记录
     */
    List<CheckinRecord> selectByUserIdAndDate(@Param("userId") Long userId,
                                              @Param("date") LocalDate date);

    /**
     * 检查用户某日是否已打卡
     */
    CheckinRecord selectByUserIdItemIdAndDate(@Param("userId") Long userId,
                                               @Param("itemId") Long itemId,
                                               @Param("date") LocalDate date);
}
