<template>
  <div class="section-header">
    <h2>{{ title }}</h2>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>

<style scoped>
.section-header {
  margin-top: 24px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-header::before {
  content: '';
  width: 4px;
  height: 20px;
  border-radius: 4px;
  background: linear-gradient(180deg, #2563eb, #7c3aed);
}
.section-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}
</style>
