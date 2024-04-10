<script setup lang="ts">
import { reactive, ref } from 'vue';

const scrollRef = ref<HTMLElement | null>(null)
const scrollState = reactive({ enabled: true })

const scrollToBottom = (eventIndex: number, events: any[]) => {
  if (scrollState.enabled && scrollRef.value && eventIndex + 1 === events.length) {
    scrollRef.value.scrollTo({ top: scrollRef.value.scrollHeight, behavior: 'smooth' })
  }
}
</script>

<template>
  <div ref="scrollRef" @mouseenter="scrollState.enabled = false" @mouseleave="scrollState.enabled = true"
    class="disable-scrollbars is-flex is-justify-content-flex-start is-flex-direction-column"
    style="overflow: scroll; width: 400px">
    <div v-show="false" v-html="preload()" />
    <div v-for="event, i in events">
      <Event :event="event" @vue:mounted="scrollToBottom(i, events)" />
    </div>
  </div>
</template>

<script lang="ts">
import Event from './Event.vue'
import helpers from '@/mixins/helpers'
import { format } from 'date-fns'

const { withEmojis } = helpers.methods

export default {
  props: ['game'],
  name: 'EventLog',
  components: {
    Event,
  },
  data() {
    return {
      chatScroll: {
        enabled: true,
        always: true,
        smooth: true
      }
    }
  },
  computed: {
    events() {
      return this.game.events
    }
  },
  methods: {
    format,
    preload: withEmojis(() => ('✨💬👁️💰👀🤔🔁🚀💪🥲🥳🙌👑👋💦🍃👞👨‍✈️🌻♣️❤️♦️♠️'))
  },
}
</script>

<style lang="scss" scoped>
.disable-scrollbars::-webkit-scrollbar {
  background: transparent;
  /* Chrome/Safari/Webkit */
  width: 0px;
}

.disable-scrollbars {
  scrollbar-width: none;
  /* Firefox */
  -ms-overflow-style: none;
  /* IE 10+ */
}
</style>
