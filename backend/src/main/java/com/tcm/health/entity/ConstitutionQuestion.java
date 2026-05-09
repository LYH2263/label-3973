package com.tcm.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("constitution_question")
public class ConstitutionQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String constitutionType;

    private String constitutionName;

    private String questionText;

    private Integer sortOrder;
}
