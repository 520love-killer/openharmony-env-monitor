import { defineStore } from 'pinia'
import { ref } from 'vue'
import { systemApi } from '@/api/system'
import type { DatabaseStatus, SerialStatus, CacheStatus, RetentionPolicy } from '@/types'

export const useSystemStore = defineStore('system', () => {
  const db = ref<DatabaseStatus | null>(null)
  const serial = ref<SerialStatus | null>(null)
  const cache = ref<CacheStatus | null>(null)
  const retention = ref<RetentionPolicy | null>(null)
  const loading = ref(false)

  async function loadAll() {
    loading.value = true
    try {
      const [dbRes, serialRes, cacheRes, retentionRes] = await Promise.all([
        systemApi.databaseStatus(),
        systemApi.serialStatus(),
        systemApi.cacheStatus(),
        systemApi.retentionPolicy(),
      ])
      db.value = dbRes.data
      serial.value = serialRes.data
      cache.value = cacheRes.data
      retention.value = retentionRes.data
    } finally {
      loading.value = false
    }
  }

  return { db, serial, cache, retention, loading, loadAll }
})
