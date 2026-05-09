package com.tcm.health.controller;

import com.tcm.health.common.R;
import com.tcm.health.common.UserContext;
import com.tcm.health.dto.ConstitutionSubmitDTO;
import com.tcm.health.entity.ConstitutionQuestion;
import com.tcm.health.entity.ConstitutionResult;
import com.tcm.health.service.ConstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "体质辨识", description = "中医体质辨识问卷与结果")
@SecurityRequirement(name = "Bearer Token")
@RestController
@RequestMapping("/api/constitution")
public class ConstitutionController {

    @Autowired
    private ConstitutionService constitutionService;

    @Operation(summary = "获取体质辨识问卷题目")
    @GetMapping("/questions")
    public R<List<ConstitutionQuestion>> getQuestions() {
        return R.ok(constitutionService.getQuestions());
    }

    @Operation(summary = "提交问卷答案，获取体质辨识结果")
    @PostMapping("/submit")
    public R<ConstitutionResult> submit(@Valid @RequestBody ConstitutionSubmitDTO dto) {
        try {
            return R.ok(constitutionService.submitAnswers(UserContext.getUserId(), dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @Operation(summary = "查看我的体质辨识历史记录")
    @GetMapping("/results")
    public R<List<ConstitutionResult>> getResults() {
        return R.ok(constitutionService.getResultsByUserId(UserContext.getUserId()));
    }
}
