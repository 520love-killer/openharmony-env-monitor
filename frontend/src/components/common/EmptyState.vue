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
  color: #64748b;
  padding: 24px;
}
</style>
