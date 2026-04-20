package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.Tag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {

    @Select("SELECT * FROM tags WHERE name = #{name} AND status = 1 LIMIT 1")
    Tag selectByName(@Param("name") String name);

    @Select("SELECT * FROM tags WHERE is_hot = 1 AND status = 1 ORDER BY usage_count DESC LIMIT #{limit}")
    List<Tag> selectHotTags(@Param("limit") int limit);

    @Select("SELECT * FROM tags WHERE status = 1 AND name LIKE CONCAT('%', #{keyword}, '%') ORDER BY usage_count DESC LIMIT #{limit}")
    List<Tag> searchTags(@Param("keyword") String keyword, @Param("limit") int limit);

    @Update("UPDATE tags SET usage_count = usage_count + 1 WHERE id = #{tagId}")
    int incrementUsageCount(@Param("tagId") Long tagId);

    @Update("UPDATE tags SET usage_count = GREATEST(usage_count - 1, 0) WHERE id = #{tagId}")
    int decrementUsageCount(@Param("tagId") Long tagId);
}
