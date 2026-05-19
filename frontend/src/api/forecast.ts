import http from './http'
import type { ForecastResponse } from '@/types'

export const forecastApi = {
  temperature(source: string, limit = 50) {
    return http.get<ForecastResponse>(`/api/forecast/temperature?limit=${limit}&source=${encodeURIComponent(source)}`)
  },
}
