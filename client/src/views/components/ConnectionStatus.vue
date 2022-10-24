<template>
  <div>
    <a @click="handleClick" class="button is-small is-rounded" :class="statusClass">
      <span class="icon is-small"><i class="fa" :class="statusIcon"></i></span>
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
  methods: {
    handleClick () {
      if (this.connected) {
        return
      }
      this.$emit('reconnect')
    }
  },
  computed: {
    statusText () {
      return this.connected ? 'Connected to server' : 'Not connected. Click to try again'
    },
    statusClass () {
      return this.connected ? 'is-success' : 'is-danger'
    },
    statusIcon () {
      return this.connected ? 'fa-check' : 'fa-refresh'
    }
  }
}
</script>
