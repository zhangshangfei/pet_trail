package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 疫苗提醒 Mapper
 */
@Mapper
public interface VaccineReminderMapper extends BaseMapper<VaccineReminder> {

    /**
     * 查询即将到期的疫苗提醒
     */
    List<VaccineReminder> selectDueReminders(@Param("userId") Long userId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 查询宠物的疫苗提醒
     */
    List<VaccineReminder> selectByPetId(@Param("petId") Long petId);
}
