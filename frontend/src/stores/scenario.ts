import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ScenarioAnalysis } from '@/types'

const STORAGE_KEY = 'thermometer-scenario-settings'

function loadSettings(): ScenarioSettings {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch { /* ignore */ }
  return defaultSettings()
}

function defaultSettings(): ScenarioSettings {
  return {
    userRole: 'STUDENT',
    scenario: 'GENERAL_MONITOR',
    crop: 'vegetable',
    roomType: 'dormitory',
    customTempMin: 18,
    customTempMax: 35,
    customHumidityMin: 30,
    customHumidityMax: 75,
    customGasMax: 50,
  }
}

function saveSettings(s: ScenarioSettings) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(s))
  } catch { /* ignore */ }
}

export interface ScenarioSettings {
  userRole: string
  scenario: string
  crop: string
  roomType: string
  customTempMin: number
  customTempMax: number
  customHumidityMin: number
  customHumidityMax: number
  customGasMax: number
}

export const useScenarioStore = defineStore('scenario', () => {
  const settings = ref<ScenarioSettings>(loadSettings())
  const lastAnalysis = ref<ScenarioAnalysis | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  function updateSettings(partial: Partial<ScenarioSettings>) {
    settings.value = { ...settings.value, ...partial }
    saveSettings(settings.value)
  }

  function setAnalysis(data: ScenarioAnalysis | null) {
    lastAnalysis.value = data
  }

  return {
    settings,
    lastAnalysis,
    loading,
    error,
    updateSettings,
    setAnalysis,
  }
})
