package com.tcm.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_indicator")
public class HealthIndicator {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private BigDecimal height;

    private BigDecimal weight;

    /** 收缩压 */
    private Integer bloodPressureHigh;

    /** 舒张压 */
    private Integer bloodPressureLow;

    /** 空腹血糖 mmol/L */
    private BigDecimal bloodSugar;

    /** 心率 次/分 */
    private Integer heartRate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
