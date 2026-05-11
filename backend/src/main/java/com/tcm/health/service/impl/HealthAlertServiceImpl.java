package com.tcm.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.health.entity.HealthAlert;
import com.tcm.health.mapper.HealthAlertMapper;
import com.tcm.health.service.HealthAlertService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthAlertServiceImpl extends ServiceImpl<HealthAlertMapper, HealthAlert> implements HealthAlertService {

    @Override
    public List<HealthAlert> listByRecordId(Long recordId) {
        return list(new LambdaQueryWrapper<HealthAlert>()
                .eq(HealthAlert::getRecordId, recordId)
                .orderByDesc(HealthAlert::getAlertTime));
    }
}
