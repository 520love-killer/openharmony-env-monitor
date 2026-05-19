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
  margin-bottom: 22px;
  padding: 4px 0 0;
}
.page-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-heading);
  letter-spacing: -0.3px;
}
.page-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin-top: 6px;
}
.page-header-extra {
  margin-top: 12px;
}
.page-header.compact h1 {
  font-size: 22px;
}
</style>
