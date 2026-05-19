<template>
  <div class="page-header" :class="{ compact }">
    <h1>{{ title }}</h1>
    <p v-if="subtitle">{{ subtitle }}</p>
    <div v-if="$slots.extra" class="page-header-extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  compact?: boolean
}>()
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
  padding: 4px 0 0;
}
.page-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}
.page-header p {
  font-size: 14px;
  color: #64748b;
  margin-top: 6px;
}
.page-header-extra {
  margin-top: 12px;
}
.page-header.compact h1 {
  font-size: 22px;
}
</style>
