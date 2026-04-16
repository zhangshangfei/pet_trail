package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.util.NicknameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 微信登录
     */
    public User login(String openid) {
        // 查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenid, openid);
        User user = this.getOne(queryWrapper);

        if (user == null) {
            // 创建新用户
            user = new User();
            user.setOpenid(openid);
            user.setNickname(NicknameUtil.generateRandomNickname());
            this.save(user);
            log.info("创建新用户: id={}, openid={}", user.getId(), openid);
        } else {
            log.info("用户登录: id={}, openid={}", user.getId(), openid);
        }

        return user;
    }

    /**
     * 用户注册
     */
    public User register(String openid, String unionid, String nickname, String avatar) {
        // 检查用户是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.or().eq(User::getOpenid, openid);
        if (unionid != null) {
            queryWrapper.or().eq(User::getUnionid, unionid);
        }

        User existingUser = this.getOne(queryWrapper);
        if (existingUser != null) {
            throw new RuntimeException("用户已存在");
        }

        // 创建新用户
        User user = new User();
        user.setOpenid(openid);
        user.setUnionid(unionid);
        user.setNickname(nickname != null ? nickname : "萌宠主人");
        user.setAvatar(avatar);
        this.save(user);

        log.info("用户注册成功: id={}, openid={}, nickname={}", user.getId(), openid, user.getNickname());
        return user;
    }

    /**
     * 获取用户资料
     */
    public User getProfile(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    /**
     * 更新用户资料
     */
    public User updateProfile(
            Long userId,
            String nickname,
            String avatar,
            String phone,
            Integer gender) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (gender != null) {
            user.setGender(gender);
        }

        this.updateById(user);
        log.info("更新用户资料成功: userId={}", userId);
        return user;
    }

    public List<User> discoverUsers(Long currentUserId, String type, String keyword, int page, int size) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (currentUserId != null) {
            queryWrapper.ne(User::getId, currentUserId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(User::getNickname, keyword.trim());
        }

        switch (type) {
            case "hot":
                queryWrapper.orderByDesc(User::getId);
                break;
            case "new":
                queryWrapper.orderByDesc(User::getCreatedAt);
                break;
            case "recommend":
            default:
                queryWrapper.orderByDesc(User::getId);
                break;
        }

        queryWrapper.last("LIMIT " + (page - 1) * size + ", " + size);

        return this.list(queryWrapper);
    }
}
