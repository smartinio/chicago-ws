<template>
  <section class="section pb-0 pt-0" style="position: relative">
    <div class="container">
      <div
        class="content is-flex is-justify-content-flex-start is-flex-direction-column"
        style="overflow: scroll; height: 70px; padding-top: 70px"
        >
        <div
          v-for="event, index in events"
          v-if="formatEvent(event, index)"
          >
          <strong>{{ event.actor.name }}</strong>
          <span> {{ formatEvent(event) }}</span>
          <span class="has-text-grey-light is-size-7">{{ timeago(event.timestamp) }}</span>
          <span ref="rows"></span>
        </div>
      </div>
    </div>
    <div class="fade-overlay" />
  </section>
</template>
<script>
import { format as timeago } from 'timeago.js'

const suits = {
  CLUBS: '♣️',
  HEARTS: '❤️',
  DIAMONDS: '♦️',
  SPADES: '♠️'
}

export default {
  props: ['game'],
  name: 'EventLog',
  computed: {
    events() {
      return this.game.events
    }
  },
  watch: {
    events() {
      requestAnimationFrame(() => {
        const [lastRow] = this.$refs.rows.slice(-1)
        if (lastRow) {
          lastRow.scrollIntoView({ behavior: 'smooth' });
        }
      })
    }
  },
  methods: {
    timeago,
    formatEvent(event) {
      switch (event.action) {
        case 'TRADED':
          const cards = event.numCards === 1 ? 'card' : 'cards'
          const icon = event.numCards === 0 ? '🤔' : '🫳'
          return `traded ${event.numCards} ${cards} ${icon}`
        case 'PLAYED':
          return `played ${event.card.value} ${suits[event.card.suit]}`
        case 'WON_TRICK':
          return `won the trick!`
        case 'CALLED_CHICAGO':
          return 'called Chicago! 🚀'
        case 'LOST_CHICAGO':
            return 'lost their Chicago... 🥲'
        case 'WON_CHICAGO':
          return 'won their Chicago! 🥳'
        case 'WON_ROUND':
          return `won the round! 🙌`
        case 'WON_BEST_HAND':
          const handType = event.handType.toLowerCase().replace('_', ' ')
          const points = event.points === 1 ? 'point' : 'points'
          return `got ${event.points} ${points} for a ${handType}`
        case 'WON_GAME':
          return `won the game! 👑 👑 👑`
        default:
          return JSON.stringify(event)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.fade-overlay {
  position: absolute;
  width: 100%;
  top: 0;
  left: 0;
  height: 40px;
  background: linear-gradient(rgba(255,255,255,255), rgba(255,255,255,0));
}
</style>
