package com.tcm.health.service;

import com.tcm.health.dto.ConstitutionSubmitDTO;
import com.tcm.health.entity.ConstitutionQuestion;
import com.tcm.health.entity.ConstitutionResult;

import java.util.List;

public interface ConstitutionService {
    List<ConstitutionQuestion> getQuestions();
    ConstitutionResult submitAnswers(Long userId, ConstitutionSubmitDTO dto);
    List<ConstitutionResult> getResultsByUserId(Long userId);
}
