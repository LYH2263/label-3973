package com.tcm.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.health.dto.HealthIndicatorDTO;
import com.tcm.health.entity.HealthIndicator;

import java.util.List;

public interface HealthIndicatorService extends IService<HealthIndicator> {
    List<HealthIndicator> listByRecordId(Long recordId);
    HealthIndicator create(HealthIndicatorDTO dto);
    void delete(Long id);
}
