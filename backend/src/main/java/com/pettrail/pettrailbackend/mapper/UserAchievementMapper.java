package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {

    @Select("SELECT * FROM user_achievements WHERE user_id = #{userId} AND achievement_id = #{achievementId}")
    UserAchievement selectByUserIdAndAchievementId(@Param("userId") Long userId, @Param("achievementId") Long achievementId);

    @Select("SELECT achievement_id FROM user_achievements WHERE user_id = #{userId} AND status >= 2")
    List<Long> selectUnlockedAchievementIds(@Param("userId") Long userId);
}
