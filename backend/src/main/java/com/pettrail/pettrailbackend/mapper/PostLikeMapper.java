package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 动态点赞 Mapper
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    /**
     * 根据动态 ID 和用户 ID 删除点赞
     */
    int deleteByPostIdAndUserId(@Param("postId") Long postId, 
                                @Param("userId") Long userId);

    /**
     * 统计动态点赞数
     */
    int countByPostId(@Param("postId") Long postId);
}
