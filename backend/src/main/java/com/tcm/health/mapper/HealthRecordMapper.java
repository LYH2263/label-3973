package com.tcm.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.health.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
}
