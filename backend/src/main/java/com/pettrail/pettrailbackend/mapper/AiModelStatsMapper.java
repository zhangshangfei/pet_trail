package com.pettrail.pettrailbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pettrail.pettrailbackend.entity.AiModelStats;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AiModelStatsMapper extends BaseMapper<AiModelStats> {

    @Select("SELECT COALESCE(SUM(call_count), 0) as total_calls, " +
            "COALESCE(SUM(success_count), 0) as success_calls, " +
            "COALESCE(SUM(fail_count), 0) as failed_calls, " +
            "CASE WHEN SUM(call_count) > 0 THEN ROUND(SUM(success_count) * 100.0 / SUM(call_count), 1) ELSE 0 END as success_rate, " +
            "CASE WHEN SUM(call_count) > 0 THEN ROUND(SUM(total_response_time) * 1.0 / SUM(call_count), 0) ELSE 0 END as avg_response_time " +
            "FROM ai_model_stats WHERE model_id = #{modelId}")
    Map<String, Object> selectAggregatedStats(@Param("modelId") Long modelId);

    @Select("SELECT COALESCE(SUM(call_count), 0) as total_calls, " +
            "COALESCE(SUM(success_count), 0) as success_calls, " +
            "COALESCE(SUM(fail_count), 0) as failed_calls, " +
            "COALESCE(SUM(total_response_time), 0) as total_response_time, " +
            "CASE WHEN SUM(call_count) > 0 THEN ROUND(SUM(success_count) * 100.0 / SUM(call_count), 1) ELSE 0 END as success_rate, " +
            "CASE WHEN SUM(call_count) > 0 THEN ROUND(SUM(total_response_time) * 1.0 / SUM(call_count), 0) ELSE 0 END as avg_response_time " +
            "FROM ai_model_stats")
    Map<String, Object> selectGlobalAggregatedStats();

    @Select("SELECT stats_date, call_count, success_count, fail_count, avg_response_time, success_rate " +
            "FROM ai_model_stats WHERE model_id = #{modelId} ORDER BY stats_date DESC LIMIT #{limit}")
    List<Map<String, Object>> selectDailyStats(@Param("modelId") Long modelId, @Param("limit") int limit);
}
