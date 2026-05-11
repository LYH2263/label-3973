package com.tcm.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health_alert")
public class HealthAlert {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private String indicatorType;

    private String alertLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime alertTime;

    private String alertContent;
}
