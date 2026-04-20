package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.UserMembership;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMembershipMapper extends BaseMapper<UserMembership> {
}
