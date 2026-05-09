/**
 * 健康指标异常阈值常量配置
 * 所有异常判断的阈值统一在此定义，便于维护和调整
 */
export const ALERT_THRESHOLDS = {
  bmi: {
    underweight: 18.5,
    overweightLow: 24,
    overweightHigh: 28
  },
  heartRate: {
    low: 60,
    high: 100
  },
  bloodSugar: {
    preHigh: 6.1,
    high: 7.0
  },
  bloodPressure: {
    highSystolic: 140,
    highDiastolic: 90,
    preHighSystolic: 130,
    preHighDiastolic: 80
  },
  comprehensiveRisk: {
    abnormalCount: 2
  }
}

/**
 * 检测BMI异常状态
 * @param {number|null} height - 身高(cm)
 * @param {number|null} weight - 体重(kg)
 * @returns {{ type: string, label: string } | null} 异常标签信息，type为el-tag类型，label为提示文本；无异常返回null
 */
export function checkBmiAbnormal(height, weight) {
  if (!height || !weight || height <= 0) return null
  const bmi = weight / ((height / 100) ** 2)
  if (bmi < ALERT_THRESHOLDS.bmi.underweight) {
    return { type: 'info', label: '偏瘦' }
  }
  if (bmi > ALERT_THRESHOLDS.bmi.overweightHigh) {
    return { type: 'danger', label: '肥胖' }
  }
  if (bmi >= ALERT_THRESHOLDS.bmi.overweightLow) {
    return { type: 'warning', label: '超重' }
  }
  return null
}

/**
 * 检测血压异常状态
 * @param {number|null} systolic - 收缩压(mmHg)
 * @param {number|null} diastolic - 舒张压(mmHg)
 * @returns {{ type: string, label: string } | null} 异常标签信息；无异常返回null
 */
export function checkBloodPressureAbnormal(systolic, diastolic) {
  if (!systolic || !diastolic) return null
  if (systolic >= ALERT_THRESHOLDS.bloodPressure.highSystolic || diastolic >= ALERT_THRESHOLDS.bloodPressure.highDiastolic) {
    return { type: 'danger', label: '高血压' }
  }
  if (systolic >= ALERT_THRESHOLDS.bloodPressure.preHighSystolic || diastolic >= ALERT_THRESHOLDS.bloodPressure.preHighDiastolic) {
    return { type: 'warning', label: '血压偏高' }
  }
  return null
}

/**
 * 检测血糖异常状态
 * @param {number|null} bloodSugar - 空腹血糖(mmol/L)
 * @returns {{ type: string, label: string } | null} 异常标签信息；无异常返回null
 */
export function checkBloodSugarAbnormal(bloodSugar) {
  if (!bloodSugar) return null
  if (bloodSugar >= ALERT_THRESHOLDS.bloodSugar.high) {
    return { type: 'danger', label: '血糖偏高' }
  }
  if (bloodSugar >= ALERT_THRESHOLDS.bloodSugar.preHigh) {
    return { type: 'warning', label: '血糖偏高' }
  }
  return null
}

/**
 * 检测心率异常状态
 * @param {number|null} heartRate - 心率(次/分)
 * @returns {{ type: string, label: string } | null} 异常标签信息；无异常返回null
 */
export function checkHeartRateAbnormal(heartRate) {
  if (!heartRate) return null
  if (heartRate < ALERT_THRESHOLDS.heartRate.low) {
    return { type: 'warning', label: '心动过缓' }
  }
  if (heartRate > ALERT_THRESHOLDS.heartRate.high) {
    return { type: 'warning', label: '心动过速' }
  }
  return null
}

/**
 * 检测综合风险：当同一次录入有2个及以上指标异常时标记为高风险
 * @param {object} indicator - 健康指标数据对象
 * @param {number|null} indicator.height - 身高(cm)
 * @param {number|null} indicator.weight - 体重(kg)
 * @param {number|null} indicator.bloodPressureHigh - 收缩压(mmHg)
 * @param {number|null} indicator.bloodPressureLow - 舒张压(mmHg)
 * @param {number|null} indicator.bloodSugar - 空腹血糖(mmol/L)
 * @param {number|null} indicator.heartRate - 心率(次/分)
 * @returns {{ type: string, label: string } | null} 高风险标签信息；未达到阈值返回null
 */
export function detectComprehensiveRisk(indicator) {
  let abnormalCount = 0
  if (checkBmiAbnormal(indicator.height, indicator.weight)) abnormalCount++
  if (checkBloodPressureAbnormal(indicator.bloodPressureHigh, indicator.bloodPressureLow)) abnormalCount++
  if (checkBloodSugarAbnormal(indicator.bloodSugar)) abnormalCount++
  if (checkHeartRateAbnormal(indicator.heartRate)) abnormalCount++
  if (abnormalCount >= ALERT_THRESHOLDS.comprehensiveRisk.abnormalCount) {
    return { type: 'danger', label: `高风险(${abnormalCount}项异常)` }
  }
  return null
}

/**
 * 检测单条健康指标记录的所有异常标签
 * 汇总BMI、血压、血糖、心率及综合风险的异常检测结果
 * @param {object} indicator - 健康指标数据对象
 * @returns {Array<{type: string, label: string}>} 异常标签数组，无异常时为空数组
 */
export function detectIndicatorAbnormalTags(indicator) {
  const tags = []
  const bmiResult = checkBmiAbnormal(indicator.height, indicator.weight)
  if (bmiResult) tags.push(bmiResult)
  const bpResult = checkBloodPressureAbnormal(indicator.bloodPressureHigh, indicator.bloodPressureLow)
  if (bpResult) tags.push(bpResult)
  const sugarResult = checkBloodSugarAbnormal(indicator.bloodSugar)
  if (sugarResult) tags.push(sugarResult)
  const hrResult = checkHeartRateAbnormal(indicator.heartRate)
  if (hrResult) tags.push(hrResult)
  const riskResult = detectComprehensiveRisk(indicator)
  if (riskResult) tags.push(riskResult)
  return tags
}

/**
 * 健康指标异常检测 composable
 * 封装所有异常检测逻辑，供组件使用
 * @returns {{ checkBmiAbnormal: Function, checkBloodPressureAbnormal: Function, checkBloodSugarAbnormal: Function, checkHeartRateAbnormal: Function, detectComprehensiveRisk: Function, detectIndicatorAbnormalTags: Function, ALERT_THRESHOLDS: object }}
 */
export function useHealthAlertDetect() {
  return {
    ALERT_THRESHOLDS,
    checkBmiAbnormal,
    checkBloodPressureAbnormal,
    checkBloodSugarAbnormal,
    checkHeartRateAbnormal,
    detectComprehensiveRisk,
    detectIndicatorAbnormalTags
  }
}
