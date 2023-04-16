<template>
  <div id="app">
    <router-view></router-view>
    <Preloader />
  </div>
</template>

<script lang="ts">
import Preloader from '@/views/components/Preloader.vue'

let keepalive: number

export default {
  name: 'app',
  components: {
    Preloader
  },
  mounted() {
    if (import.meta.env.PROD) {
      keepalive = setInterval(() => {
        fetch(import.meta.env.VITE_HTTP_URL, { method: 'HEAD' })
      }, 10000)
    }
  },
  unmounted() {
    clearInterval(keepalive)
  }
}
</script>

<style>
html,
body {
  height: 100%;
  overflow: hidden;
}
</style>
