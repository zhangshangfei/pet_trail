package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.WxSubscribeAuthorization;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface WxSubscribeAuthorizationMapper extends BaseMapper<WxSubscribeAuthorization> {

    @Update("UPDATE wx_subscribe_authorization SET credits = credits + #{delta}, updated_at = NOW() WHERE user_id = #{userId} AND template_type = #{templateType} AND credits + #{delta} >= 0")
    int updateCredits(@Param("userId") Long userId,
                      @Param("templateType") String templateType,
                      @Param("delta") int delta);

    @Update("UPDATE wx_subscribe_authorization SET used_credits = used_credits + 1, updated_at = NOW() WHERE user_id = #{userId} AND template_type = #{templateType}")
    int updateUsedCredits(@Param("userId") Long userId,
                          @Param("templateType") String templateType);
}
