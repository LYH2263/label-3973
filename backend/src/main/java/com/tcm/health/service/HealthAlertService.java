package com.tcm.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.health.entity.HealthAlert;
import com.tcm.health.entity.HealthIndicator;

import java.util.List;

public interface HealthAlertService extends IService<HealthAlert> {
    List<HealthAlert> listByRecordId(Long recordId);
    void detectAndSaveAlerts(HealthIndicator indicator);
}
