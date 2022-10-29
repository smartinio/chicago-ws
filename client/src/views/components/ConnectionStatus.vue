<template>
  <div>
    <a class="button is-small is-rounded" :class="statusClass">
      <span class="icon is-small" :class="animationClass"><i class="fa" :class="statusIcon"></i></span>
      <span>{{ statusText }}</span>
    </a>
  </div>
</template>

<script>
export default {
  props: {
    connected: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    statusText () {
      return this.connected ? 'Connected' : 'Connecting'
    },
    statusClass () {
      return this.connected ? 'is-success is-light' : 'is-warning'
    },
    statusIcon () {
      return this.connected ? 'fa-check' : 'fa-spinner'
    },
    animationClass () {
      return this.connected ? '' : 'is-spinning'
    }
  }
}
</script>

<style scoped>
  .is-spinning {
    animation: spin 2s linear infinite;
  }

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
</style>
