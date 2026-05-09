/**
 * 健康指标异常检测阈值配置
 */
const HEALTH_THRESHOLDS = {
  BMI: {
    UNDERWEIGHT: 18.5,
    OVERWEIGHT: 24,
    OBESE: 28
  },
  HEART_RATE: {
    MIN: 60,
    MAX: 100
  },
  BLOOD_PRESSURE: {
    SYSTOLIC_HIGH: 140,
    SYSTOLIC_LOW: 90,
    DIASTOLIC_HIGH: 90,
    DIASTOLIC_LOW: 60
  },
  BLOOD_SUGAR: {
    HIGH: 7.0,
    LOW: 3.9
  }
}

/**
 * 异常标签类型映射
 */
const ALERT_TAG_TYPES = {
  偏瘦: 'info',
  超重: 'warning',
  肥胖: 'danger',
  异常: 'warning',
  高风险: 'danger'
}

/**
 * 健康指标异常检测 composable
 */
export function useHealthDetection() {
  /**
   * 检测 BMI 是否异常并返回标签类型
   * @param {number|null} height 身高（cm）
   * @param {number|null} weight 体重（kg）
   * @returns {{ bmi: string, type: string, level: string|null }}
   */
  function checkBmiStatus(height, weight) {
    if (!height || !weight) {
      return { bmi: '-', type: 'success', level: null }
    }
    const bmi = weight / Math.pow(height / 100, 2)
    const bmiStr = bmi.toFixed(1)
    if (bmi < HEALTH_THRESHOLDS.BMI.UNDERWEIGHT) {
      return { bmi: bmiStr, type: 'info', level: '偏瘦' }
    } else if (bmi >= HEALTH_THRESHOLDS.BMI.OBESE) {
      return { bmi: bmiStr, type: 'danger', level: '肥胖' }
    } else if (bmi >= HEALTH_THRESHOLDS.BMI.OVERWEIGHT) {
      return { bmi: bmiStr, type: 'warning', level: '超重' }
    }
    return { bmi: bmiStr, type: 'success', level: null }
  }

  /**
   * 检测心率是否异常
   * @param {number|null} heartRate 心率
   * @returns {{ type: string, level: string|null }}
   */
  function checkHeartRateStatus(heartRate) {
    if (!heartRate) {
      return { type: 'success', level: null }
    }
    if (heartRate < HEALTH_THRESHOLDS.HEART_RATE.MIN || heartRate > HEALTH_THRESHOLDS.HEART_RATE.MAX) {
      return { type: 'warning', level: '异常' }
    }
    return { type: 'success', level: null }
  }

  /**
   * 检测血压是否异常
   * @param {number|null} high 收缩压
   * @param {number|null} low 舒张压
   * @returns {{ type: string, level: string|null }}
   */
  function checkBloodPressureStatus(high, low) {
    if (!high && !low) {
      return { type: 'success', level: null }
    }
    const systolicAbnormal = high !== null && (high >= HEALTH_THRESHOLDS.BLOOD_PRESSURE.SYSTOLIC_HIGH || high < HEALTH_THRESHOLDS.BLOOD_PRESSURE.SYSTOLIC_LOW)
    const diastolicAbnormal = low !== null && (low >= HEALTH_THRESHOLDS.BLOOD_PRESSURE.DIASTOLIC_HIGH || low < HEALTH_THRESHOLDS.BLOOD_PRESSURE.DIASTOLIC_LOW)
    if (systolicAbnormal || diastolicAbnormal) {
      return { type: 'danger', level: '异常' }
    }
    return { type: 'success', level: null }
  }

  /**
   * 检测血糖是否异常
   * @param {number|null} sugar 血糖值
   * @returns {{ type: string, level: string|null }}
   */
  function checkBloodSugarStatus(sugar) {
    if (!sugar) {
      return { type: 'success', level: null }
    }
    if (sugar >= HEALTH_THRESHOLDS.BLOOD_SUGAR.HIGH || sugar < HEALTH_THRESHOLDS.BLOOD_SUGAR.LOW) {
      return { type: 'danger', level: '异常' }
    }
    return { type: 'success', level: null }
  }

  /**
   * 检测一条记录的所有指标异常情况
   * @param {Object} row 指标记录行
   * @returns {Array<{ type: string, label: string }>}
   */
  function detectAllAbnormalities(row) {
    const abnormalities = []
    const bmiResult = checkBmiStatus(row.height, row.weight)
    if (bmiResult.level) {
      abnormalities.push({ type: bmiResult.type, label: `BMI:${bmiResult.level}` })
    }
    const hrResult = checkHeartRateStatus(row.heartRate)
    if (hrResult.level) {
      abnormalities.push({ type: hrResult.type, label: `心率:${hrResult.level}` })
    }
    const bpResult = checkBloodPressureStatus(row.bloodPressureHigh, row.bloodPressureLow)
    if (bpResult.level) {
      abnormalities.push({ type: bpResult.type, label: `血压:${bpResult.level}` })
    }
    const sugarResult = checkBloodSugarStatus(row.bloodSugar)
    if (sugarResult.level) {
      abnormalities.push({ type: sugarResult.type, label: `血糖:${sugarResult.level}` })
    }
    return abnormalities
  }

  /**
   * 根据预警级别获取 Element Plus tag type
   * @param {string} level 预警级别
   * @returns {string}
   */
  function getAlertTagType(level) {
    return ALERT_TAG_TYPES[level] || 'info'
  }

  return {
    HEALTH_THRESHOLDS,
    ALERT_TAG_TYPES,
    checkBmiStatus,
    checkHeartRateStatus,
    checkBloodPressureStatus,
    checkBloodSugarStatus,
    detectAllAbnormalities,
    getAlertTagType
  }
}
