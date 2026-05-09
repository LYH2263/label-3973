package com.tcm.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcm.health.dto.ConstitutionSubmitDTO;
import com.tcm.health.entity.ConstitutionQuestion;
import com.tcm.health.entity.ConstitutionResult;
import com.tcm.health.mapper.ConstitutionQuestionMapper;
import com.tcm.health.mapper.ConstitutionResultMapper;
import com.tcm.health.service.ConstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConstitutionServiceImpl implements ConstitutionService {

    @Autowired
    private ConstitutionQuestionMapper questionMapper;

    @Autowired
    private ConstitutionResultMapper resultMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Map<String, String[]> CONSTITUTION_INFO = new LinkedHashMap<>();

    static {
        CONSTITUTION_INFO.put("PINGHE", new String[]{
                "平和质",
                "体质平和，精力充沛，面色红润，睡眠良好，适应能力强。",
                "日常保养：均衡饮食，规律作息，适量运动，保持良好心态。"
        });
        CONSTITUTION_INFO.put("QIXU", new String[]{
                "气虚质",
                "元气不足，容易疲乏气短，声音低弱，易感冒，消化功能偏弱。",
                "养生建议：多食益气食物（山药、大枣、黄芪炖鸡），避免过度劳累，可练太极拳。"
        });
        CONSTITUTION_INFO.put("YANGXU", new String[]{
                "阳虚质",
                "阳气不足，畏寒怕冷，手脚冰凉，大便溏薄，精神不振。",
                "养生建议：多食温阳食物（羊肉、韭菜、生姜），避免生冷，适当晒太阳，艾灸关元穴。"
        });
        CONSTITUTION_INFO.put("YINXU", new String[]{
                "阴虚质",
                "阴液不足，手足心热，口燥咽干，易失眠，大便干燥。",
                "养生建议：多食滋阴食物（百合、银耳、枸杞、鸭肉），少辛辣，充足睡眠，避免熬夜。"
        });
        CONSTITUTION_INFO.put("TANSHI", new String[]{
                "痰湿质",
                "痰湿凝聚，形体偏胖，腹部松软，胸闷，口黏腻，大便黏滞。",
                "养生建议：控制饮食，少甜腻，多食健脾祛湿食物（薏仁、冬瓜、荷叶茶），加强运动。"
        });
        CONSTITUTION_INFO.put("SHIRE", new String[]{
                "湿热质",
                "湿热内蕴，面部油腻，易生痤疮，口苦，大便黏滞或燥结。",
                "养生建议：少辛辣油腻，多食清热祛湿食物（绿豆、苦瓜、薏仁），保持皮肤清洁。"
        });
        CONSTITUTION_INFO.put("XUEYU", new String[]{
                "血瘀质",
                "血行不畅，面色晦暗，易有瘀斑，肤色暗沉，易健忘。",
                "养生建议：多食活血化瘀食物（山楂、黑木耳、红酒少量），适量运动，保持情绪舒畅。"
        });
        CONSTITUTION_INFO.put("QIYU", new String[]{
                "气郁质",
                "气机郁滞，情绪低沉，胸胁胀闷，易焦虑，咽喉有异物感。",
                "养生建议：保持乐观情绪，多与朋友交流，适当练习瑜伽、散步，可饮玫瑰花茶、佛手茶。"
        });
        CONSTITUTION_INFO.put("TEBING", new String[]{
                "特禀质",
                "先天禀赋异常，易过敏，对花粉、食物、药物等易产生过敏反应。",
                "养生建议：避免接触过敏原，增强体质，饮食清淡，可服用玉屏风散等益气固表方剂。"
        });
    }

    @Override
    public List<ConstitutionQuestion> getQuestions() {
        return questionMapper.selectList(
                new LambdaQueryWrapper<ConstitutionQuestion>()
                        .orderByAsc(ConstitutionQuestion::getSortOrder));
    }

    @Override
    public ConstitutionResult submitAnswers(Long userId, ConstitutionSubmitDTO dto) {
        List<ConstitutionQuestion> questions = getQuestions();

        Map<String, List<Integer>> typeScores = new LinkedHashMap<>();
        for (ConstitutionQuestion q : questions) {
            String type = q.getConstitutionType();
            Integer score = dto.getAnswers().get(q.getId());
            if (score == null) score = 1;
            score = Math.max(1, Math.min(5, score));
            typeScores.computeIfAbsent(type, k -> new ArrayList<>()).add(score);
        }

        Map<String, Double> normalizedScores = new LinkedHashMap<>();
        for (Map.Entry<String, List<Integer>> entry : typeScores.entrySet()) {
            List<Integer> scores = entry.getValue();
            double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(1.0);
            double normalized = (avg - 1.0) / 4.0 * 100.0;
            normalizedScores.put(entry.getKey(), Math.round(normalized * 10.0) / 10.0);
        }

        String primaryType = determinePrimaryType(normalizedScores);
        String[] info = CONSTITUTION_INFO.getOrDefault(primaryType,
                new String[]{"未知体质", "体质信息暂缺", "请咨询专业中医师"});

        ConstitutionResult result = new ConstitutionResult();
        result.setUserId(userId);
        result.setResultType(primaryType);
        result.setResultName(info[0]);
        result.setDescription(info[1]);
        result.setAdvice(info[2]);

        try {
            Map<String, Object> scoreDetail = new LinkedHashMap<>();
            for (Map.Entry<String, Double> e : normalizedScores.entrySet()) {
                String[] typeInfo = CONSTITUTION_INFO.get(e.getKey());
                String name = typeInfo != null ? typeInfo[0] : e.getKey();
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", name);
                item.put("score", e.getValue());
                scoreDetail.put(e.getKey(), item);
            }
            result.setScoreDetail(objectMapper.writeValueAsString(scoreDetail));
        } catch (JsonProcessingException e) {
            result.setScoreDetail("{}");
        }

        resultMapper.insert(result);
        return result;
    }

    @Override
    public List<ConstitutionResult> getResultsByUserId(Long userId) {
        return resultMapper.selectList(
                new LambdaQueryWrapper<ConstitutionResult>()
                        .eq(ConstitutionResult::getUserId, userId)
                        .orderByDesc(ConstitutionResult::getCreatedAt));
    }

    private String determinePrimaryType(Map<String, Double> scores) {
        double pingheScore = scores.getOrDefault("PINGHE", 0.0);
        boolean otherTypesLow = scores.entrySet().stream()
                .filter(e -> !e.getKey().equals("PINGHE"))
                .allMatch(e -> e.getValue() < 30.0);

        if (pingheScore >= 60.0 && otherTypesLow) {
            return "PINGHE";
        }

        return scores.entrySet().stream()
                .filter(e -> !e.getKey().equals("PINGHE"))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("PINGHE");
    }
}
