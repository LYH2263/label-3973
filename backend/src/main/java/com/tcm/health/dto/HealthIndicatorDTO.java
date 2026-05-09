package com.tcm.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HealthIndicatorDTO {
    @NotNull(message = "档案ID不能为空")
    private Long recordId;

    private BigDecimal height;

    private BigDecimal weight;

    private Integer bloodPressureHigh;

    private Integer bloodPressureLow;

    private BigDecimal bloodSugar;

    private Integer heartRate;

    private String remark;
}
