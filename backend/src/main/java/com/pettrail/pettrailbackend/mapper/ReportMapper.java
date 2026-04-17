package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    int countByReporterAndTarget(@Param("reporterId") Long reporterId,
                                 @Param("targetId") Long targetId,
                                 @Param("targetType") String targetType);
}
