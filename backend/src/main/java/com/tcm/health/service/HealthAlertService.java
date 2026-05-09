package com.tcm.health.service;

import com.tcm.health.entity.HealthAlert;

import java.util.List;

public interface HealthAlertService {
    List<HealthAlert> listByRecordId(Long recordId);

    void saveAlert(HealthAlert alert);
}
