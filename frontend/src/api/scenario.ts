import http from './http'
import type { ScenarioAnalysis } from '@/types'

export interface ScenarioParams {
  scenario?: string
  userRole?: string
  source?: string
  limit?: number
  crop?: string
  roomType?: string
  customTempMin?: number
  customTempMax?: number
  customHumidityMin?: number
  customHumidityMax?: number
  customGasMax?: number
}

export const scenarioApi = {
  analysis(params: ScenarioParams) {
    return http.get<ScenarioAnalysis>('/api/scenario/analysis', { params })
  },
}
