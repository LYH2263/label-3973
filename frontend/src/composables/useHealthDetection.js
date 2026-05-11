import { ref, computed } from 'vue'

const HEALTH_THRESHOLDS = {
  BMI: {
    UNDERWEIGHT: 18.5,
    OVERWEIGHT: 24,
    OBESITY: 28
  },
  HEART_RATE: {
    MIN: 60,
    MAX: 100
  },
  BLOOD_PRESSURE: {
    SYS_NORMAL_HIGH: 130,
    SYS_HIGH: 140,
    DIA_NORMAL_HIGH: 80,
    DIA_HIGH: 90
  },
  BLOOD_SUGAR: {
    NORMAL_HIGH: 6.1,
    HIGH: 7.0
  }
}

/**
 * 计算BMI值
 * @param {number} height - 身高(cm)
 * @param {number} weight - 体重(kg)
 * @returns {number} BMI值
 */
export function calcBMIValue(height, weight) {
  if (!height || !weight) return null
  return (weight / ((height / 100) ** 2)).toFixed(1)
}

/**
 * 检测BMI异常状态
 * @param {number} bmi - BMI值
 * @returns {Object} 检测结果 { type: string, label: string, isAbnormal: boolean }
 */
export function detectBMIStatus(bmi) {
  if (!bmi) return { type: 'success', label: '正常', isAbnormal: false }
  if (bmi < HEALTH_THRESHOLDS.BMI.UNDERWEIGHT) {
    return { type: 'info', label: '偏瘦', isAbnormal: true }
  }
  if (bmi >= HEALTH_THRESHOLDS.BMI.OBESITY) {
    return { type: 'danger', label: '肥胖', isAbnormal: true }
  }
  if (bmi >= HEALTH_THRESHOLDS.BMI.OVERWEIGHT) {
    return { type: 'warning', label: '超重', isAbnormal: true }
  }
  return { type: 'success', label: '正常', isAbnormal: false }
}

/**
 * 检测心率异常状态
 * @param {number} rate - 心率值
 * @returns {Object} 检测结果 { type: string, label: string, isAbnormal: boolean }
 */
export function checkHeartRateStatus(rate) {
  if (!rate) return { type: 'success', label: '正常', isAbnormal: false }
  if (rate < HEALTH_THRESHOLDS.HEART_RATE.MIN) {
    return { type: 'warning', label: '心动过缓', isAbnormal: true }
  }
  if (rate > HEALTH_THRESHOLDS.HEART_RATE.MAX) {
    return { type: 'warning', label: '心动过速', isAbnormal: true }
  }
  return { type: 'success', label: '正常', isAbnormal: false }
}

/**
 * 检测血压异常状态
 * @param {number} high - 收缩压
 * @param {number} low - 舒张压
 * @returns {Object} 检测结果 { type: string, label: string, isAbnormal: boolean }
 */
export function checkBloodPressureStatus(high, low) {
  if (!high || !low) return { type: 'success', label: '正常', isAbnormal: false }
  if (high >= HEALTH_THRESHOLDS.BLOOD_PRESSURE.SYS_HIGH || low >= HEALTH_THRESHOLDS.BLOOD_PRESSURE.DIA_HIGH) {
    return { type: 'danger', label: '高血压', isAbnormal: true }
  }
  if (high >= HEALTH_THRESHOLDS.BLOOD_PRESSURE.SYS_NORMAL_HIGH || low >= HEALTH_THRESHOLDS.BLOOD_PRESSURE.DIA_NORMAL_HIGH) {
    return { type: 'warning', label: '偏高', isAbnormal: true }
  }
  return { type: 'success', label: '正常', isAbnormal: false }
}

/**
 * 检测血糖异常状态
 * @param {number} sugar - 血糖值
 * @returns {Object} 检测结果 { type: string, label: string, isAbnormal: boolean }
 */
export function checkBloodSugarStatus(sugar) {
  if (!sugar) return { type: 'success', label: '正常', isAbnormal: false }
  if (sugar >= HEALTH_THRESHOLDS.BLOOD_SUGAR.HIGH) {
    return { type: 'danger', label: '糖尿病风险', isAbnormal: true }
  }
  if (sugar >= HEALTH_THRESHOLDS.BLOOD_SUGAR.NORMAL_HIGH) {
    return { type: 'warning', label: '偏高', isAbnormal: true }
  }
  return { type: 'success', label: '正常', isAbnormal: false }
}

/**
 * 检测单条记录的所有异常指标
 * @param {Object} record - 健康指标记录
 * @returns {Array} 异常标签列表
 */
export function detectAbnormalIndicators(record) {
  const alerts = []
  const bmi = calcBMIValue(record.height, record.weight)
  const bmiStatus = detectBMIStatus(bmi)
  if (bmiStatus.isAbnormal) {
    alerts.push({ type: bmiStatus.type, label: `BMI: ${bmiStatus.label}` })
  }
  const heartRateStatus = checkHeartRateStatus(record.heartRate)
  if (heartRateStatus.isAbnormal) {
    alerts.push({ type: heartRateStatus.type, label: `心率: ${heartRateStatus.label}` })
  }
  const bpStatus = checkBloodPressureStatus(record.bloodPressureHigh, record.bloodPressureLow)
  if (bpStatus.isAbnormal) {
    alerts.push({ type: bpStatus.type, label: `血压: ${bpStatus.label}` })
  }
  const sugarStatus = checkBloodSugarStatus(record.bloodSugar)
  if (sugarStatus.isAbnormal) {
    alerts.push({ type: sugarStatus.type, label: `血糖: ${sugarStatus.label}` })
  }
  return alerts
}

/**
 * 健康指标检测组合式函数
 */
export function useHealthDetection() {
  return {
    calcBMIValue,
    detectBMIStatus,
    checkHeartRateStatus,
    checkBloodPressureStatus,
    checkBloodSugarStatus,
    detectAbnormalIndicators
  }
}
