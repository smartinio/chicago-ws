<template>
    <div class="container breathe">
      <a @click="handleClick" class="button is-pulled-right is-small" :class="statusClass">
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

<style lang="css">
  .breathe {
    margin-top: 20px;
  }
</style>
