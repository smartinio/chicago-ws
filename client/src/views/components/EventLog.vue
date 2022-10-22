<template>
  <section class="section pb-0 pt-0" style="position: relative">
    <div class="container">
      <div v-show="true" v-html="f('✨💰👀🤔🔁🚀🥲🥳🙌👑💦♣️❤️♦️♠️')" />
      <div
        class="disable-scrollbars content is-flex is-justify-content-flex-start is-flex-direction-column"
        style="overflow: scroll; height: 300px; width: 400px; padding-top: 40px"
        >
        <div v-for="event in events">
          <div
            v-if="event.actor.id === 'server' && formatServerEvent(event)"
            style="border-bottom: 1px solid #ccc; height: 15px; text-align: center; margin-bottom: 15px"
          >
            <span
              class="has-text-grey is-size-7"
              style="height: 40px; background-color: #fff; padding: 5px"
              v-html="f(formatServerEvent(event))"
            />
          </div>
          <div v-else-if="formatPlayerEvent(event)">
            <strong>{{ event.actor.name }}</strong>
            <span v-html="f(formatPlayerEvent(event))" />
            <span class="has-text-grey-light is-size-7" style="line-height: 30px">{{ timeago(event.timestamp) }}</span>
          </div>
          <span ref="rows"></span>
        </div>
      </div>
    </div>
    <div class="fade-overlay" />
  </section>
</template>
<script>
import { format as timeago } from 'timeago.js'
import EmojiConvertor from 'emoji-js'

const emoji = new EmojiConvertor();
emoji.replace_mode = 'img'
emoji.img_set = 'apple'
emoji.img_sets.apple.path = 'https://raw.githubusercontent.com/iamcal/emoji-data/master/img-apple-64/'

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

const niceCard = (card) => {
  const Card = capitalize(card.value)
  return `${suits[card.suit]} ${Card}`
}

export default {
  props: ['game'],
  name: 'EventLog',
  computed: {
    events() {
      return this.game.events
    }
  },
  mounted() {
    window.onfocus = () => {
      this.scrollToBottom()
    }
  },
  watch: {
    events() {
      this.scrollToBottom()
    }
  },
  methods: {
    timeago,
    f: emoji.replace_unified.bind(emoji),
    formatServerEvent(event) {
      switch (event.action) {
        case 'NEW_ROUND':
          return '✨ A new round begins ✨'
        case 'TRICK_DONE':
          return '👀'
      }
    },
    formatPlayerEvent(event) {
      switch (event.action) {
        case 'TRADED': {
          const cards = event.numCards === 1 ? 'card' : 'cards'
          const icon = event.numCards === 0 ? '🤔' : '🔁'
          return `traded ${event.numCards} ${cards} ${icon}`
        }
        case 'PLAYED':
          return `played ${niceCard(event.card)}`
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
          return `got ${event.points} ${points} for a ${Hand} 💰`
        case 'WON_GAME':
          return `won the game! 👑 👑 👑`
        case 'WON_ROUND_GUARANTEED': {
          const cards = event.cards.map(niceCard).join(', ')
          return `made it rain 💦 with ${cards}`
        }
      }
    },
    scrollToBottom() {
      requestAnimationFrame(() => {
        const rows = this.$refs.rows || []
        const [lastRow] = rows.slice(-1)
        if (lastRow) {
          lastRow.scrollIntoView({ behavior: 'smooth' });
        }
      })
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
