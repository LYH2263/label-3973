package com.tcm.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("constitution_result")
public class ConstitutionResult {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String resultType;

    private String resultName;

    private String scoreDetail;

    private String description;

    private String advice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
