package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.Follow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 关注 Mapper
 */
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 根据关注者ID和被关注者ID删除关注
     */
    int deleteByFollowerIdAndFolloweeId(@Param("followerId") Long followerId,
                                         @Param("followeeId") Long followeeId);

    /**
     * 查询用户的粉丝数
     */
    int countFollowers(@Param("followeeId") Long followeeId);

    /**
     * 查询用户的关注数
     */
    int countFollowees(@Param("followerId") Long followerId);

    /**
     * 查询用户关注的用户ID列表
     */
    List<Long> selectFolloweeIds(@Param("followerId") Long followerId);

    List<Long> selectFollowerIds(@Param("followeeId") Long followeeId);
}
