package com.tcm.health.controller;

import com.tcm.health.common.R;
import com.tcm.health.dto.ChronicDiseaseDTO;
import com.tcm.health.entity.ChronicDisease;
import com.tcm.health.service.ChronicDiseaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "慢病管理", description = "慢病类型标记与管理")
@SecurityRequirement(name = "Bearer Token")
@RestController
@RequestMapping("/api/chronic-diseases")
public class ChronicDiseaseController {

    @Autowired
    private ChronicDiseaseService chronicDiseaseService;

    @Operation(summary = "查询档案的慢病列表")
    @GetMapping
    public R<List<ChronicDisease>> list(@RequestParam Long recordId) {
        return R.ok(chronicDiseaseService.listByRecordId(recordId));
    }

    @Operation(summary = "添加慢病记录")
    @PostMapping
    public R<ChronicDisease> create(@Valid @RequestBody ChronicDiseaseDTO dto) {
        try {
            return R.ok(chronicDiseaseService.create(dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @Operation(summary = "更新慢病记录")
    @PutMapping("/{id}")
    public R<ChronicDisease> update(@PathVariable Long id, @Valid @RequestBody ChronicDiseaseDTO dto) {
        try {
            return R.ok(chronicDiseaseService.update(id, dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @Operation(summary = "删除慢病记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        chronicDiseaseService.delete(id);
        return R.ok();
    }
}
