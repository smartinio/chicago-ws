<template>
  <div id="app">
    <router-view></router-view>
    <Preloader />
  </div>
</template>

<script>
import Preloader from '@/views/components/Preloader'

let keepalive

export default {
  name: 'app',
  components: {
    Preloader
  },
  mounted() {
    keepalive = setInterval(() => {
      fetch(process.env.HTTP_URL, { method: 'HEAD' })
    }, 10000)
  },
  unmounted() {
    clearInterval(keepalive)
  }
}
</script>

<style>

</style>
