<template>
  <div class="card-base card-hover" :class="{ flat }">
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  flat?: boolean
}>()
</script>

<style scoped>
.card-base {
  background: rgba(255,255,255,0.88);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(148,163,184,0.22);
  border-radius: 18px;
  box-shadow: 0 14px 35px rgba(15,23,42,0.06);
}
.card-hover {
  transition: all 0.2s ease;
}
.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 40px rgba(37,99,235,0.10);
}
.card-base.flat {
  box-shadow: none;
}
.card-base.flat:hover {
  transform: none;
  box-shadow: none;
}
</style>
