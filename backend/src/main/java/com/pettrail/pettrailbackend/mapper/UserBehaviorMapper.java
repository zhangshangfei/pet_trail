package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.UserBehavior;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    @Select("SELECT target_id FROM user_behaviors WHERE user_id = #{userId} AND action = #{action} AND target_type = #{targetType} ORDER BY created_at DESC LIMIT #{limit}")
    List<Long> selectRecentTargetIds(@Param("userId") Long userId, @Param("action") String action, @Param("targetType") String targetType, @Param("limit") int limit);

    @Select("SELECT action, COUNT(*) as cnt FROM user_behaviors WHERE user_id = #{userId} AND created_at >= #{since} GROUP BY action")
    List<Map<String, Object>> countByActionSince(@Param("userId") Long userId, @Param("since") String since);

    @Select("SELECT target_id, COUNT(*) as cnt FROM user_behaviors WHERE user_id = #{userId} AND action = 'view' AND target_type = 'post' AND created_at >= #{since} GROUP BY target_id ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> selectMostViewedPosts(@Param("userId") Long userId, @Param("since") String since, @Param("limit") int limit);
}
