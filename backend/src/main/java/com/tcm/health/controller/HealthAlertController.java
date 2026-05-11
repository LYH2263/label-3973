package com.tcm.health.controller;

import com.tcm.health.common.R;
import com.tcm.health.entity.HealthAlert;
import com.tcm.health.service.HealthAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "健康预警", description = "健康指标异常预警查询")
@SecurityRequirement(name = "Bearer Token")
@RestController
@RequestMapping("/api/alerts")
public class HealthAlertController {

    @Autowired
    private HealthAlertService healthAlertService;

    @Operation(summary = "查询档案的预警历史")
    @GetMapping
    public R<List<HealthAlert>> list(@RequestParam Long recordId) {
        return R.ok(healthAlertService.listByRecordId(recordId));
    }
}
