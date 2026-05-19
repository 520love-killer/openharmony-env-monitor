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
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.82), rgba(15, 23, 42, 0.72));
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 22px;
  box-shadow:
    0 18px 45px rgba(0, 0, 0, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}
.card-hover {
  transition: all 0.25s ease;
}
.card-hover:hover {
  transform: translateY(-2px);
  border-color: rgba(56, 189, 248, 0.35);
  box-shadow:
    0 22px 60px rgba(8, 47, 73, 0.35),
    0 0 0 1px rgba(56, 189, 248, 0.10);
}
.card-base.flat {
  box-shadow: none;
}
.card-base.flat:hover {
  transform: none;
  box-shadow: none;
}
</style>
