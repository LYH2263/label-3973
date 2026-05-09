package com.tcm.health.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HealthRecordDTO {
    @NotBlank(message = "姓名不能为空")
    private String name;

    private Integer gender;

    private Integer age;

    private String bloodType;

    private String allergy;

    private String medicalHistory;
}
