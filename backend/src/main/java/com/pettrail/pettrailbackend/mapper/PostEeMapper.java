package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.PostEe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 动态收藏 Mapper
 */
@Mapper
public interface PostEeMapper extends BaseMapper<PostEe> {

    /**
     * 根据动态 ID 和用户 ID 删除收藏
     */
    int deleteByPostIdAndUserId(@Param("postId") Long postId,
                                @Param("userId") Long userId);

    /**
     * 统计动态收藏数
     */
    int countByPostId(@Param("postId") Long postId);
}
