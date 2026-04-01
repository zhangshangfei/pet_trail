package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.WeightRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeightRecordMapper extends BaseMapper<WeightRecord> {
}
