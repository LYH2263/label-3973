package com.tcm.health.controller;

import com.tcm.health.common.R;
import com.tcm.health.dto.HealthIndicatorDTO;
import com.tcm.health.entity.HealthIndicator;
import com.tcm.health.service.HealthIndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "健康指标", description = "健康指标录入与查看")
@SecurityRequirement(name = "Bearer Token")
@RestController
@RequestMapping("/api/indicators")
public class HealthIndicatorController {

    @Autowired
    private HealthIndicatorService healthIndicatorService;

    @Operation(summary = "查询档案的健康指标列表")
    @GetMapping
    public R<List<HealthIndicator>> list(@RequestParam Long recordId) {
        return R.ok(healthIndicatorService.listByRecordId(recordId));
    }

    @Operation(summary = "录入健康指标")
    @PostMapping
    public R<HealthIndicator> create(@Valid @RequestBody HealthIndicatorDTO dto) {
        try {
            return R.ok(healthIndicatorService.create(dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @Operation(summary = "删除健康指标记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        healthIndicatorService.delete(id);
        return R.ok();
    }
}
