package com.tcm.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.health.dto.ChronicDiseaseDTO;
import com.tcm.health.entity.ChronicDisease;
import com.tcm.health.mapper.ChronicDiseaseMapper;
import com.tcm.health.service.ChronicDiseaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChronicDiseaseServiceImpl extends ServiceImpl<ChronicDiseaseMapper, ChronicDisease> implements ChronicDiseaseService {

    @Override
    public List<ChronicDisease> listByRecordId(Long recordId) {
        return list(new LambdaQueryWrapper<ChronicDisease>()
                .eq(ChronicDisease::getRecordId, recordId)
                .orderByDesc(ChronicDisease::getCreatedAt));
    }

    @Override
    public ChronicDisease create(ChronicDiseaseDTO dto) {
        ChronicDisease disease = new ChronicDisease();
        copyDto(dto, disease);
        save(disease);
        return disease;
    }

    @Override
    public ChronicDisease update(Long id, ChronicDiseaseDTO dto) {
        ChronicDisease disease = getById(id);
        if (disease == null) {
            throw new RuntimeException("记录不存在");
        }
        copyDto(dto, disease);
        updateById(disease);
        return disease;
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    private void copyDto(ChronicDiseaseDTO dto, ChronicDisease disease) {
        disease.setRecordId(dto.getRecordId());
        disease.setDiseaseType(dto.getDiseaseType());
        disease.setDiseaseName(dto.getDiseaseName());
        disease.setDiagnosisDate(dto.getDiagnosisDate());
        disease.setNotes(dto.getNotes());
    }
}
