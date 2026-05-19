import http from './http'
import type { DatabaseStatus, SerialStatus, CacheStatus, RetentionPolicy } from '@/types'

export const systemApi = {
  databaseStatus() {
    return http.get<DatabaseStatus>('/api/system/database-status')
  },
  serialStatus() {
    return http.get<SerialStatus>('/api/system/serial-status')
  },
  cacheStatus() {
    return http.get<CacheStatus>('/api/system/cache-status')
  },
  retentionPolicy() {
    return http.get<RetentionPolicy>('/api/system/retention-policy')
  },
}
