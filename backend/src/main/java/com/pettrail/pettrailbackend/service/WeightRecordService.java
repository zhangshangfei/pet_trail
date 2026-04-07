package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pettrail.pettrailbackend.entity.WeightRecord;
import com.pettrail.pettrailbackend.enums.ErrorCodeEnum;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.WeightRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeightRecordService extends ServiceImpl<WeightRecordMapper, WeightRecord> {

    /**
     * 获取宠物的体重记录
     */
    public List<WeightRecord> listByPetId(Long petId) {
        LambdaQueryWrapper<WeightRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WeightRecord::getPetId, petId);
        queryWrapper.orderByDesc(WeightRecord::getRecordDate);
        return this.list(queryWrapper);
    }

    /**
     * 获取指定时间范围的体重记录
     */
    public List<WeightRecord> listByPetIdAndDateRange(Long petId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<WeightRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WeightRecord::getPetId, petId);
        queryWrapper.ge(WeightRecord::getRecordDate, startDate);
        queryWrapper.le(WeightRecord::getRecordDate, endDate);
        queryWrapper.orderByAsc(WeightRecord::getRecordDate);
        return this.list(queryWrapper);
    }

    /**
     * 创建体重记录
     */
    @Transactional(rollbackFor = Exception.class)
    public WeightRecord createRecord(Long petId, BigDecimal weight, LocalDate recordDate) {
        // 检查是否已存在当天记录
        LambdaQueryWrapper<WeightRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WeightRecord::getPetId, petId);
        queryWrapper.eq(WeightRecord::getRecordDate, recordDate);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCodeEnum.WEIGHT_RECORD_DUPLICATE.getCode(), ErrorCodeEnum.WEIGHT_RECORD_DUPLICATE.getMessage());
        }

        WeightRecord record = new WeightRecord();
        record.setPetId(petId);
        record.setWeight(weight);
        record.setRecordDate(recordDate);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        this.save(record);
        log.info("创建体重记录成功: id={}, petId={}, weight={}, recordDate={}",
                record.getId(), petId, weight, recordDate);
        return record;
    }

    /**
     * 更新体重记录
     */
    @Transactional(rollbackFor = Exception.class)
    public WeightRecord updateRecord(Long recordId, BigDecimal weight, LocalDate recordDate) {
        WeightRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCodeEnum.WEIGHT_RECORD_NOT_FOUND.getCode(), ErrorCodeEnum.WEIGHT_RECORD_NOT_FOUND.getMessage());
        }

        // 检查是否已存在其他记录的当天记录
        LambdaQueryWrapper<WeightRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WeightRecord::getPetId, record.getPetId());
        queryWrapper.eq(WeightRecord::getRecordDate, recordDate);
        queryWrapper.ne(WeightRecord::getId, recordId); // 排除当前记录
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCodeEnum.WEIGHT_RECORD_DUPLICATE.getCode(), ErrorCodeEnum.WEIGHT_RECORD_DUPLICATE.getMessage());
        }

        record.setWeight(weight);
        record.setRecordDate(recordDate);
        record.setUpdatedAt(LocalDateTime.now());

        this.updateById(record);
        log.info("更新体重记录成功: id={}, petId={}, weight={}, recordDate={}",
                record.getId(), record.getPetId(), weight, recordDate);
        return record;
    }

    /**
     * 获取宠物最后一次体重记录
     */
    public WeightRecord getLastRecord(Long petId) {
        LambdaQueryWrapper<WeightRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WeightRecord::getPetId, petId);
        queryWrapper.orderByDesc(WeightRecord::getRecordDate);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 获取宠物体重变化趋势
     */
    public List<WeightRecord> getTrend(Long petId, int days) {
        LambdaQueryWrapper<WeightRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WeightRecord::getPetId, petId);
        queryWrapper.ge(WeightRecord::getRecordDate, LocalDate.now().minusDays(days));
        queryWrapper.orderByAsc(WeightRecord::getRecordDate);
        return this.list(queryWrapper);
    }

    /**
     * 删除体重记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long recordId) {
        WeightRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        this.removeById(recordId);
        log.info("删除体重记录成功: recordId={}", recordId);
    }
}
