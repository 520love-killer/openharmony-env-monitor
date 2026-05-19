import http from './http'
import type { LatestResponse, SensorData } from '@/types'

export const sensorApi = {
  latest(source: string) {
    return http.get<LatestResponse>(`/api/sensor-data/latest?source=${encodeURIComponent(source)}`)
  },
  recent(source: string, limit = 50) {
    return http.get<SensorData[]>(`/api/sensor-data/recent?limit=${limit}&source=${encodeURIComponent(source)}`)
  },
}
