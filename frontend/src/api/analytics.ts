import http from './http'
import type { AnalyticsSummary, TrendResponse } from '@/types'

export const analyticsApi = {
  summary(source: string, limit = 50) {
    return http.get<AnalyticsSummary>(`/api/analytics/summary?limit=${limit}&source=${encodeURIComponent(source)}`)
  },
  trend(source: string, limit = 50) {
    return http.get<TrendResponse>(`/api/analytics/trend?limit=${limit}&source=${encodeURIComponent(source)}`)
  },
}
