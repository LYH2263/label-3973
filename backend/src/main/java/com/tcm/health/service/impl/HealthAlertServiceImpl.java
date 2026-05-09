package com.tcm.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.health.entity.HealthAlert;
import com.tcm.health.entity.HealthIndicator;
import com.tcm.health.mapper.HealthAlertMapper;
import com.tcm.health.service.HealthAlertService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthAlertServiceImpl extends ServiceImpl<HealthAlertMapper, HealthAlert> implements HealthAlertService {

    private static final BigDecimal BMI_UNDERWEIGHT = new BigDecimal("18.5");
    private static final BigDecimal BMI_OVERWEIGHT_LOW = new BigDecimal("24");
    private static final BigDecimal BMI_OVERWEIGHT_HIGH = new BigDecimal("28");
    private static final int HEART_RATE_LOW = 60;
    private static final int HEART_RATE_HIGH = 100;
    private static final BigDecimal BLOOD_SUGAR_HIGH = new BigDecimal("7.0");
    private static final BigDecimal BLOOD_SUGAR_LOW = new BigDecimal("6.1");
    private static final int BP_HIGH_SYSTOLIC = 140;
    private static final int BP_HIGH_DIASTOLIC = 90;
    private static final int BP_PREHIGH_SYSTOLIC = 130;
    private static final int BP_PREHIGH_DIASTOLIC = 80;
    private static final int COMPREHENSIVE_RISK_THRESHOLD = 2;

    @Override
    public List<HealthAlert> listByRecordId(Long recordId) {
        return list(new LambdaQueryWrapper<HealthAlert>()
                .eq(HealthAlert::getRecordId, recordId)
                .orderByDesc(HealthAlert::getAlertTime));
    }

    @Override
    public void detectAndSaveAlerts(HealthIndicator indicator) {
        List<HealthAlert> alerts = new ArrayList<>();
        int abnormalCount = 0;

        if (indicator.getHeight() != null && indicator.getWeight() != null
                && indicator.getHeight().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = indicator.getHeight().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            BigDecimal bmi = indicator.getWeight().divide(heightM.multiply(heightM), 1, BigDecimal.ROUND_HALF_UP);
            if (bmi.compareTo(BMI_UNDERWEIGHT) < 0) {
                alerts.add(buildAlert(indicator.getRecordId(), "BMI", "info",
                        "BMI为" + bmi + "，偏瘦（<18.5）"));
                abnormalCount++;
            } else if (bmi.compareTo(BMI_OVERWEIGHT_HIGH) > 0) {
                alerts.add(buildAlert(indicator.getRecordId(), "BMI", "danger",
                        "BMI为" + bmi + "，肥胖（>28）"));
                abnormalCount++;
            } else if (bmi.compareTo(BMI_OVERWEIGHT_LOW) >= 0) {
                alerts.add(buildAlert(indicator.getRecordId(), "BMI", "warning",
                        "BMI为" + bmi + "，超重（24-28）"));
                abnormalCount++;
            }
        }

        if (indicator.getBloodPressureHigh() != null && indicator.getBloodPressureLow() != null) {
            if (indicator.getBloodPressureHigh() >= BP_HIGH_SYSTOLIC
                    || indicator.getBloodPressureLow() >= BP_HIGH_DIASTOLIC) {
                alerts.add(buildAlert(indicator.getRecordId(), "血压", "danger",
                        "血压" + indicator.getBloodPressureHigh() + "/" + indicator.getBloodPressureLow()
                                + "mmHg，高血压（收缩压≥140或舒张压≥90）"));
                abnormalCount++;
            } else if (indicator.getBloodPressureHigh() >= BP_PREHIGH_SYSTOLIC
                    || indicator.getBloodPressureLow() >= BP_PREHIGH_DIASTOLIC) {
                alerts.add(buildAlert(indicator.getRecordId(), "血压", "warning",
                        "血压" + indicator.getBloodPressureHigh() + "/" + indicator.getBloodPressureLow()
                                + "mmHg，血压偏高（收缩压≥130或舒张压≥80）"));
                abnormalCount++;
            }
        }

        if (indicator.getBloodSugar() != null) {
            if (indicator.getBloodSugar().compareTo(BLOOD_SUGAR_HIGH) >= 0) {
                alerts.add(buildAlert(indicator.getRecordId(), "血糖", "danger",
                        "空腹血糖" + indicator.getBloodSugar() + "mmol/L，偏高（≥7.0）"));
                abnormalCount++;
            } else if (indicator.getBloodSugar().compareTo(BLOOD_SUGAR_LOW) >= 0) {
                alerts.add(buildAlert(indicator.getRecordId(), "血糖", "warning",
                        "空腹血糖" + indicator.getBloodSugar() + "mmol/L，偏高（6.1-7.0）"));
                abnormalCount++;
            }
        }

        if (indicator.getHeartRate() != null) {
            if (indicator.getHeartRate() < HEART_RATE_LOW) {
                alerts.add(buildAlert(indicator.getRecordId(), "心率", "warning",
                        "心率" + indicator.getHeartRate() + "次/分，心动过缓（<60）"));
                abnormalCount++;
            } else if (indicator.getHeartRate() > HEART_RATE_HIGH) {
                alerts.add(buildAlert(indicator.getRecordId(), "心率", "warning",
                        "心率" + indicator.getHeartRate() + "次/分，心动过速（>100）"));
                abnormalCount++;
            }
        }

        if (abnormalCount >= COMPREHENSIVE_RISK_THRESHOLD) {
            alerts.add(buildAlert(indicator.getRecordId(), "综合风险", "danger",
                    "本次录入有" + abnormalCount + "项指标异常，存在高风险"));
        }

        if (!alerts.isEmpty()) {
            saveBatch(alerts);
        }
    }

    private HealthAlert buildAlert(Long recordId, String indicatorType, String alertLevel, String alertContent) {
        HealthAlert alert = new HealthAlert();
        alert.setRecordId(recordId);
        alert.setIndicatorType(indicatorType);
        alert.setAlertLevel(alertLevel);
        alert.setAlertTime(LocalDateTime.now());
        alert.setAlertContent(alertContent);
        return alert;
    }
}
