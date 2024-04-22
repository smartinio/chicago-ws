<template>
  <div class="media" :style="player.connected ? '' : 'opacity: 0.2'" style="min-height: 105px">
    <div class="media-left">
      <figure class="image" :class="figureClass">
        <img ref="avatarRef" :src="getAvatarUrl(player)" alt="Placeholder image" class="avatar">
      </figure>
    </div>
    <div class="media-content">
      <div class="columns is-flex">
        <div class="column is-narrow">
          <p class="title is-4"
            style="text-overflow: ellipsis; width: 150px !important; overflow: hidden; white-space: nowrap;">
            <span>{{ player.name || fallbackName }}</span>
            <span class="shake-it" v-if="isChicagoTaker(player)" v-html="emojify('🚀')"></span>
          </p>
          <p class="subtitle is-6" style="margin-bottom: 5px">
            {{ player.score }} points
          </p>
          <button v-if="!isMe && imHost" @click="kickPlayer(player)" class="button is-rounded is-small is-danger">
            <span class="icon is-small">
              <i class="fa fa-user-slash"></i>
            </span>
          </button>
        </div>
        <div class="column is-expanded">
          <div v-if="player.hand" style="padding-right: 40px">
            <figure v-for="card, idx in player.hand.played" class="image is-inline-flex opaque"
              style="width: 66px; height: 96px; margin-right: -40px">
              <img :src="getCardUrl(card)" :alt="card.type + '' + card.value"
                :class="isBaseCard(card) ? 'red-pulse' : isOldNews(card, idx) ? 'dim' : ''">
            </figure>
          </div>
        </div>
        <div class="column is-narrow" v-if="!isMe">
          <PlayerStatus :currentPlayer="currentPlayer" :dealer="dealer" :player="player" />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import confetti from 'canvas-confetti'
import { ref } from 'vue'

import PlayerStatus from '@/views/components/PlayerStatus.vue'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import { KICK_PLAYER } from '@/dto/action/types'
import Action from '@/dto/action/Action'
import { Player, PlayingCard } from '@/server-types'

export default {
  name: 'Player',
  props: ['player', 'currentPlayer', 'dealer', 'chicagoTaker', 'roundWinner', 'fallbackName', 'variant', 'isMe'],
  setup() {
    const avatarRef = ref<HTMLImageElement>()
    const wintervalRef = ref<number>()

    return { avatarRef, wintervalRef }
  },
  components: {
    PlayerStatus
  },
  computed: {
    game() {
      return this.$store.state.game
    },
    baseMove() {
      if (!this.currentTrick) return
      return this.currentTrick.moves &&
        this.currentTrick.moves.length > 0 &&
        this.currentTrick.moves[0]
    },
    currentTrick() {
      const { tricks } = this.game.round
      return tricks.length > 0 ? tricks[tricks.length - 1] : null
    },
    isRoundWinner() {
      return this.roundWinner && this.player.id === this.roundWinner.id;
    },
    imHost() {
      return this.$store.state.me.id === this.$store.state.game.host.id
    },
    figureClass() {
      const size = this.variant === 'large' ? 96 : 48
      return `is-${size}x${size}`
    }
  },
  watch: {
    isRoundWinner(is: boolean) {
      if (is) {
        this.celebratePlayer()
      }
    },
    player(is: Player, was: Player) {
      if (is.winner && !was.winner) {
        const offset = Math.ceil(Math.random() * 30) - 15

        this.wintervalRef = setInterval(() => this.celebratePlayer(offset), 1500)
      }

      if (!is.winner) {
        clearInterval(this.wintervalRef)
      }
    }
  },
  methods: {
    celebratePlayer(offset = 0) {
      const position = this.avatarRef?.getBoundingClientRect();
      if (position) {
        const { clientWidth, clientHeight } = document.documentElement
        const x = (position.x + 35 + offset) / clientWidth
        const y = (position.y + 110 + offset) / clientHeight
        confetti({ origin: { x, y }, startVelocity: 20, ticks: 75 })
      }
    },
    kickPlayer(player: Player) {
      const actionDTO = new Action(KICK_PLAYER, player.id)
      this.$store.dispatch(SEND_ACTION, actionDTO)
    },
    isOldNews(card: PlayingCard, idx: number) {
      if (!this.currentTrick?.moves.length) {
        return idx < this.player.hand.played.length - 1;
      }

      return !this.currentTrick.moves.some(m => this.isCard(card, m.card))
    },
    isCard(cardA: PlayingCard, cardB: PlayingCard) {
      return cardA.suit === cardB.suit && cardA.value === cardB.value
    },
    isBaseCard(card: PlayingCard) {
      if (!this.baseMove) return false
      return this.isCard(card, this.baseMove.card)
    },
    isChicagoTaker(player: Player) {
      return this.chicagoTaker && player.id === this.chicagoTaker.id
    },
  }
}
</script>

<style scoped>
.shake-it {
  display: inline-block;
  animation: shake 0.5s infinite;
}

.opaque {
  background-color: #FFF;
}

.dim {
  opacity: 0.4;
}

@keyframes shake {
  0% {
    transform: translate(1px, 1px) rotate(0deg);
  }

  10% {
    transform: translate(-1px, -2px) rotate(-1deg);
  }

  20% {
    transform: translate(-2px, 0px) rotate(1deg);
  }

  30% {
    transform: translate(2px, 2px) rotate(0deg);
  }

  40% {
    transform: translate(1px, -1px) rotate(1deg);
  }

  50% {
    transform: translate(-1px, 2px) rotate(-1deg);
  }

  60% {
    transform: translate(-2px, 1px) rotate(0deg);
  }

  70% {
    transform: translate(2px, 1px) rotate(-1deg);
  }

  80% {
    transform: translate(-1px, -1px) rotate(1deg);
  }

  90% {
    transform: translate(1px, 2px) rotate(0deg);
  }

  100% {
    transform: translate(1px, -2px) rotate(-1deg);
  }
}
</style>
