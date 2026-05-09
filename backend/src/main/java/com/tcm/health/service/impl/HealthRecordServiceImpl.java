package com.tcm.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.health.dto.HealthRecordDTO;
import com.tcm.health.entity.HealthRecord;
import com.tcm.health.mapper.HealthRecordMapper;
import com.tcm.health.service.HealthRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements HealthRecordService {

    @Override
    public List<HealthRecord> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<HealthRecord>()
                .eq(HealthRecord::getUserId, userId)
                .orderByDesc(HealthRecord::getCreatedAt));
    }

    @Override
    public HealthRecord createRecord(Long userId, HealthRecordDTO dto) {
        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        copyDtoToEntity(dto, record);
        save(record);
        return record;
    }

    @Override
    public HealthRecord updateRecord(Long id, Long userId, HealthRecordDTO dto) {
        HealthRecord record = getById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("档案不存在或无权操作");
        }
        copyDtoToEntity(dto, record);
        updateById(record);
        return record;
    }

    @Override
    public void deleteRecord(Long id, Long userId) {
        HealthRecord record = getById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("档案不存在或无权操作");
        }
        removeById(id);
    }

    private void copyDtoToEntity(HealthRecordDTO dto, HealthRecord record) {
        record.setName(dto.getName());
        record.setGender(dto.getGender());
        record.setAge(dto.getAge());
        record.setBloodType(dto.getBloodType());
        record.setAllergy(dto.getAllergy());
        record.setMedicalHistory(dto.getMedicalHistory());
    }
}
