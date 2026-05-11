package com.tcm.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.health.dto.HealthIndicatorDTO;
import com.tcm.health.entity.HealthAlert;
import com.tcm.health.entity.HealthIndicator;
import com.tcm.health.mapper.HealthAlertMapper;
import com.tcm.health.mapper.HealthIndicatorMapper;
import com.tcm.health.service.HealthIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthIndicatorServiceImpl extends ServiceImpl<HealthIndicatorMapper, HealthIndicator> implements HealthIndicatorService {

    @Autowired
    private HealthAlertMapper healthAlertMapper;

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

        detectAndCreateAlerts(indicator);
        return indicator;
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    private void detectAndCreateAlerts(HealthIndicator indicator) {
        List<HealthAlert> alerts = new ArrayList<>();
        int abnormalCount = 0;

        if (indicator.getHeight() != null && indicator.getWeight() != null) {
            BigDecimal height = indicator.getHeight();
            BigDecimal weight = indicator.getWeight();
            BigDecimal bmi = weight.divide(height.divide(new BigDecimal("100")).pow(2), 1, BigDecimal.ROUND_HALF_UP);
            String bmiAlert = checkBMI(bmi);
            if (bmiAlert != null) {
                abnormalCount++;
                HealthAlert alert = new HealthAlert();
                alert.setRecordId(indicator.getRecordId());
                alert.setIndicatorType("BMI");
                alert.setAlertLevel(bmiAlert.contains("肥胖") ? "danger" : bmiAlert.contains("超重") ? "warning" : "info");
                alert.setAlertContent("BMI: " + bmi + "，" + bmiAlert);
                alerts.add(alert);
            }
        }

        if (indicator.getHeartRate() != null) {
            String heartRateAlert = checkHeartRate(indicator.getHeartRate());
            if (heartRateAlert != null) {
                abnormalCount++;
                HealthAlert alert = new HealthAlert();
                alert.setRecordId(indicator.getRecordId());
                alert.setIndicatorType("心率");
                alert.setAlertLevel("warning");
                alert.setAlertContent("心率: " + indicator.getHeartRate() + "次/分，" + heartRateAlert);
                alerts.add(alert);
            }
        }

        if (indicator.getBloodPressureHigh() != null && indicator.getBloodPressureLow() != null) {
            String bpAlert = checkBloodPressure(indicator.getBloodPressureHigh(), indicator.getBloodPressureLow());
            if (bpAlert != null) {
                abnormalCount++;
                HealthAlert alert = new HealthAlert();
                alert.setRecordId(indicator.getRecordId());
                alert.setIndicatorType("血压");
                alert.setAlertLevel(bpAlert.contains("高血压") ? "danger" : "warning");
                alert.setAlertContent("血压: " + indicator.getBloodPressureHigh() + "/" + indicator.getBloodPressureLow() + "mmHg，" + bpAlert);
                alerts.add(alert);
            }
        }

        if (indicator.getBloodSugar() != null) {
            String sugarAlert = checkBloodSugar(indicator.getBloodSugar());
            if (sugarAlert != null) {
                abnormalCount++;
                HealthAlert alert = new HealthAlert();
                alert.setRecordId(indicator.getRecordId());
                alert.setIndicatorType("血糖");
                alert.setAlertLevel(sugarAlert.contains("糖尿病") ? "danger" : "warning");
                alert.setAlertContent("血糖: " + indicator.getBloodSugar() + "mmol/L，" + sugarAlert);
                alerts.add(alert);
            }
        }

        if (abnormalCount >= 2) {
            HealthAlert riskAlert = new HealthAlert();
            riskAlert.setRecordId(indicator.getRecordId());
            riskAlert.setIndicatorType("综合风险");
            riskAlert.setAlertLevel("danger");
            riskAlert.setAlertContent("高风险预警：本次录入有 " + abnormalCount + " 项指标异常，请及时关注健康状况");
            alerts.add(riskAlert);
        }

        for (HealthAlert alert : alerts) {
            healthAlertMapper.insert(alert);
        }
    }

    private String checkBMI(BigDecimal bmi) {
        if (bmi.compareTo(new BigDecimal("18.5")) < 0) {
            return "偏瘦";
        } else if (bmi.compareTo(new BigDecimal("28")) > 0) {
            return "肥胖";
        } else if (bmi.compareTo(new BigDecimal("24")) >= 0) {
            return "超重";
        }
        return null;
    }

    private String checkHeartRate(Integer heartRate) {
        if (heartRate < 60) {
            return "心动过缓";
        } else if (heartRate > 100) {
            return "心动过速";
        }
        return null;
    }

    private String checkBloodPressure(Integer high, Integer low) {
        if (high >= 140 || low >= 90) {
            return "高血压";
        } else if (high >= 130 || low >= 80) {
            return "血压偏高";
        }
        return null;
    }

    private String checkBloodSugar(BigDecimal sugar) {
        if (sugar.compareTo(new BigDecimal("7.0")) >= 0) {
            return "糖尿病风险";
        } else if (sugar.compareTo(new BigDecimal("6.1")) >= 0) {
            return "血糖偏高";
        }
        return null;
    }
}
