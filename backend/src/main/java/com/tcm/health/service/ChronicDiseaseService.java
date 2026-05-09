package com.tcm.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.health.dto.ChronicDiseaseDTO;
import com.tcm.health.entity.ChronicDisease;

import java.util.List;

public interface ChronicDiseaseService extends IService<ChronicDisease> {
    List<ChronicDisease> listByRecordId(Long recordId);
    ChronicDisease create(ChronicDiseaseDTO dto);
    ChronicDisease update(Long id, ChronicDiseaseDTO dto);
    void delete(Long id);
}
