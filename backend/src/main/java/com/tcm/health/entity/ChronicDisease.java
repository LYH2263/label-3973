package com.tcm.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("chronic_disease")
public class ChronicDisease {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private String diseaseType;

    private String diseaseName;

    private LocalDate diagnosisDate;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
