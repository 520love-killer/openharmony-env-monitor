export interface SensorData {
  id?: number
  deviceId: string
  temperature: number
  humidity: number
  gas: number
  status: 'SAFE' | 'WARNING'
  dataSource: string
  createdAt: string
}

export interface LatestResponse {
  success?: boolean
  hasData: boolean
  data?: SensorData
  message?: string
}

export interface AnalyticsSummary {
  success: boolean
  sampleCount: number
  temperatureAvg: number
  temperatureMax: number
  temperatureMin: number
  temperatureStd: number
  humidityAvg: number
  gasMax: number
  volatilityLevel: string
  temperatureChangeRate: number
  humidityChangeRate: number
  gasChangeRate: number
  trend: string
  message?: string
}

export interface TrendResponse {
  success: boolean
  temperatureTrend: string
  humidityTrend: string
  gasTrend: string
  explanation: string
}

export interface ForecastResponse {
  success: boolean
  currentTemperature: number
  finalForecast5min: number
  finalForecast10min: number
  trend: string
  confidence: string
  sampleCount: number
  movingAverageForecast5min: number
  movingAverageForecast10min: number
  linearRegressionForecast5min: number
  linearRegressionForecast10min: number
  exponentialSmoothingForecast5min: number
  exponentialSmoothingForecast10min: number
  message?: string
}

export interface AnomalyItem {
  type: string
  level: string
  reason: string
  time: string
  suggestion: string
}

export interface AnomalyResponse {
  success: boolean
  hasAnomaly: boolean
  anomalyCount: number
  message: string
  items: AnomalyItem[]
}

export interface DatabaseStatus {
  connected: boolean
  database: string
  sensorDataCount: number
  realSerialCount: number
  realMqttCount: number
  mockCount: number
}

export interface SerialStatus {
  connected: boolean
  portName: string
  baudRate: number
  receivedLines: number
  savedRecords: number
}

export interface CacheStatus {
  provider: string
  enabled: boolean
  ttlSeconds: number
  maxSize: number
  cacheNames: string[]
}

export interface RetentionPolicy {
  rawDataRetentionDays: number
  mockDataRetentionDays: number
  summaryRetentionDays: number
  anomalyRetentionDays: number
  cleanupCron: string
}

export interface AgentStatus {
  mode: 'deepseek' | 'mock'
  model: string
}

export interface ChatMessage {
  id?: string
  role: 'user' | 'assistant'
  content: string
  createdAt: string
  usedTools?: string[]
  dataSource?: string
  confidence?: string
}

export interface AgentSession {
  id: string
  title: string
  role: string
  messages: ChatMessage[]
  createdAt: string
  updatedAt: string
}

export interface ScenarioRiskItem {
  type: string
  level: string
  message: string
}

export interface ScenarioAdviceItem {
  type: string
  title: string
  content: string
}

export interface ScenarioMetricStatus {
  temperatureStatus: string
  humidityStatus: string
  gasStatus: string
  trendStatus: string
}

export interface ScenarioScoreBreakdown {
  temperatureScore: number
  humidityScore: number
  gasScore: number
  trendScore: number
  anomalyScore: number
}

export interface ScenarioAnalysis {
  scenario: string
  scenarioName: string
  userRole: string
  userRoleName: string
  score: number
  level: string
  levelName: string
  confidence: string
  summary: string
  risks: ScenarioRiskItem[]
  advices: ScenarioAdviceItem[]
  metrics: ScenarioMetricStatus
  scoreBreakdown: ScenarioScoreBreakdown
  algorithmNotes: string[]
  dataSource: string
  sampleCount: number
  updatedAt: string
}
