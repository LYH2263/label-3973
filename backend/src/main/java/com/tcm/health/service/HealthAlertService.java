package com.tcm.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.health.entity.HealthAlert;

import java.util.List;

public interface HealthAlertService extends IService<HealthAlert> {
    List<HealthAlert> listByRecordId(Long recordId);
}
