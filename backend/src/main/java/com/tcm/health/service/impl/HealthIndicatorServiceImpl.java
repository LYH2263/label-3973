package com.tcm.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.health.dto.HealthIndicatorDTO;
import com.tcm.health.entity.HealthIndicator;
import com.tcm.health.mapper.HealthIndicatorMapper;
import com.tcm.health.service.HealthIndicatorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthIndicatorServiceImpl extends ServiceImpl<HealthIndicatorMapper, HealthIndicator> implements HealthIndicatorService {

    @Override
    public List<HealthIndicator> listByRecordId(Long recordId) {
        return list(new LambdaQueryWrapper<HealthIndicator>()
                .eq(HealthIndicator::getRecordId, recordId)
                .orderByDesc(HealthIndicator::getCreatedAt));
    }

    @Override
    public HealthIndicator create(HealthIndicatorDTO dto) {
        HealthIndicator indicator = new HealthIndicator();
        indicator.setRecordId(dto.getRecordId());
        indicator.setHeight(dto.getHeight());
        indicator.setWeight(dto.getWeight());
        indicator.setBloodPressureHigh(dto.getBloodPressureHigh());
        indicator.setBloodPressureLow(dto.getBloodPressureLow());
        indicator.setBloodSugar(dto.getBloodSugar());
        indicator.setHeartRate(dto.getHeartRate());
        indicator.setRemark(dto.getRemark());
        save(indicator);
        return indicator;
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }
}
