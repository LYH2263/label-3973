package com.tcm.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.health.dto.HealthRecordDTO;
import com.tcm.health.entity.HealthRecord;

import java.util.List;

public interface HealthRecordService extends IService<HealthRecord> {
    List<HealthRecord> listByUserId(Long userId);
    HealthRecord createRecord(Long userId, HealthRecordDTO dto);
    HealthRecord updateRecord(Long id, Long userId, HealthRecordDTO dto);
    void deleteRecord(Long id, Long userId);
}
