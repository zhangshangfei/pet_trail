package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pettrail.pettrailbackend.entity.Post;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PostMapper extends BaseMapper<Post> {

    List<Post> selectByUserId(@Param("userId") Long userId, 
                              @Param("offset") int offset, 
                              @Param("limit") int limit);

    List<Post> selectFeed(@Param("offset") int offset, @Param("limit") int limit);

    List<Post> selectFollowFeed(@Param("followerId") Long followerId,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    List<Post> selectCollectFeed(@Param("userId") Long userId,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    List<Post> selectLikedFeed(@Param("userId") Long userId,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    List<Post> selectByDateRange(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    List<Post> selectPendingForAudit(@Param("beforeTime") LocalDateTime beforeTime);

    List<Post> selectRecommendFeed(@Param("offset") int offset, @Param("limit") int limit);

    List<Post> selectCandidatePosts(@Param("limit") int limit);

    @Update("UPDATE posts SET like_count = GREATEST(0, like_count + #{delta}) WHERE id = #{postId}")
    int updateLikeCountAtomic(@Param("postId") Long postId, @Param("delta") int delta);

    @Update("UPDATE posts SET ee_count = GREATEST(0, ee_count + #{delta}) WHERE id = #{postId}")
    int updateEeCountAtomic(@Param("postId") Long postId, @Param("delta") int delta);

    @Update("UPDATE posts SET comment_count = GREATEST(0, comment_count + #{delta}) WHERE id = #{postId}")
    int updateCommentCountAtomic(@Param("postId") Long postId, @Param("delta") int delta);

    @Select("SELECT * FROM posts WHERE id = #{id}")
    Post selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("UPDATE posts SET deleted = 0, status = CASE WHEN status IS NULL OR status = 0 THEN 1 ELSE status END, updated_at = NOW() WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);

    IPage<Post> selectDeletedPage(IPage<Post> page);
}
