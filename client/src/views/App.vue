<template>
  <div id="app">
    <router-view></router-view>
    <Preloader />
  </div>
</template>

<script>
import Preloader from '@/views/components/Preloader'
export default {
  name: 'app',
  components: {
    Preloader
  }
}
</script>

<style>

</style>
