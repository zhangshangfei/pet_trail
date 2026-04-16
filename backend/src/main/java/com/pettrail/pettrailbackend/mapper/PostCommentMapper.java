package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
}
