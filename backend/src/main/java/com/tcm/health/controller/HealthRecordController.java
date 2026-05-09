package com.tcm.health.controller;

import com.tcm.health.common.R;
import com.tcm.health.common.UserContext;
import com.tcm.health.dto.HealthRecordDTO;
import com.tcm.health.entity.HealthRecord;
import com.tcm.health.service.HealthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "健康档案", description = "健康档案增删改查")
@SecurityRequirement(name = "Bearer Token")
@RestController
@RequestMapping("/api/records")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    @Operation(summary = "获取我的健康档案列表")
    @GetMapping
    public R<List<HealthRecord>> list() {
        return R.ok(healthRecordService.listByUserId(UserContext.getUserId()));
    }

    @Operation(summary = "获取单个档案详情")
    @GetMapping("/{id}")
    public R<HealthRecord> get(@PathVariable Long id) {
        HealthRecord record = healthRecordService.getById(id);
        if (record == null || !record.getUserId().equals(UserContext.getUserId())) {
            return R.fail("档案不存在");
        }
        return R.ok(record);
    }

    @Operation(summary = "创建健康档案")
    @PostMapping
    public R<HealthRecord> create(@Valid @RequestBody HealthRecordDTO dto) {
        try {
            return R.ok(healthRecordService.createRecord(UserContext.getUserId(), dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @Operation(summary = "更新健康档案")
    @PutMapping("/{id}")
    public R<HealthRecord> update(@PathVariable Long id, @Valid @RequestBody HealthRecordDTO dto) {
        try {
            return R.ok(healthRecordService.updateRecord(id, UserContext.getUserId(), dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @Operation(summary = "删除健康档案")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        try {
            healthRecordService.deleteRecord(id, UserContext.getUserId());
            return R.ok();
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }
}
