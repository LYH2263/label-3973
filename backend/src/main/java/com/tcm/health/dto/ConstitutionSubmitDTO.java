package com.tcm.health.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class ConstitutionSubmitDTO {
    /**
     * key: 题目ID, value: 分数(1-5)
     * 1=从不, 2=很少, 3=有时, 4=经常, 5=总是
     */
    @NotEmpty(message = "答案不能为空")
    private Map<Long, Integer> answers;
}
