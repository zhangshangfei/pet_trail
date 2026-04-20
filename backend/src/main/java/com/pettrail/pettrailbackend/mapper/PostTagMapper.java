package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    @Select("SELECT tag_id FROM post_tags WHERE post_id = #{postId}")
    List<Long> selectTagIdsByPostId(@Param("postId") Long postId);

    @Select("SELECT post_id FROM post_tags WHERE tag_id = #{tagId} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<Long> selectPostIdsByTagId(@Param("tagId") Long tagId, @Param("offset") int offset, @Param("size") int size);
}
