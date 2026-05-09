package com.tcm.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.health.entity.HealthIndicator;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthIndicatorMapper extends BaseMapper<HealthIndicator> {
}
