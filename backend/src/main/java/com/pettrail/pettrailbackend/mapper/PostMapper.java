package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 动态 Mapper
 */
@Mapper
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
     * 根据日期范围查询动态
     */
    List<Post> selectByDateRange(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}
