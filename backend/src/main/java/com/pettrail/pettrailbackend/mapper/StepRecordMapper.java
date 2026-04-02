package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.StepRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 步数记录 Mapper
 */
@Mapper
public interface StepRecordMapper extends BaseMapper<StepRecord> {

    /**
     * 根据用户 ID、宠物 ID 和日期查询步数记录
     */
    StepRecord selectByUserIdPetIdDate(@Param("userId") Long userId,
                                        @Param("petId") Long petId,
                                        @Param("date") LocalDate date);

    /**
     * 查询步数趋势
     */
    List<StepRecord> selectTrend(@Param("userId") Long userId,
                                 @Param("petId") Long petId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}
