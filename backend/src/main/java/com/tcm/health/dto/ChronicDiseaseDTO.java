package com.tcm.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ChronicDiseaseDTO {
    @NotNull(message = "档案ID不能为空")
    private Long recordId;

    @NotBlank(message = "慢病类型不能为空")
    private String diseaseType;

    private String diseaseName;

    private LocalDate diagnosisDate;

    private String notes;
}
