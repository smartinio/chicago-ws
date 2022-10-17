<template>
  <div>
    <Controls
      :game="game"
      :me="me"
      :controlPlayer="controlPlayer"
      :currentPlayer="currentPlayer"
      :baseMove="baseMove"
      :markedCards="markedCards"
      @action="unmarkAll"
    />
    <section class="section pb-0 pt-0 is-flex is-flex-direction-row">
      <EventLog
        :game="game"
      />
      <MyHand
        v-if="game.started"
        :me="me"
        :markedCards="markedCards"
        @toggleMark="toggleMark"
      />
    </section>
    <Players
      v-if="otherPlayers"
      :players="otherPlayers"
      :baseMove="baseMove"
      :currentPlayer="currentPlayer"
    />
  </div>
</template>
<script>
import { PLAYING, CHICAGO } from '@/store/modules/game/phase_types'
import Controls from '@/views/components/Controls'
import Players from '@/views/components/Players'
import MyHand from '@/views/components/MyHand'
import EventLog from '@/views/components/EventLog'

export default {
  name: 'Game',
  props: {
    game: {
      type: Object,
      required: true
    },
    me: {
      type: Object,
      required: true
    }
  },
  components: {
    Controls,
    Players,
    MyHand,
    EventLog,
  },
  data () {
    return {
      markedCards: []
    }
  },
  methods: {
    toggleMark (card) {
      if (!this.me.isMyTurn || this.isPhase(CHICAGO)) {
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
    unmark (card) {
      this.markedCards = this.markedCards.filter(c => c !== card)
    },
    unmarkAll () {
      this.markedCards = []
    },
    mark (card) {
      this.markedCards.push(card)
    },
    markSingle (card) {
      this.markedCards = [card]
    },
    isMarked (card) {
      return this.markedCards.includes(card)
    },
    isPhase (phase) {
      return this.game.round.phase === phase
    }
  },
  computed: {
    baseMove () {
      if (!this.currentTrick) return
      return this.currentTrick.moves &&
        this.currentTrick.moves.length > 0 &&
        this.currentTrick.moves[0]
    },
    currentTrick () {
      const round = this.game.round
      const tricks = round && round.tricks
      if (!tricks) return
      return tricks.length > 0 && tricks[tricks.length - 1]
    },
    currentPlayer () {
      return this.game.round && this.game.round.currentPlayer
    },
    controlPlayer () {
      return this.game.players.find(p => p.id === this.me.id)
    },
    otherPlayers () {
      return this.game.players.filter(p => p.id !== this.me.id)
    }
  }
}
</script>
<style lang="scss">
  .playingCard {
    opacity: 0.5;
    transform: translateY(0px);
    box-shadow: 0 0 5px rgba(0,0,0,0.1);
    transition: all 0.2s ease;
    max-height: 30vh;
  }
  .markedCard {
    transform: translateY(-20px);
    box-shadow: 0px 8px 20px rgba(0,0,0,0.4);
    transition: all 0.3s ease;
    max-height: 30vh;
  }
  .baseCard {
    border: 2px solid;
    box-sizing: border-box;
    border-radius: 5px;
    animation: pulse 1s infinite ease-in-out alternate;
  }
  .avatar {
    border-radius: 25%;
  }
  .myTurn {
    opacity: 1;
  }

  @keyframes pulse {
    from {
      border-color: rgba(255, 0, 0, 0);
    }
    to {
      border-color: rgba(255, 0, 0, 255);
    }
  }
</style>
