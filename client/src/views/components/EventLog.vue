<template>
  <div class="disable-scrollbars is-flex is-justify-content-flex-start is-flex-direction-column"
    style="overflow: scroll; width: 400px"
    v-chat-scroll="{ always: false, smooth: true }">
    <div v-show="false" v-html="preload()" />
    <div v-for="event in events">
      <div v-if="event.actor.id === 'server' && formatServerEvent(event)"
        style="border-bottom: 1px solid #ccc; height: 15px; text-align: center; margin-bottom: 15px">
        <span class="has-text-grey is-size-7" style="height: 40px; background-color: #f5f5f5; padding: 5px"
          v-html="formatServerEvent(event)" />
      </div>
      <div v-else-if="formatPlayerEvent(event)">
        <span class="has-text-grey-light is-size-7" style="line-height: 30px;">
          {{ format(event.timestamp, 'HH:mm') }}
        </span>
        <span width="30px" style="display: inline-block" />
        <strong>{{ event.actor.name }}</strong>
        <span v-html="formatPlayerEvent(event)" />
      </div>
      <span ref="rows"></span>
    </div>
  </div>
</template>

<script>
import helpers from '@/mixins/helpers'
import { format } from 'date-fns'

const { withEmojis, capitalize, niceCard } = helpers.methods

const points = (event) => {
  const numerus = event.points === 1 ? 'point' : 'points'
  return `${event.points} ${numerus}`
}

const sanitize = (string) => {
  const pre = document.createElement('pre')
  const text = document.createTextNode(string)
  pre.appendChild(text)
  return pre.innerHTML ? pre.innerHTML.trim() : undefined
}

export default {
  props: ['game'],
  name: 'EventLog',
  computed: {
    events() {
      return this.game.events
    }
  },
  methods: {
    format,
    preload: withEmojis(() => ('✨💬👁️💰👀🤔🔁🚀💪🥲🥳🙌👑👋💦🍃👞👨‍✈️🌻♣️❤️♦️♠️')),
    formatServerEvent: withEmojis((event) => {
      switch (event.action) {
        case 'NEW_ROUND':
          return '✨ A new round begins ✨'
        case 'TRICK_DONE':
          return '👀'
      }
    }),
    formatPlayerEvent: withEmojis((event) => {
      switch (event.action) {
        case 'CHAT_MESSAGE':
          const message = sanitize(event.message)
          return message ? `💬 ${message}` : undefined
        case 'TRADED': {
          const cards = event.numCards === 1 ? 'card' : 'cards'
          const icon = event.numCards === 0 ? '🤔' : '🔁'
          return `traded ${event.numCards} ${cards} ${icon}`
        }
        case 'PLAYED':
          return `played ${niceCard(event.card)}`
        case 'WON_TRICK':
          return `took the trick!`
        case 'CALLED_CHICAGO':
          return 'called Chicago! 🚀'
        case 'LOST_CHICAGO':
          return 'lost 15 points for failing their Chicago... 🥲'
        case 'WON_CHICAGO':
          return 'got 15 points for their Chicago! 🥳'
        case 'WON_ROUND': {
          const winning = event.points === 5 ? `closing with a Two!` : 'winning!'
          return `got ${points(event)} for ${winning} 🙌`
        }
        case 'WON_BEST_HAND':
          const Hand = capitalize(event.handType.replaceAll('_', ' '))
          return `got ${points(event)} for a ${Hand} 💰`
        case 'WON_GAME':
          return `won the game! 👑 👑 👑`
        case 'WON_ROUND_GUARANTEED': {
          const cards = event.cards.map(niceCard).join(', ')
          if (event.cards.length === 1) {
            return `won the round with an unbeatable ${cards}! 💪`
          }
          return `made it rain 💦 with ${cards}`
        }
        case 'CREATED_GAME': {
          return 'created the game 🌻'
        }
        case 'BECAME_HOST': {
          return 'became the new game host 👨‍✈️'
        }
        case 'JOINED_GAME': {
          return 'joined the game 👋'
        }
        case 'LEFT_GAME': {
          return event.kicked ? 'was kicked from the game 👞' : 'left the game 🍃'
        }
        case 'REQUESTED_ONE_OPEN': {
          return `is being offered ${niceCard(event.card)}`
        }
        case 'RESPONDED_TO_ONE_OPEN': {
          return event.accepted ? 'accepted 👍' : 'declined 👎'
        }
      }
    }),
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
