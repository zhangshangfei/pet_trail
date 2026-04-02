package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.WaterRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 饮水记录 Mapper
 */
@Mapper
public interface WaterRecordMapper extends BaseMapper<WaterRecord> {

    /**
     * 统计指定日期的饮水量
     */
    BigDecimal sumByUserIdPetIdDate(@Param("userId") Long userId,
                                     @Param("petId") Long petId,
                                     @Param("date") LocalDate date);

    /**
     * 查询每日饮水量汇总
     */
    List<WaterRecord> selectDailySum(@Param("userId") Long userId,
                                     @Param("petId") Long petId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}
