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
  gap: 10px;
}
.section-header::before {
  content: '';
  width: 4px;
  height: 22px;
  border-radius: 4px;
  background: linear-gradient(180deg, var(--blue), var(--purple));
  box-shadow: 0 0 14px var(--blue-glow);
}
.section-header h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-heading);
  letter-spacing: -0.2px;
}
</style>
