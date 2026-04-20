package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.Post;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态 Mapper
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 根据用户 ID 查询动态列表
     */
    List<Post> selectByUserId(@Param("userId") Long userId, 
                              @Param("offset") int offset, 
                              @Param("limit") int limit);

    /**
     * 查询动态列表（按时间倒序）
     */
    List<Post> selectFeed(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询关注用户的动态列表（按时间倒序）
     */
    List<Post> selectFollowFeed(@Param("followerId") Long followerId,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    /**
     * 查询用户收藏的动态列表（按时间倒序）
     */
    List<Post> selectCollectFeed(@Param("userId") Long userId,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    List<Post> selectLikedFeed(@Param("userId") Long userId,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    /**
     * 根据日期范围查询动态
     */
    List<Post> selectByDateRange(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    /**
     * 查询待审核的动态（创建时间早于指定时间）
     * 用于定时任务兜底处理
     */
    List<Post> selectPendingForAudit(@Param("beforeTime") LocalDateTime beforeTime);

    List<Post> selectRecommendFeed(@Param("offset") int offset, @Param("limit") int limit);

    List<Post> selectCandidatePosts(@Param("limit") int limit);
}
