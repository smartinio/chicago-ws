<template>
  <section class="smooth-slide section pb-0 pt-0 is-flex is-flex-direction-column">
    <div class="is-flex is-flex-direction-row" style="flex: 1; overflow-x: hidden">
      <div class="is-flex is-flex-direction-column is-justify-content-space-between" style="height: 100vh">
        <div v-if="!isChatHidden" class="is-flex" style="flex: 1; overflow: hidden; padding-top: 30px; padding-bottom: 30px">
          <Chat :game="game" :connected="connected" @leave="leave" @hide="isChatHidden = true" @show="isChatHidden = false" />
        </div>
      </div>
      <button v-if="isChatHidden" class="button is-small is-rounded is-info is-light" @click="isChatHidden = false" style="margin-top: 30px">
          <span class="icon is-small"><i class="fa fa-comments"></i></span>
          <span>Chat</span>
        </button>
      <div style="width: 30px" />
      <div class="container is-flex is-flex-direction-column">
        <div style="padding-top: 30px">
          <div class="is-flex is-flex-direction-row is-justify-content-space-between">
            <Player
              :isMe="true"
              variant="large"
              fallbackName="You"
              :player="controlPlayer"
              :currentPlayer="currentPlayer"
              :dealer="dealer"
              :chicagoTaker="chicagoTaker"
              :roundWinner="roundWinner"
            />
            <Controls :game="game" :me="me" :controlPlayer="controlPlayer" :currentPlayer="currentPlayer" :dealer="dealer"
               :markedCards="markedCards" @action="unmarkAll" />
          </div>
          <Players
            v-if="otherPlayers"
            :players="otherPlayers"
            :currentPlayer="currentPlayer"
            :dealer="dealer"
            :chicagoTaker="chicagoTaker"
            :roundWinner="roundWinner"
          />
        </div>
        <div class="is-flex is-justify-content-flex-end" style="flex: 1">
          <MyHand v-if="game.started" :me="me" :markedCards="markedCards" :phase="game.round.phase" :game="game"
            @toggleMark="toggleMark" @action="unmarkAll" />
        </div>
      </div>
    </div>
  </section>
</template>
<script lang="ts">
import { PLAYING, CHICAGO } from '@/store/modules/game/phase_types'
import Controls from '@/views/components/Controls.vue'
import Players from '@/views/components/Players.vue'
import MyHand from '@/views/components/MyHand.vue'
import Chat from '@/views/components/Chat.vue'
import Player from '@/views/components/Player.vue'
import { GamePhase, PlayingCard } from '@/server-types'

const isCard = (a: PlayingCard) => (b: PlayingCard) => a.suit === b.suit && a.value === b.value
const isNotCard = (a: PlayingCard) => (b: PlayingCard) => !isCard(a)(b)

export default {
  name: 'Game',
  props: {
    connected: {
      type: Boolean,
      required: true,
    },
  },
  watch: {
    ['game.round.phase'](phase) {
      if (phase === 'AFTER') {
        this.unmarkAll()
      }
    },
    connected(conn) {
      if (conn && this.game.stale) {
        this.$emit('rejoin')
      }
    }
  },
  components: {
    Controls,
    Players,
    MyHand,
    Chat,
    Player,
  },
  data() {
    return {
      isChatHidden: false,
      markedCards: [] as PlayingCard[]
    }
  },
  methods: {
    leave() {
      this.$emit('leave')
      setTimeout(() => {
        window.location.href = '/'
      }, 0)
    },
    toggleMark(card: PlayingCard) {
      if (this.isPhase(CHICAGO)) {
        this.unmarkAll()
        return
      }
      if (this.isPhase(PLAYING)) {
        this.markSingle(card)
        return
      }
      if (this.isMarked(card)) {
        this.unmark(card)
      } else {
        this.mark(card)
      }
    },
    unmark(card: PlayingCard) {
      this.markedCards = this.markedCards.filter(isNotCard(card))
    },
    unmarkAll() {
      this.markedCards = []
    },
    mark(card: PlayingCard) {
      if (!this.isMarked(card)) {
        this.markedCards.push(card)
      }
    },
    markSingle(card: PlayingCard) {
      this.markedCards = [card]
    },
    isMarked(card: PlayingCard) {
      return this.markedCards.some(isCard(card))
    },
    isPhase(phase: GamePhase) {
      return this.game.round.phase === phase
    },
  },
  computed: {
    game() {
      return this.$store.state.game
    },
    me() {
      return this.$store.state.me
    },
    chicagoTaker() {
      return this.game.round && this.game.round.chicagoTaker
    },
    roundWinner() {
      return this.game.round && this.game.round.winner
    },
    currentPlayer() {
      return this.game.round && this.game.round.currentPlayer
    },
    dealer() {
      return this.game.dealer
    },
    controlPlayer() {
      return this.game.players.find(p => p.id === this.me.id)
    },
    otherPlayers() {
      const everyone = [...this.game.players]
      const myIndex = everyone.findIndex(p => p.id === this.me.id)
      for (let i = 0; i < myIndex; i++) {
        const player = everyone.shift()
        player && everyone.push(player)
      }
      return everyone.slice(1)
    }
  }
}
</script>
<style lang="scss">
.smooth-slide {
  transition: transform 0.3s ease;
  transform: translateX(0);
}

.hide-chat {
  transform: translateX(-415px);
}

.is-size-7 img.emoji {
  width: 0.75rem;
  height: 0.75rem;
  margin-bottom: -1px;
}

img.emoji {
  margin-bottom: -3px;
  width: 1rem;
  height: 1rem;
}

.red-pulse {
  border: 2px solid;
  box-sizing: border-box;
  border-radius: 5px;
  animation: border-pulse 1s infinite ease-in-out alternate;
}

.avatar {
  border-radius: 25%;
}

@keyframes border-pulse {
  from {
    border-color: rgba(255, 0, 0, 0);
  }

  to {
    border-color: rgba(255, 0, 0, 255);
  }
}
</style>
