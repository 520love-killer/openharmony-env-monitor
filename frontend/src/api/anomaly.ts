import http from './http'
import type { AnomalyResponse } from '@/types'

export const anomalyApi = {
  detect(source: string, limit = 50) {
    return http.get<AnomalyResponse>(`/api/anomaly/detect?limit=${limit}&source=${encodeURIComponent(source)}`)
  },
}
