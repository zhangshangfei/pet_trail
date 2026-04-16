package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.CommentVO;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.PostComment;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PostCommentMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostCommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Long postId, Long userId, String content, Long parentId, Long replyToId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("动态不存在");
        }

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setReplyToId(replyToId);
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        post.setCommentCount((post.getCommentCount() != null ? post.getCommentCount() : 0) + 1);
        postMapper.updateById(post);

        if (!userId.equals(post.getUserId())) {
            notificationService.createNotification(
                post.getUserId(), userId, "comment", postId, "评论了你的动态");
        }

        if (replyToId != null) {
            PostComment replyToComment = commentMapper.selectById(replyToId);
            if (replyToComment != null && !userId.equals(replyToComment.getUserId())) {
                notificationService.createNotification(
                    replyToComment.getUserId(), userId, "comment", postId, "回复了你的评论");
            }
        }

        return convertToVO(comment);
    }

    public List<CommentVO> getComments(Long postId, int page, int size) {
        int offset = (page - 1) * size;

        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getStatus, 1)
                .orderByAsc(PostComment::getCreatedAt)
                .last("LIMIT " + offset + "," + size);

        List<PostComment> comments = commentMapper.selectList(wrapper);
        return buildCommentTree(comments);
    }

    public int getCommentCount(Long postId) {
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getStatus, 1);
        return Math.toIntExact(commentMapper.selectCount(wrapper));
    }

    private List<CommentVO> buildCommentTree(List<PostComment> comments) {
        List<CommentVO> allVOs = comments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Map<Long, CommentVO> voMap = allVOs.stream()
                .collect(Collectors.toMap(CommentVO::getId, v -> v));

        List<CommentVO> rootComments = new ArrayList<>();
        for (CommentVO vo : allVOs) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                rootComments.add(vo);
            } else {
                CommentVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getReplies() == null) {
                        parent.setReplies(new ArrayList<>());
                    }
                    parent.getReplies().add(vo);
                } else {
                    rootComments.add(vo);
                }
            }
        }

        return rootComments;
    }

    private CommentVO convertToVO(PostComment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setReplyToId(comment.getReplyToId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreatedAt(comment.getCreatedAt());

        try {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUserName(user.getNickname() != null ? user.getNickname() : "萌宠主人");
                vo.setUserAvatar(user.getAvatar() != null ? user.getAvatar() : "");
            } else {
                vo.setUserName("未知用户");
                vo.setUserAvatar("");
            }
        } catch (Exception e) {
            vo.setUserName("未知用户");
            vo.setUserAvatar("");
        }

        if (comment.getReplyToId() != null) {
            try {
                PostComment replyToComment = commentMapper.selectById(comment.getReplyToId());
                if (replyToComment != null) {
                    User replyToUser = userMapper.selectById(replyToComment.getUserId());
                    if (replyToUser != null) {
                        vo.setReplyToUserName(replyToUser.getNickname() != null ? replyToUser.getNickname() : "萌宠主人");
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return vo;
    }
}
