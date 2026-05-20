package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.Report;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReportMapper extends BaseMapper<Report> {

    @Select("SELECT COUNT(*) FROM reports WHERE reporter_id = #{reporterId} AND target_id = #{targetId} AND target_type = #{targetType} AND deleted = 0")
    int countByReporterAndTarget(@Param("reporterId") Long reporterId,
                                 @Param("targetId") Long targetId,
                                 @Param("targetType") String targetType);
}
