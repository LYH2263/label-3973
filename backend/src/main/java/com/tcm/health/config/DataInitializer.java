package com.tcm.health.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcm.health.entity.*;
import com.tcm.health.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired private UserMapper userMapper;
    @Autowired private HealthRecordMapper healthRecordMapper;
    @Autowired private ChronicDiseaseMapper chronicDiseaseMapper;
    @Autowired private HealthIndicatorMapper healthIndicatorMapper;
    @Autowired private ConstitutionQuestionMapper questionMapper;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        initConstitutionQuestions();
        initDemoData();
    }

    // ==================== 体质问卷（全部通过 JDBC 写入，避免 SQL 文件乱码）====================
    private void initConstitutionQuestions() {
        if (questionMapper.selectCount(null) > 0) return;

        List<ConstitutionQuestion> list = new ArrayList<>();
        int i = 1;

        // 平和质
        list.add(q("PINGHE",  "平和质", "您精力充沛吗？", i++));
        list.add(q("PINGHE",  "平和质", "您能适应外界自然和社会环境的变化吗？", i++));
        list.add(q("PINGHE",  "平和质", "您的睡眠质量好吗？", i++));
        // 气虚质
        list.add(q("QIXU",   "气虚质", "您容易疲乏吗？", i++));
        list.add(q("QIXU",   "气虚质", "您容易气短（呼吸短促、接不上气）吗？", i++));
        list.add(q("QIXU",   "气虚质", "您说话声音低弱无力吗？", i++));
        // 阳虚质
        list.add(q("YANGXU", "阳虚质", "您手脚发凉吗？", i++));
        list.add(q("YANGXU", "阳虚质", "您胃腹部、腰背部或膝关节等部位怕冷吗？", i++));
        // 阴虚质
        list.add(q("YINXU",  "阴虚质", "您感到手脚心发热吗？", i++));
        list.add(q("YINXU",  "阴虚质", "您皮肤或口唇干燥吗？", i++));
        list.add(q("YINXU",  "阴虚质", "您容易失眠吗？", i++));
        // 痰湿质
        list.add(q("TANSHI", "痰湿质", "您感到胸闷或腹部胀满吗？", i++));
        list.add(q("TANSHI", "痰湿质", "您感到身体沉重、不轻松或不爽快吗？", i++));
        list.add(q("TANSHI", "痰湿质", "您腹部肥满松软吗？", i++));
        // 湿热质
        list.add(q("SHIRE",  "湿热质", "您脸部或鼻部有油腻感或油光发亮吗？", i++));
        list.add(q("SHIRE",  "湿热质", "您脸上容易生痤疮或皮肤容易生疮疖吗？", i++));
        // 血瘀质
        list.add(q("XUEYU",  "血瘀质", "您皮肤在不知不觉中会出现青紫瘀斑吗？", i++));
        list.add(q("XUEYU",  "血瘀质", "您两颧部有细微红丝吗？", i++));
        // 气郁质
        list.add(q("QIYU",   "气郁质", "您感到闷闷不乐、情绪低沉吗？", i++));
        list.add(q("QIYU",   "气郁质", "您容易精神紧张、焦虑不安吗？", i++));
        list.add(q("QIYU",   "气郁质", "您多愁善感、感情脆弱吗？", i++));
        // 特禀质
        list.add(q("TEBING", "特禀质", "您没有感冒时也会打喷嚏吗？", i++));
        list.add(q("TEBING", "特禀质", "您容易过敏（对药物、食物、气味、花粉或季节交替）吗？", i));

        for (ConstitutionQuestion q : list) {
            questionMapper.insert(q);
        }
    }

    private ConstitutionQuestion q(String type, String name, String text, int order) {
        ConstitutionQuestion q = new ConstitutionQuestion();
        q.setConstitutionType(type);
        q.setConstitutionName(name);
        q.setQuestionText(text);
        q.setSortOrder(order);
        return q;
    }

    // ==================== 演示用户及数据 ====================
    private void initDemoData() {
        createUserIfNotExists("admin", "admin123", "管理员",  "13800138000");
        Long demoId = createUserIfNotExists("demo", "demo123", "演示用户", "13900139000");
        if (demoId == null) return;

        if (healthRecordMapper.selectCount(
                new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getUserId, demoId)) > 0) return;

        HealthRecord r1 = new HealthRecord();
        r1.setUserId(demoId);
        r1.setName("张三");
        r1.setGender(1);
        r1.setAge(35);
        r1.setBloodType("A");
        r1.setAllergy("青霉素过敏");
        r1.setMedicalHistory("无");
        healthRecordMapper.insert(r1);

        HealthRecord r2 = new HealthRecord();
        r2.setUserId(demoId);
        r2.setName("李四");
        r2.setGender(0);
        r2.setAge(28);
        r2.setBloodType("O");
        r2.setAllergy("无");
        r2.setMedicalHistory("轻度贫血");
        healthRecordMapper.insert(r2);

        ChronicDisease cd1 = new ChronicDisease();
        cd1.setRecordId(r1.getId());
        cd1.setDiseaseType("高血压");
        cd1.setDiseaseName("原发性高血压");
        cd1.setDiagnosisDate(LocalDate.of(2020, 3, 15));
        cd1.setNotes("长期服用降压药");
        chronicDiseaseMapper.insert(cd1);

        ChronicDisease cd2 = new ChronicDisease();
        cd2.setRecordId(r1.getId());
        cd2.setDiseaseType("糖尿病");
        cd2.setDiseaseName("2型糖尿病");
        cd2.setDiagnosisDate(LocalDate.of(2021, 6, 20));
        cd2.setNotes("饮食控制中");
        chronicDiseaseMapper.insert(cd2);

        HealthIndicator hi1 = new HealthIndicator();
        hi1.setRecordId(r1.getId());
        hi1.setHeight(new BigDecimal("175.0"));
        hi1.setWeight(new BigDecimal("75.5"));
        hi1.setBloodPressureHigh(135);
        hi1.setBloodPressureLow(85);
        hi1.setBloodSugar(new BigDecimal("6.2"));
        hi1.setHeartRate(78);
        hi1.setRemark("晨起空腹");
        healthIndicatorMapper.insert(hi1);

        HealthIndicator hi2 = new HealthIndicator();
        hi2.setRecordId(r2.getId());
        hi2.setHeight(new BigDecimal("163.0"));
        hi2.setWeight(new BigDecimal("52.0"));
        hi2.setBloodPressureHigh(110);
        hi2.setBloodPressureLow(70);
        hi2.setBloodSugar(new BigDecimal("4.8"));
        hi2.setHeartRate(72);
        hi2.setRemark("正常");
        healthIndicatorMapper.insert(hi2);
    }

    private Long createUserIfNotExists(String username, String rawPassword,
                                       String nickname, String phone) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing != null) return null;
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setPhone(phone);
        userMapper.insert(user);
        return user.getId();
    }
}
