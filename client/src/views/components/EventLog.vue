<template>
  <section class="section pb-0 pt-0" style="position: relative">
    <div class="container">
      <div
        class="disable-scrollbars content is-flex is-justify-content-flex-start is-flex-direction-column"
        style="overflow: scroll; height: 300px; width: 400px; padding-top: 40px"
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

const capitalize = (word) => {
  const [first, ...rest] = word.split('')
  return first.toUpperCase() + rest.join('').toLowerCase()
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
        const rows = this.$refs.rows || []
        const [lastRow] = rows.slice(-1)
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
          const Card = capitalize(event.card.value)
          return `played ${Card} ${suits[event.card.suit]}`
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
          const Hand = capitalize(event.handType.replaceAll('_', ' '))
          const points = event.points === 1 ? 'point' : 'points'
          return `got ${event.points} ${points} for a ${Hand}`
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
.disable-scrollbars::-webkit-scrollbar {
  background: transparent; /* Chrome/Safari/Webkit */
  width: 0px;
}

.disable-scrollbars {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none;  /* IE 10+ */
}

.fade-overlay {
  position: absolute;
  width: 100%;
  top: 0;
  left: 0;
  height: 40px;
  background: linear-gradient(rgba(255,255,255,255), rgba(255,255,255,0));
}
</style>
