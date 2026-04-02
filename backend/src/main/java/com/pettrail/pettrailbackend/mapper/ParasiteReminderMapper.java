package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 驱虫提醒 Mapper
 */
@Mapper
public interface ParasiteReminderMapper extends BaseMapper<ParasiteReminder> {

    /**
     * 查询即将到期的驱虫提醒
     */
    List<ParasiteReminder> selectDueReminders(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 查询宠物的驱虫提醒
     */
    List<ParasiteReminder> selectByPetId(@Param("petId") Long petId);
}
