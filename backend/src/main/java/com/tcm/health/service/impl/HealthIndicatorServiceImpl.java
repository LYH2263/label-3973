package com.tcm.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.health.dto.HealthIndicatorDTO;
import com.tcm.health.entity.HealthAlert;
import com.tcm.health.entity.HealthIndicator;
import com.tcm.health.mapper.HealthIndicatorMapper;
import com.tcm.health.service.HealthAlertService;
import com.tcm.health.service.HealthIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthIndicatorServiceImpl extends ServiceImpl<HealthIndicatorMapper, HealthIndicator> implements HealthIndicatorService {

    @Autowired
    private HealthAlertService healthAlertService;

    @Override
    public List<HealthIndicator> listByRecordId(Long recordId) {
        return list(new LambdaQueryWrapper<HealthIndicator>()
                .eq(HealthIndicator::getRecordId, recordId)
                .orderByDesc(HealthIndicator::getCreatedAt));
    }

    @Override
    @Transactional
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

        detectAndGenerateAlerts(indicator);

        return indicator;
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    private void detectAndGenerateAlerts(HealthIndicator indicator) {
        List<HealthAlert> alerts = new ArrayList<>();

        if (indicator.getHeight() != null && indicator.getWeight() != null) {
            BigDecimal height = indicator.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal bmi = indicator.getWeight().divide(height.multiply(height), 1, RoundingMode.HALF_UP);

            if (bmi.compareTo(new BigDecimal("18.5")) < 0) {
                alerts.add(createAlert(indicator.getRecordId(), "BMI", "偏瘦", "BMI为" + bmi + "，低于正常值"));
            } else if (bmi.compareTo(new BigDecimal("24")) >= 0 && bmi.compareTo(new BigDecimal("28")) < 0) {
                alerts.add(createAlert(indicator.getRecordId(), "BMI", "超重", "BMI为" + bmi + "，属于超重范围"));
            } else if (bmi.compareTo(new BigDecimal("28")) >= 0) {
                alerts.add(createAlert(indicator.getRecordId(), "BMI", "肥胖", "BMI为" + bmi + "，属于肥胖范围"));
            }
        }

        if (indicator.getHeartRate() != null) {
            int hr = indicator.getHeartRate();
            if (hr < 60 || hr > 100) {
                alerts.add(createAlert(indicator.getRecordId(), "心率", "异常", "心率为" + hr + "次/分，不在正常范围(60-100)内"));
            }
        }

        if (indicator.getBloodPressureHigh() != null || indicator.getBloodPressureLow() != null) {
            Integer high = indicator.getBloodPressureHigh();
            Integer low = indicator.getBloodPressureLow();
            boolean abnormal = (high != null && (high >= 140 || high < 90)) || (low != null && (low >= 90 || low < 60));
            if (abnormal) {
                alerts.add(createAlert(indicator.getRecordId(), "血压", "异常", "血压为" + high + "/" + low + "mmHg，不在正常范围"));
            }
        }

        if (indicator.getBloodSugar() != null) {
            BigDecimal sugar = indicator.getBloodSugar();
            if (sugar.compareTo(new BigDecimal("7.0")) >= 0) {
                alerts.add(createAlert(indicator.getRecordId(), "血糖", "异常", "血糖为" + sugar + "mmol/L，偏高"));
            } else if (sugar.compareTo(new BigDecimal("3.9")) < 0) {
                alerts.add(createAlert(indicator.getRecordId(), "血糖", "异常", "血糖为" + sugar + "mmol/L，偏低"));
            }
        }

        if (alerts.size() >= 2) {
            alerts.add(createAlert(indicator.getRecordId(), "综合风险", "高风险", "本次录入有" + alerts.size() + "项指标异常，请注意健康管理"));
        }

        for (HealthAlert alert : alerts) {
            healthAlertService.saveAlert(alert);
        }
    }

    private HealthAlert createAlert(Long recordId, String type, String level, String content) {
        HealthAlert alert = new HealthAlert();
        alert.setRecordId(recordId);
        alert.setIndicatorType(type);
        alert.setAlertLevel(level);
        alert.setContent(content);
        return alert;
    }
}
