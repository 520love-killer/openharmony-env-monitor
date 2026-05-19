<template>
  <div class="empty-state">
    <p>{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  text?: string
}>()
</script>

<style scoped>
.empty-state {
  text-align: center;
  color: var(--text-muted);
  padding: 32px 24px;
  font-size: 14px;
}
</style>
