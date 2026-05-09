package com.tcm.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    /** 性别 0女 1男 */
    private Integer gender;

    private Integer age;

    /** 血型 A/B/O/AB */
    private String bloodType;

    private String allergy;

    private String medicalHistory;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
