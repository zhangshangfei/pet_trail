package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.CheckinReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CheckinReminderMapper extends BaseMapper<CheckinReminder> {

    @Select("SELECT * FROM checkin_reminders WHERE user_id = #{userId} AND is_enabled = 1")
    List<CheckinReminder> selectEnabledByUserId(@Param("userId") Long userId);
}
