<template>
  <section class="section pb-0 pt-0 is-flex is-flex-direction-column">
    <div class="is-flex is-flex-direction-row" style="flex: 1">
      <div class="is-flex is-flex-direction-column is-justify-content-space-between" style="height: 100vh">
        <div class="is-flex" style="flex: 1; overflow: hidden; padding-top: 30px; padding-bottom: 30px">
          <Chat :game="game" :connected="connected" @leave="leave" />
        </div>
      </div>
      <div style="width: 30px" />
      <div class="container is-flex is-flex-direction-column">
        <div style="padding-top: 30px">
          <div class="is-flex is-flex-direction-row is-justify-content-space-between">
            <Player :isMe="true" variant="large" fallbackName="You" :player="controlPlayer" :baseMove="baseMove"
              :currentPlayer="currentPlayer" :dealer="dealer" :chicagoTaker="chicagoTaker" />
            <Controls :game="game" :me="me" :controlPlayer="controlPlayer" :currentPlayer="currentPlayer" :dealer="dealer"
              :baseMove="baseMove" :markedCards="markedCards" @action="unmarkAll" />
          </div>
          <Players v-if="otherPlayers" :players="otherPlayers" :baseMove="baseMove" :currentPlayer="currentPlayer"
            :dealer="dealer" :chicagoTaker="chicagoTaker" />
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

const isCard = (a) => (b) => a.suit === b.suit && a.value === b.value
const isNotCard = (a) => (b) => !isCard(a)(b)

export default {
  name: 'Game',
  props: {
    connected: {
      type: Boolean,
      required: true,
    },
    game: {
      type: Object,
      required: true
    },
    me: {
      type: Object,
      required: true
    }
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
      markedCards: []
    }
  },
  methods: {
    leave() {
      this.$emit('leave')
      setTimeout(() => {
        window.location.href = '/'
      }, 0)
    },
    toggleMark(card) {
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
    unmark(card) {
      this.markedCards = this.markedCards.filter(isNotCard(card))
    },
    unmarkAll() {
      this.markedCards = []
    },
    mark(card) {
      if (!this.isMarked(card)) {
        this.markedCards.push(card)
      }
    },
    markSingle(card) {
      this.markedCards = [card]
    },
    isMarked(card) {
      return this.markedCards.some(isCard(card))
    },
    isPhase(phase) {
      return this.game.round.phase === phase
    }
  },
  computed: {
    baseMove() {
      if (!this.currentTrick) return
      return this.currentTrick.moves &&
        this.currentTrick.moves.length > 0 &&
        this.currentTrick.moves[0]
    },
    currentTrick() {
      const round = this.game.round
      const tricks = round && round.tricks
      if (!tricks) return
      return tricks.length > 0 && tricks[tricks.length - 1]
    },
    chicagoTaker() {
      return this.game.round && this.game.round.chicagoTaker
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
        everyone.push(everyone.shift())
      }
      return everyone.slice(1)
    }
  }
}
</script>
<style lang="scss">
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
